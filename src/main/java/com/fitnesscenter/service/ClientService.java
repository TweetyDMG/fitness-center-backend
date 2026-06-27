package com.fitnesscenter.service;

import com.fitnesscenter.exception.InvalidRequestException;
import com.fitnesscenter.entity.*;
import com.fitnesscenter.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final SaleRepository saleRepository;
    private final PreferenceRepository preferenceRepository;
    private final DiseaseRepository diseaseRepository;
    private final ScheduleRepository scheduleRepository;
    private final RegistrationOfVisitRepository visitsRepository;
    private final TrainerRepository trainerRepository;

    public ClientService(ClientRepository clientRepository,
                         SaleRepository saleRepository,
                         PreferenceRepository preferenceRepository,
                         DiseaseRepository diseaseRepository,
                         ScheduleRepository scheduleRepository,
                         RegistrationOfVisitRepository visitsRepository,
                         TrainerRepository trainerRepository) {
        this.clientRepository = clientRepository;
        this.saleRepository = saleRepository;
        this.preferenceRepository = preferenceRepository;
        this.diseaseRepository = diseaseRepository;
        this.scheduleRepository = scheduleRepository;
        this.visitsRepository = visitsRepository;
        this.trainerRepository = trainerRepository;
    }

    public Client getClientProfile(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
    }

    @Transactional
    public void updateClientProfile(Client client) {
        if (client == null || client.getId() == null) {
            throw new InvalidRequestException("Некорректный объект клиента для обновления.");
        }
        Client existingClient = clientRepository.findById(client.getId())
                .orElseThrow(() -> new InvalidRequestException("Клиент не найден с ID: " + client.getId()));
        existingClient.setFirstname(client.getFirstname());
        existingClient.setLastname(client.getLastname());
        existingClient.setPatronymic(client.getPatronymic());
        existingClient.setPhone(client.getPhone());
        existingClient.setGender(client.getGender());
        existingClient.setEmail(client.getEmail());
        existingClient.setPassport(client.getPassport());
        clientRepository.save(existingClient);
    }

    public List<Sale> getClientSubscriptions(Long clientId) {
        return saleRepository.findByClientId(clientId);
    }

    public List<Preference> getClientPreferences(Long clientId) {
        return preferenceRepository.findByClientId(clientId);
    }

    public List<Disease> getClientDiseases(Long clientId) {
        return diseaseRepository.findByClientId(clientId);
    }

    public List<RegistrationOfVisit> getClientVisits(Long clientId) {
        List<Sale> clientSales = saleRepository.findByClientId(clientId);
        if (clientSales.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> saleIds = clientSales.stream().map(Sale::getId).collect(Collectors.toList());
        return visitsRepository.findBySaleIdIn(saleIds);
    }

    public int getRemainingVisits(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new InvalidRequestException("Продажа не найдена с ID: " + saleId));
        Subscription subscription = sale.getSubscription();
        if (subscription == null) {
            throw new InvalidRequestException("Детали абонемента не найдены для продажи с ID: " + saleId);
        }
        Integer totalAllowedVisits = subscription.getNumberOfVisits();
        if (totalAllowedVisits == null || totalAllowedVisits <= 0) {
            return Integer.MAX_VALUE;
        }
        int usedVisits = visitsRepository.countBySaleId(saleId);
        return Math.max(0, totalAllowedVisits - usedVisits);
    }

    public int getUsedVisitsForSale(Long saleId) {
        if (!saleRepository.existsById(saleId)) {
            throw new InvalidRequestException("Продажа не найдена с ID: " + saleId);
        }
        return visitsRepository.countBySaleId(saleId);
    }

    public Trainer getTrainerById(Long trainerId) {
        return trainerId == null ? null : trainerRepository.findById(trainerId).orElse(null);
    }

    public List<Schedule> getClientSchedule() {
        return scheduleRepository.findAll();
    }

    public List<Schedule> getClientScheduleByDate(LocalDate date) {
        if (date == null) {
            return Collections.emptyList();
        }
        return scheduleRepository.findByDate(date);
    }

    @Transactional
    public void registerForVisit(Long clientId, Long scheduleId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new InvalidRequestException("Клиент не найден с ID: " + clientId));

        Sale activeSale = saleRepository.findActiveSubscriptionByClientId(clientId, LocalDate.now())
                .orElseThrow(() -> new InvalidRequestException("У клиента нет активного абонемента на текущую дату."));

        Subscription subscription = activeSale.getSubscription();
        if (subscription == null) {
            throw new InvalidRequestException("Детали абонемента не найдены для активной продажи.");
        }

        Integer totalAllowedVisits = subscription.getNumberOfVisits();
        if (totalAllowedVisits != null && totalAllowedVisits > 0) {
            int visitsUsed = visitsRepository.countBySaleId(activeSale.getId());
            if (visitsUsed >= totalAllowedVisits) {
                throw new InvalidRequestException("Превышено количество доступных посещений по абонементу.");
            }
        }

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new InvalidRequestException("Занятие не найдено с ID: " + scheduleId));

        List<Sale> clientSales = saleRepository.findByClientId(clientId);
        if (!clientSales.isEmpty()) {
            List<Long> clientSaleIds = clientSales.stream().map(Sale::getId).collect(Collectors.toList());
            boolean alreadyRegistered = visitsRepository.existsByScheduleIdAndSaleIdIn(scheduleId, clientSaleIds);
            if (alreadyRegistered) {
                throw new InvalidRequestException("Вы уже записаны на это занятие.");
            }
        }

        RegistrationOfVisit visit = new RegistrationOfVisit();
        visit.setScheduleId(schedule.getId());
        visit.setSaleId(activeSale.getId());
        visit.setVisitDate(Timestamp.valueOf(LocalDateTime.now()));
        visitsRepository.save(visit);
    }

    @Transactional
    public void cancelVisit(Long clientId, Long visitId) {
        RegistrationOfVisit visit = visitsRepository.findById(visitId)
                .orElseThrow(() -> new InvalidRequestException("Посещение не найдено с ID: " + visitId));
        Sale sale = saleRepository.findById(visit.getSaleId())
                .orElseThrow(() -> new InvalidRequestException("Продажа (абонемент), по которой было посещение, не найдена."));

        if (sale.getClient() == null || !sale.getClient().getId().equals(clientId)) {
            throw new InvalidRequestException("Отмена запрещена. Это не ваше посещение.");
        }

        Schedule scheduledClass = scheduleRepository.findById(visit.getScheduleId())
                .orElseThrow(() -> new InvalidRequestException("Занятие, связанное с посещением, не найдено."));
        LocalDateTime classDateTime = LocalDateTime.of(scheduledClass.getDate(), scheduledClass.getStartTime().toLocalTime());

        if (classDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Нельзя отменить прошедшее или уже начавшееся занятие.");
        }

        visitsRepository.delete(visit);
    }

    public Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).orElse(null);
    }
}