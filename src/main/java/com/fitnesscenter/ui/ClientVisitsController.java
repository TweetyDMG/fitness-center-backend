package com.fitnesscenter.ui;

import com.fitnesscenter.dto.VisitResponse;
import com.fitnesscenter.entity.RegistrationOfVisit;
import com.fitnesscenter.entity.Schedule;
import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.service.ClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ClientVisitsController {

    private static final Logger LOGGER = Logger.getLogger(ClientVisitsController.class.getName());

    @FXML private TableView<VisitResponse> visitsTable;
    @FXML private TableColumn<VisitResponse, String> visitDateCol;
    @FXML private TableColumn<VisitResponse, String> visitTimeCol;
    @FXML private TableColumn<VisitResponse, String> visitActivityCol;
    @FXML private TableColumn<VisitResponse, String> visitStatusCol;
    @FXML private TableColumn<VisitResponse, Void> visitActionCol;

    private ClientService clientService;
    private Long currentClientId;
    @Getter
    private boolean initialized = false;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void initializeData(Long clientId, ClientService service) {
        this.currentClientId = clientId;
        this.clientService = service;
        this.initialized = true;
        loadVisits();
        LOGGER.info("ClientVisitsController данные инициализированы для клиента ID: " + clientId);
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupRowStyling();
        visitsTable.setPlaceholder(new Label("У вас нет зарегистрированных посещений."));
        LOGGER.info("ClientVisitsController FXML инициализирован.");
    }

    private void setupTableColumns() {
        visitDateCol.setCellValueFactory(cellData -> {
            VisitResponse visit = cellData.getValue();
            LocalDate date = (visit != null) ? visit.getVisitDate() : null;
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });
        visitTimeCol.setCellValueFactory(cellData -> {
            VisitResponse visit = cellData.getValue();
            LocalTime time = (visit != null) ? visit.getVisitTime() : null;
            return new SimpleStringProperty(time != null ? time.format(TIME_FORMATTER) : "");
        });
        visitActivityCol.setCellValueFactory(cellData -> {
            VisitResponse visit = cellData.getValue();
            if (visit == null) return new SimpleStringProperty("");

            String serviceName = visit.getServiceName();
            String trainerName = visit.getTrainerName();
            String activityText = (serviceName != null && !serviceName.isEmpty() ? serviceName : "Общее занятие");
            if (trainerName != null && !trainerName.isEmpty() && !trainerName.equals("Тренер не указан") && !trainerName.equals("Тренер не найден")) {
                activityText += " (" + trainerName + ")";
            }
            return new SimpleStringProperty(activityText);
        });

        visitStatusCol.setCellValueFactory(cellData -> {
            VisitResponse visit = cellData.getValue();
            String status = (visit != null) ? visit.getStatus() : "";
            return new SimpleStringProperty(status);
        });
        visitStatusCol.setCellFactory(column -> new TableCell<VisitResponse, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-upcoming", "status-past", "status-cancelled");

                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if ("Предстоит".equals(item) || "Запланировано".equals(item)) {
                        getStyleClass().add("status-upcoming");
                    } else if ("Прошло".equals(item)) {
                        getStyleClass().add("status-past");
                    } else if ("Отменено".equals(item)){
                        getStyleClass().add("status-cancelled");
                    }
                }
            }
        });

        visitActionCol.setCellFactory(param -> new TableCell<VisitResponse, Void>() {
            private final Button cancelButton = new Button();
            private final Tooltip cancelTooltip = new Tooltip("Отменить посещение");

            {
                try {
                    Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/cancel_icon.png")));
                    ImageView iconView = new ImageView(icon);
                    iconView.setFitHeight(16);
                    iconView.setFitWidth(16);
                    cancelButton.setGraphic(iconView);
                } catch (Exception e) {
                    cancelButton.setText("X");
                    LOGGER.log(Level.SEVERE, "Ошибка загрузки иконки отмены: " + e.getMessage(), e);
                }

                cancelButton.getStyleClass().addAll("action-button", "cancel-action-button");
                Tooltip.install(cancelButton, cancelTooltip);

                cancelButton.setOnAction(event -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        VisitResponse visit = getTableView().getItems().get(getIndex());
                        if (visit != null && visit.getId() != null) {
                            handleCancelVisit(visit.getId());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                VisitResponse visit = getTableView().getItems().get(getIndex());
                if (visit != null && ("Предстоит".equals(visit.getStatus()) || "Запланировано".equals(visit.getStatus()))) {
                    setGraphic(cancelButton);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void setupRowStyling() {
        visitsTable.setRowFactory(tv -> new TableRow<VisitResponse>() {
            @Override
            protected void updateItem(VisitResponse item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("past-visit-row", "upcoming-visit-row", "cancelled-visit-row");

                if (item != null && !empty) {
                    if ("Прошло".equals(item.getStatus())) {
                        getStyleClass().add("past-visit-row");
                    } else if ("Предстоит".equals(item.getStatus()) || "Запланировано".equals(item.getStatus())) {
                        getStyleClass().add("upcoming-visit-row");
                    } else if ("Отменено".equals(item.getStatus())) {
                        getStyleClass().add("cancelled-visit-row");
                    }
                } else {
                    setStyle("");
                }
            }
        });
    }

    public void refreshVisitsData() {
        LOGGER.info("Обновление данных посещений.");
        loadVisits();
    }

    private void loadVisits() {
        if (clientService == null || currentClientId == null) {
            LOGGER.log(Level.WARNING, "ClientService или currentClientId не установлены. Невозможно загрузить посещения.");
            visitsTable.setItems(FXCollections.emptyObservableList());
            return;
        }

        try {
            List<RegistrationOfVisit> clientVisits = clientService.getClientVisits(currentClientId);
            List<VisitResponse> visitResponses = clientVisits.stream().map(visit -> {
                LocalDateTime visitDateTimeFromDb = visit.getVisitDate().toLocalDateTime();

                String actualServiceName = "Запланированное занятие";
                String actualTrainerName = "Тренер не указан";
                LocalDate actualVisitDate = visitDateTimeFromDb.toLocalDate();
                LocalTime actualVisitTime = visitDateTimeFromDb.toLocalTime();
                String status;

                if (visit.getScheduleId() != null) {
                    Schedule schedule = clientService.getScheduleById(visit.getScheduleId());
                    if (schedule != null) {
                        actualVisitDate = schedule.getDate();
                        actualVisitTime = schedule.getStartTime().toLocalTime();
                        Long trainerId = schedule.getTrainer();
                        if (trainerId != null) {
                            Trainer trainer = clientService.getTrainerById(trainerId);
                            if (trainer != null) {
                                String firstName = trainer.getFirstname();
                                String lastName = trainer.getLastname();
                                actualTrainerName = new StringBuilder()
                                        .append(firstName != null ? firstName : "")
                                        .append(firstName != null && lastName != null ? " " : "")
                                        .append(lastName != null ? lastName : "")
                                        .toString().trim();
                                if (actualTrainerName.isEmpty()) {
                                    actualTrainerName = "Тренер не найден";
                                }
                            } else {
                                actualTrainerName = "Тренер не найден";
                            }
                        }
                    }
                }

                LocalDateTime actualClassDateTime = LocalDateTime.of(actualVisitDate, actualVisitTime);
                status = actualClassDateTime.isBefore(LocalDateTime.now()) ? "Прошло" : "Предстоит";

                VisitResponse response = new VisitResponse(
                        visit.getId(),
                        actualVisitDate,
                        actualVisitTime,
                        actualTrainerName,
                        actualServiceName,
                        status
                );
                return response;
            }).collect(Collectors.toList());

            visitsTable.setItems(FXCollections.observableArrayList(visitResponses));
            LOGGER.info("Загружено " + visitResponses.size() + " посещений для клиента ID: " + currentClientId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при загрузке посещений для клиента ID: " + currentClientId, e);
            visitsTable.setItems(FXCollections.emptyObservableList());
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки", "Не удалось загрузить данные о посещениях.");
        }
    }

    private void handleCancelVisit(Long visitId) {
        try {
            clientService.cancelVisit(currentClientId, visitId);
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Посещение успешно отменено.");
            loadVisits(); // Обновляем данные после отмены
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при отмене посещения ID: " + visitId + " для клиента ID: " + currentClientId, e);
            showAlert(Alert.AlertType.ERROR, "Ошибка отмены", "Не удалось отменить посещение: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}