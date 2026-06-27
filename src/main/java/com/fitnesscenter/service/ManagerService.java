package com.fitnesscenter.service;

import com.fitnesscenter.controller.SaleRequest;
import com.fitnesscenter.dto.ClientFilterRequest;
import com.fitnesscenter.dto.SaleFilterRequest;
import com.fitnesscenter.dto.ScheduleRequest;
import com.fitnesscenter.entity.*;
import com.fitnesscenter.exception.NotFoundException;
import com.fitnesscenter.exception.SubscriptionExpiredException;
import com.fitnesscenter.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ManagerService {

    private final ClientRepository clientRepository;
    private final SaleRepository saleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FitnessCenterRepository fitnessCenterRepository;
    private final DiscountRepository discountRepository;
    private final ClientSpecifications clientSpec;
    private final SaleSpecifications saleSpec;
    private final TrainerRepository trainerRepository;
    private final ScheduleRepository scheduleRepository;
    private final RegistrationOfVisitRepository visitRepository;

    public ManagerService(ClientRepository clientRepository,
                          SaleRepository saleRepository,
                          SubscriptionRepository subscriptionRepository,
                          FitnessCenterRepository fitnessCenterRepository,
                          DiscountRepository discountRepository, ClientSpecifications clientSpec, SaleSpecifications saleSpec, TrainerRepository trainerRepository, ScheduleRepository scheduleRepository, RegistrationOfVisitRepository visitRepository) {
        this.clientRepository = clientRepository;
        this.saleRepository = saleRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.fitnessCenterRepository = fitnessCenterRepository;
        this.discountRepository = discountRepository;
        this.clientSpec = clientSpec;
        this.saleSpec = saleSpec;
        this.trainerRepository = trainerRepository;
        this.scheduleRepository = scheduleRepository;
        this.visitRepository = visitRepository;
    }


    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }


    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public Client updateClient(Long id, Client updatedClient) {
        Client existingClient = getClientById(id);
        existingClient.setFirstname(updatedClient.getFirstname());
        existingClient.setLastname(updatedClient.getLastname());
        existingClient.setPatronymic(updatedClient.getPatronymic());
        existingClient.setEmail(updatedClient.getEmail());
        existingClient.setPhone(updatedClient.getPhone());
        return clientRepository.save(existingClient);
    }


    public Sale createSale(SaleRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        FitnessCenter center = fitnessCenterRepository.findById(request.getFitnessCenterId())
                .orElseThrow(() -> new RuntimeException("Fitness Center not found"));

        Discount discount = null;
        if (request.getDiscountId() != null) {
            discount = discountRepository.findById(request.getDiscountId())
                    .orElseThrow(() -> new RuntimeException("Discount not found"));
        }

        Sale sale = new Sale();
        sale.setBankCardNum(request.getBankCardNum());
        sale.setClient(client);
        sale.setSubscription(subscription);
        sale.setFitnessCenter(center);
        sale.setDiscount(discount);
        sale.setStartDate(java.time.LocalDate.now());

        if (subscription.getValidity() != null) {
            sale.setEndDate(subscription.getValidity());
        }

        return saleRepository.save(sale);
    }


    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }


    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    public Page<Client> getAllClientsWithFilter(ClientFilterRequest filter, int page, int size) {
        if (filter == null) {
            filter = new ClientFilterRequest();
        }
        String sortOrder = filter.getSortOrder() != null ? filter.getSortOrder().toLowerCase() : "asc";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "lastname";
        if (!sortOrder.equals("asc") && !sortOrder.equals("desc")) {
            sortOrder = "asc";
            }
        Sort sort = sortOrder.equals("asc") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 15;
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Client> spec = ClientSpecifications.withFilter(filter);
        Page<Client> result = clientRepository.findAll(spec, pageable);
        return result;
    }

    // --- SALES ---
    public Page<Sale> getAllSalesWithFilter(SaleFilterRequest filter, int page, int size) {
        if (filter == null) {
            filter = new SaleFilterRequest();
        }
        String sortOrder = filter.getSortOrder() != null ? filter.getSortOrder().toLowerCase() : "desc";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "startDate";
        if (!sortOrder.equals("asc") && !sortOrder.equals("desc")) {
            sortOrder = "desc";
            }
        Sort sort = sortOrder.equals("asc") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 15;
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Sale> result = saleRepository.findAll(SaleSpecifications.withFilter(filter), pageable);
        return result;
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public Trainer getTrainerById(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тренер не найден"));
    }

    public List<Schedule> getScheduleByTrainerId(Long trainerId) {
        return scheduleRepository.findByTrainer(trainerId);
    }

    public Schedule addScheduleToTrainer(Long trainerId, ScheduleRequest request) {
        Schedule schedule = new Schedule();
        schedule.setDate(Date.valueOf(request.getDate()).toLocalDate());
        schedule.setStartTime(Time.valueOf(LocalTime.parse(request.getStartTime())));
        schedule.setEndTime(Time.valueOf(LocalTime.parse(request.getEndTime())));
        schedule.setTrainer(trainerId);

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void assignClientToTrainer(Long trainerId, Long clientId, Long scheduleId) {
        if (trainerId == null || clientId == null || scheduleId == null) {
            throw new IllegalArgumentException("ID тренера, клиента или слота не может быть null");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Клиент не найден"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Слот не найден"));

        if (!schedule.getTrainer().equals(trainerId)) {
            throw new IllegalArgumentException("Слот не принадлежит выбранному тренеру");
        }

        Sale activeSale = saleRepository.findActiveSubscriptionByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Нет активного абонемента"));

        Subscription subscription = activeSale.getSubscription();
        if (subscription == null) {
            throw new NotFoundException("Абонемент не найден");
        }

        int visitsUsed = visitRepository.countBySaleId(activeSale.getId());
        if (visitsUsed >= subscription.getNumberOfVisits()) {
            throw new SubscriptionExpiredException("Превышено количество доступных посещений");
        }

        if (visitRepository.existsByScheduleId(scheduleId)) {
            throw new RuntimeException("Этот слот уже занят");
        }

        RegistrationOfVisit visit = new RegistrationOfVisit();
        visit.setVisitDate(Timestamp.valueOf(schedule.getDate().atStartOfDay()));
        visit.setScheduleId(scheduleId);
        visit.setSaleId(activeSale.getId());
        visitRepository.save(visit);
    }

    public List<Schedule> getAvailableFutureSlots(Long trainerId) {
        List<Schedule> futureSlots = scheduleRepository.findFutureSlotsByTrainerId(trainerId);
        return futureSlots.stream()
                .filter(schedule -> !visitRepository.existsByScheduleId(schedule.getId()))
                .toList();
    }

    @Transactional
    public void cancelClientVisit(Long visitId) {
        RegistrationOfVisit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Посещение не найдено"));

        Sale sale = saleRepository.findById(visit.getSaleId())
                .orElseThrow(() -> new RuntimeException("Продажа не найдена"));

        Subscription subscription = subscriptionRepository.findById(sale.getSubscription().getId())
                .orElseThrow(() -> new RuntimeException("Абонемент не найден"));

        if (visit.getVisitDate() == null) {
            throw new RuntimeException("Дата посещения не указана");
        }

        LocalDateTime visitDateTime = visit.getVisitDate().toInstant()
                .atZone(ZoneId.of("Europe/Moscow"))
                .toLocalDateTime();

        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"));

        System.out.println("Visit DateTime (MSK): " + visitDateTime);
        System.out.println("Current DateTime (MSK): " + currentDateTime);

        if (visitDateTime.isBefore(currentDateTime)) {
            throw new RuntimeException("Нельзя отменить прошедшее занятие");
        }

        visitRepository.deleteById(visitId);
    }

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public List<FitnessCenter> getAllFitnessCenters() {
        return fitnessCenterRepository.findAll();
    }

    public void addSchedule(Schedule schedule) {
        scheduleRepository.save(schedule);
    }
}