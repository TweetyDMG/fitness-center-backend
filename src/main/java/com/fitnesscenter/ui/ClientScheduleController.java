package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Schedule;
import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.service.ClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClientScheduleController {

    @FXML private DatePicker dateFilterPicker;
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> schDateCol;
    @FXML private TableColumn<Schedule, String> schStartTimeCol;
    @FXML private TableColumn<Schedule, String> schEndTimeCol;

    @FXML private TableColumn<Schedule, String> schTrainerCol;
    @FXML private TableColumn<Schedule, Void> schActionCol;

    private ClientService clientService;
    private Long currentClientId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void initializeData(Long clientId, ClientService service) {
        this.currentClientId = clientId;
        this.clientService = service;
        if (dateFilterPicker != null) {
            dateFilterPicker.setValue(LocalDate.now());
        } else {
            loadSchedule();
        }
    }

    @FXML
    public void initialize() {
        if (dateFilterPicker != null) {
            dateFilterPicker.valueProperty().addListener((obs, oldDate, newDate) -> loadSchedule());
        }

        setupTableColumns();
        scheduleTable.setPlaceholder(new Label("Нет доступных занятий по выбранным фильтрам."));
    }

    private void setupTableColumns() {
        schDateCol.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            return new SimpleStringProperty((schedule != null && schedule.getDate() != null) ? schedule.getDate().format(DATE_FORMATTER) : "N/A");
        });

        schStartTimeCol.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            return new SimpleStringProperty((schedule != null && schedule.getStartTime() != null) ? schedule.getStartTime().toLocalTime().format(TIME_FORMATTER) : "N/A");
        });

        schEndTimeCol.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            return new SimpleStringProperty((schedule != null && schedule.getEndTime() != null) ? schedule.getEndTime().toLocalTime().format(TIME_FORMATTER) : "N/A");
        });



        schTrainerCol.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            if (schedule != null && schedule.getTrainer() != null && clientService != null) {
                Trainer trainer = clientService.getTrainerById(schedule.getTrainer());
                if (trainer != null) {
                    String firstName = trainer.getFirstname() != null ? trainer.getFirstname() : "";
                    String lastName = trainer.getLastname() != null ? trainer.getLastname() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    return new SimpleStringProperty(!fullName.isEmpty() ? fullName : "Тренер не назначен");
                } else {
                    return new SimpleStringProperty("Тренер не найден");
                }
            }
            return new SimpleStringProperty("Тренер N/A");
        });

        schActionCol.setCellFactory(param -> new TableCell<Schedule, Void>() {
            private final Button registerButton = new Button("Записаться");

            {
                registerButton.getStyleClass().addAll("btn-primary-small");
                registerButton.setOnAction(event -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Schedule selectedSchedule = getTableView().getItems().get(getIndex());
                        if (selectedSchedule != null) {
                            handleRegisterForVisit(selectedSchedule.getId());
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
                Schedule scheduleItem = getTableView().getItems().get(getIndex());
                if (scheduleItem == null || scheduleItem.getDate() == null || scheduleItem.getStartTime() == null) {
                    setGraphic(null);
                    return;
                }
                LocalDateTime scheduleDateTime = LocalDateTime.of(scheduleItem.getDate(), scheduleItem.getStartTime().toLocalTime());
                if (scheduleDateTime.isAfter(LocalDateTime.now())) {
                    setGraphic(registerButton);
                } else {
                    setGraphic(null);
                }
            }
        });
    }
    public void refreshScheduleData() {
        if (clientService != null) {
            loadSchedule();
        } else {
            if (scheduleTable != null) {
                scheduleTable.setItems(FXCollections.emptyObservableList());
                scheduleTable.setPlaceholder(new Label("Не удалось обновить расписание: сервис недоступен."));
            }
        }
    }


    private void loadSchedule() {
        if (clientService == null) {
            scheduleTable.setItems(FXCollections.emptyObservableList());
            scheduleTable.setPlaceholder(new Label("Сервис недоступен. Невозможно загрузить расписание."));
            return;
        }
        scheduleTable.setPlaceholder(new Label("Нет доступных занятий по выбранным фильтрам."));

        List<Schedule> allSchedules;
        LocalDate selectedDate = (dateFilterPicker != null) ? dateFilterPicker.getValue() : null;

        if (selectedDate != null) {
            allSchedules = clientService.getClientScheduleByDate(selectedDate);
        } else {
            allSchedules = clientService.getClientSchedule().stream()
                    .filter(s -> s.getDate() != null &&
                            LocalDateTime.of(
                                    s.getDate(),
                                    s.getStartTime() != null ? s.getStartTime().toLocalTime() : LocalTime.MIDNIGHT
                            ).isAfter(LocalDateTime.now().minusDays(1)))
                    .collect(Collectors.toList());
        }
        scheduleTable.setItems(FXCollections.observableArrayList(allSchedules));
    }

    private void handleRegisterForVisit(Long scheduleId) {
        if (currentClientId == null || scheduleId == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Невозможно выполнить запись: ID клиента или занятия не определены.");
            return;
        }
        try {
            clientService.registerForVisit(currentClientId, scheduleId);
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Вы успешно записаны на занятие!");

        } catch (Exception e) {
            String errorMessage = (e instanceof com.fitnesscenter.exception.InvalidRequestException) ?
                    e.getMessage() : "Произошла непредвиденная ошибка: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, "Ошибка записи", errorMessage);
            e.printStackTrace();
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