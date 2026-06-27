package com.fitnesscenter.ui;

import com.fitnesscenter.dto.VisitResponse;
import com.fitnesscenter.entity.*;
import com.fitnesscenter.repository.ScheduleRepository;
import com.fitnesscenter.repository.TrainerRepository;
import com.fitnesscenter.repository.ServiceRepository;
import com.fitnesscenter.repository.SaleRepository;
import com.fitnesscenter.service.ClientService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ClientDashboardController {

    private static final Logger LOGGER = Logger.getLogger(ClientDashboardController.class.getName());

    @FXML private Label subscriptionNameLabel;
    @FXML private Label subscriptionEndDateLabel;
    @FXML private Label visitsRemainingLabel;
    @FXML private TableView<VisitResponse> upcomingVisitsTable;
    @FXML private TableColumn<VisitResponse, String> visitDateColumn;
    @FXML private TableColumn<VisitResponse, String> visitTimeColumn;
    @FXML private TableColumn<VisitResponse, String> visitActivityColumn;
    @FXML private TableColumn<VisitResponse, String> visitTrainerColumn;

    private ClientService clientService;
    private Long currentClientId;
    @Setter
    private TabPane mainTabPane;

    private final ScheduleRepository scheduleRepository;
    private final TrainerRepository trainerRepository;
    private final ServiceRepository serviceRepository;
    private final SaleRepository saleRepository;


    @Autowired
    public ClientDashboardController(ScheduleRepository scheduleRepository,
                                     TrainerRepository trainerRepository,
                                     ServiceRepository serviceRepository,
                                     SaleRepository saleRepository) {
        this.scheduleRepository = scheduleRepository;
        this.trainerRepository = trainerRepository;
        this.serviceRepository = serviceRepository;
        this.saleRepository = saleRepository;
    }

    public void initializeData(Long clientId, ClientService service) {
        this.currentClientId = clientId;
        this.clientService = service;
        if (this.clientService == null || this.currentClientId == null) {
            LOGGER.log(Level.SEVERE, "ClientService или ClientId не инициализированы в ClientDashboardController.");
            subscriptionNameLabel.setText("Ошибка данных");
            subscriptionEndDateLabel.setText("-");
            visitsRemainingLabel.setText("-");
            upcomingVisitsTable.setPlaceholder(new Label("Ошибка загрузки данных."));
            return;
        }
        refreshDashboardData();
    }

    @FXML
    public void initialize() {
        visitDateColumn.setCellValueFactory(cellData ->
                cellData.getValue().getVisitDate() != null ?
                        new SimpleStringProperty(cellData.getValue().getVisitDate().format(DateTimeFormatter.ISO_DATE)) :
                        new SimpleStringProperty(""));

        visitTimeColumn.setCellValueFactory(cellData ->
                cellData.getValue().getVisitTime() != null ?
                        new SimpleStringProperty(cellData.getValue().getVisitTime().format(DateTimeFormatter.ofPattern("HH:mm"))) :
                        new SimpleStringProperty(""));

        visitActivityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getServiceName()));
        visitTrainerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrainerName()));
    }

    public void refreshDashboardData() {
        LOGGER.info("Обновление данных главной панели.");
        loadActiveSubscription();
        loadUpcomingVisits();
    }

    private void loadActiveSubscription() {
        if (clientService == null || currentClientId == null) return;

        List<Sale> sales = clientService.getClientSubscriptions(currentClientId);
        Optional<Sale> activeSaleOpt = sales.stream()
                .filter(sale -> sale.getSubscription() != null &&
                        !sale.getStartDate().isAfter(LocalDate.now()) &&
                        !sale.getEndDate().isBefore(LocalDate.now()))
                .findFirst();

        if (activeSaleOpt.isPresent()) {
            Sale activeSale = activeSaleOpt.get();
            Subscription sub = activeSale.getSubscription();
            subscriptionNameLabel.setText(sub.getName());
            subscriptionEndDateLabel.setText(activeSale.getEndDate().format(DateTimeFormatter.ISO_DATE));

            int totalVisits = sub.getNumberOfVisits() != null ? sub.getNumberOfVisits() : 0;

            List<RegistrationOfVisit> allClientVisits = clientService.getClientVisits(currentClientId);
            long usedVisits = allClientVisits.stream()
                    .filter(visit -> visit.getSaleId() != null && visit.getSaleId().equals(activeSale.getId()))
                    .count();

            visitsRemainingLabel.setText(String.valueOf(totalVisits - usedVisits));
        } else {
            subscriptionNameLabel.setText("Нет активного абонемента");
            subscriptionEndDateLabel.setText("-");
            visitsRemainingLabel.setText("-");
        }
    }

    private void loadUpcomingVisits() {
        if (clientService == null || currentClientId == null) {
            upcomingVisitsTable.setPlaceholder(new Label("Не удалось загрузить посещения."));
            return;
        }

        List<RegistrationOfVisit> clientVisits = clientService.getClientVisits(currentClientId);
        LocalDateTime now = LocalDateTime.now();

        List<VisitResponse> upcomingVisitDTOs = clientVisits.stream()
                .map(this::convertToVisitResponse)
                .filter(dto -> dto.getVisitDate() != null && dto.getVisitTime() != null &&
                        LocalDateTime.of(dto.getVisitDate(), dto.getVisitTime()).isAfter(now))
                .sorted(Comparator.comparing(VisitResponse::getVisitDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(VisitResponse::getVisitTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .collect(Collectors.toList());

        if (upcomingVisitDTOs.isEmpty()) {
            upcomingVisitsTable.setPlaceholder(new Label("Нет предстоящих посещений."));
            upcomingVisitsTable.setItems(FXCollections.emptyObservableList());
        } else {
            upcomingVisitsTable.setItems(FXCollections.observableArrayList(upcomingVisitDTOs));
        }
    }

    private VisitResponse convertToVisitResponse(RegistrationOfVisit regVisit) {
        VisitResponse dto = new VisitResponse();
        dto.setId(regVisit.getId());

        LocalDateTime visitDateTime = regVisit.getVisitDate().toLocalDateTime();
        dto.setVisitDate(visitDateTime.toLocalDate());
        dto.setVisitTime(visitDateTime.toLocalTime());
        dto.setStatus("Запланировано");
        LocalDateTime effectiveVisitDateTime = null;
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(regVisit.getScheduleId());
        if (scheduleOpt.isPresent()) {
            Schedule schedule = scheduleOpt.get();
            if(schedule.getDate() != null && schedule.getStartTime() != null){
                effectiveVisitDateTime = LocalDateTime.of(schedule.getDate(), schedule.getStartTime().toLocalTime());
            }
            Optional<Trainer> trainerOpt = trainerRepository.findById(schedule.getTrainer());
            trainerOpt.ifPresent(trainer -> dto.setTrainerName(trainer.getFirstname() + " " + trainer.getLastname()));
            Optional<Sale> saleOpt = Optional.ofNullable(regVisit.getSaleId()).flatMap(saleRepository::findById);
            if (saleOpt.isPresent()) {
                Sale sale = saleOpt.get();
                if (sale.getSubscription() != null) {
                    Subscription subscription = sale.getSubscription();
                    if (subscription.getFitnessService() != null) {
                        Optional<FitnessService> serviceOpt = serviceRepository.findById(subscription.getFitnessService().getId());
                        serviceOpt.ifPresent(service -> dto.setServiceName(service.getName()));
                    } else if(subscription.getName() != null) {
                        dto.setServiceName(subscription.getName());
                    }
                }
            }
        }
        if (effectiveVisitDateTime == null && regVisit.getVisitDate() != null) {
            effectiveVisitDateTime = regVisit.getVisitDate().toLocalDateTime();
        }
        if (effectiveVisitDateTime != null) {
            dto.setVisitDate(effectiveVisitDateTime.toLocalDate());
            dto.setVisitTime(effectiveVisitDateTime.toLocalTime());
        } else {
            dto.setVisitDate(null);
            dto.setVisitTime(null);
        }
        if (dto.getServiceName() == null) {
            dto.setServiceName("Тренировка");
        }
        if (dto.getTrainerName() == null) {
            dto.setTrainerName("Н/Д");
        }

        return dto;
    }

    @FXML
    private void handleViewSchedule() {
        if (mainTabPane != null) {
            if (mainTabPane.getTabs().size() > 4) {
                mainTabPane.getSelectionModel().select(4);
            } else {
                LOGGER.log(Level.WARNING, "Не удалось найти вкладку расписания для переключения.");
            }
        } else {
            LOGGER.log(Level.WARNING, "mainTabPane не установлен в ClientDashboardController. Невозможно переключить вкладку.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Навигация");
            alert.setHeaderText(null);
            alert.setContentText("Не удалось открыть расписание. Обратитесь к администратору.");
            alert.showAndWait();
        }
    }
}