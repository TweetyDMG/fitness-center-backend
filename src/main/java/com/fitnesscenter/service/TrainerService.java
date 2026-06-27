package com.fitnesscenter.service;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private final ClientRepository clientRepository;
    private final PreferenceRepository preferenceRepository;
    private final DiseaseRepository diseaseRepository;
    private final ScheduleRepository scheduleRepository;
    private final RegistrationOfVisitRepository visitsRepository;
    private final RecommendationRepository recommendationRepository;
    private final SaleRepository saleRepository;

    public TrainerService(ClientRepository clientRepository,
                          PreferenceRepository preferenceRepository,
                          DiseaseRepository diseaseRepository,
                          ScheduleRepository scheduleRepository,
                          RegistrationOfVisitRepository visitsRepository,
                          RecommendationRepository recommendationRepository,
                          SaleRepository saleRepository) {
        this.clientRepository = clientRepository;
        this.preferenceRepository = preferenceRepository;
        this.diseaseRepository = diseaseRepository;
        this.scheduleRepository = scheduleRepository;
        this.visitsRepository = visitsRepository;
        this.recommendationRepository = recommendationRepository;
        this.saleRepository = saleRepository;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public List<Preference> getClientPreferences(Long clientId) {
        return preferenceRepository.findByClientId(clientId);
    }

    public List<Disease> getClientDiseases(Long clientId) {
        return diseaseRepository.findByClientId(clientId);
    }

    public List<Schedule> getMySchedule(Long trainerId) {
        return scheduleRepository.findByTrainer(trainerId);
    }

    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public void deleteScheduleById(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    public List<RegistrationOfVisit> getVisitsByScheduleId(Long scheduleId) {
        return visitsRepository.findByScheduleId(scheduleId);
    }

    public List<Recommendation> getClientRecommendations(Long clientId) {
        return recommendationRepository.findByClientId(clientId);
    }

    public Recommendation addRecommendation(Recommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }

    public List<RegistrationOfVisit> getAllVisitsByTrainer(Long trainerId) {
        List<Schedule> trainerSchedules = scheduleRepository.findByTrainer(trainerId);
        return trainerSchedules.stream()
                .flatMap(schedule -> visitsRepository.findByScheduleId(schedule.getId()).stream())
                .collect(Collectors.toList());
    }

    public List<RegistrationOfVisit> getVisitsByDateRange(Long trainerId, LocalDate fromDate, LocalDate toDate) {
        List<Schedule> trainerSchedules = scheduleRepository.findByTrainer(trainerId);
        return trainerSchedules.stream()
                .flatMap(schedule -> visitsRepository.findByScheduleId(schedule.getId()).stream())
                .filter(visit -> {
                    // Преобразуем Timestamp в LocalDate для сравнения
                    LocalDate visitDate = visit.getVisitDate().toLocalDateTime().toLocalDate();
                    return (fromDate == null || !visitDate.isBefore(fromDate)) &&
                            (toDate == null || !visitDate.isAfter(toDate));
                })
                .collect(Collectors.toList());
    }

    public void deleteRecommendation(Long recommendationId) {
        recommendationRepository.deleteById(recommendationId);
    }

    public Recommendation updateRecommendation(Recommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }

    public List<Recommendation> getRecommendationsByTrainer(Long trainerId) {
        return recommendationRepository.findByTrainerId(trainerId);
    }

    public List<Client> getClientsByTrainer(Long trainerId) {
        List<Schedule> trainerSchedules = scheduleRepository.findByTrainer(trainerId);
        return trainerSchedules.stream()
                .flatMap(schedule -> visitsRepository.findByScheduleId(schedule.getId()).stream())
                .map(visit -> {
                    Sale sale = saleRepository.findById(visit.getSaleId())
                            .orElse(null);
                    return sale != null ? sale.getClient() : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public Sale getSaleById(Long saleId) {
        if (saleId == null) {
            return null;
        }
        Optional<Sale> saleOptional = saleRepository.findById(saleId);
        return saleOptional.orElse(null);
    }

    public List<VisitWithClientInfo> getVisitsWithClientInfo(Long trainerId) {
        List<Schedule> trainerSchedules = scheduleRepository.findByTrainer(trainerId);
        return trainerSchedules.stream()
                .flatMap(schedule -> visitsRepository.findByScheduleId(schedule.getId()).stream())
                .map(visit -> {
                    Sale sale = saleRepository.findById(visit.getSaleId()).orElse(null);
                    Client client = sale != null ? sale.getClient() : null;
                    return new VisitWithClientInfo(visit, client, sale);
                })
                .filter(info -> info.getClient() != null)
                .collect(Collectors.toList());
    }

    public static class VisitWithClientInfo {
        private final RegistrationOfVisit visit;
        private final Client client;
        private final Sale sale;

        public VisitWithClientInfo(RegistrationOfVisit visit, Client client, Sale sale) {
            this.visit = visit;
            this.client = client;
            this.sale = sale;
        }

        public RegistrationOfVisit getVisit() { return visit; }
        public Client getClient() { return client; }
        public Sale getSale() { return sale; }
    }

    public Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).orElse(null);
    }

    public List<Schedule> getSchedulesByTrainerId(Long trainerId) {
        return scheduleRepository.findByTrainer(trainerId);
    }

    public Client getClientById(Long clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }

}