package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Schedule;
import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.service.TrainerService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TrainerScheduleViewController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TrainerScheduleViewController.class.getName());

    @FXML
    private DatePicker datePickerField;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private TableView<Schedule> scheduleTable;
    @FXML
    private TableColumn<Schedule, LocalDate> dateColumn;
    @FXML
    private TableColumn<Schedule, Time> startTimeColumn;
    @FXML
    private TableColumn<Schedule, Time> endTimeColumn;
    @FXML
    private TableColumn<Schedule, Void> actionsColumn;
    @FXML
    private TableColumn<Schedule, String> statusColumn;

    @Autowired
    private TrainerService trainerService;
    @Autowired
    private ApplicationContext springContext;

    private Trainer currentTrainer;
    private ObservableList<Schedule> scheduleData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        startTimeColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            protected void updateItem(Time item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalTime().format(formatter));
                }
            }
        });
        endTimeColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            protected void updateItem(Time item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalTime().format(formatter));
                }
            }
        });

        statusColumn.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            String status;
            if (schedule.getDate().isBefore(today)) {
                status = "Прошедшее";
            } else if (schedule.getDate().isAfter(today)) {
                status = "Будущее";
            } else {
                if (schedule.getEndTime().toLocalTime().isBefore(now)) {
                    status = "Прошедшее";
                } else {
                    status = "Будущее";
                }
            }
            return new ReadOnlyStringWrapper(status);
        });
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // Удаляем все предыдущие стили
                getStyleClass().removeAll("status-past", "status-future");

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.equals("Прошедшее")) {
                        getStyleClass().add("status-past");
                    } else if (item.equals("Будущее")) {
                        getStyleClass().add("status-future");
                    }
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Редактировать");
            private final Button deleteButton = new Button("Удалить");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("btn-small");
                deleteButton.getStyleClass().add("btn-danger");
                pane.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    LocalDate today = LocalDate.now();
                    LocalTime now = LocalTime.now();
                    if (schedule.getDate().isBefore(today) || (schedule.getDate().equals(today) && schedule.getEndTime().toLocalTime().isBefore(now))) {
                        showAlert("Ошибка редактирования", "Нельзя редактировать прошедшее занятие.");
                    } else {
                        handleEditSchedule(schedule);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    handleDeleteSchedule(schedule);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    LocalDate today = LocalDate.now();
                    LocalTime now = LocalTime.now();

                    boolean isFuture = schedule.getDate().isAfter(today) ||
                            (schedule.getDate().equals(today) && schedule.getEndTime().toLocalTime().isAfter(now));
                    editButton.setVisible(isFuture);
                    editButton.setManaged(isFuture);
                    setGraphic(pane);
                }
            }
        });

        scheduleTable.setItems(scheduleData);
        datePickerField.setValue(LocalDate.now());
    }

    public void setTrainer(Trainer trainer) {
        this.currentTrainer = trainer;
        loadScheduleData();
    }

    void loadScheduleData() {
        if (currentTrainer == null) {
            LOGGER.warning("Trainer is null, cannot load schedule data.");
            return;
        }
        Platform.runLater(() -> {
            try {
                List<Schedule> mySchedule = trainerService.getMySchedule(Long.valueOf(currentTrainer.getId()));
                mySchedule.sort((s1, s2) -> {
                    LocalDate today = LocalDate.now();
                    LocalTime now = LocalTime.now();

                    boolean s1IsFuture = s1.getDate().isAfter(today) || (s1.getDate().equals(today) && s1.getEndTime().toLocalTime().isAfter(now));
                    boolean s2IsFuture = s2.getDate().isAfter(today) || (s2.getDate().equals(today) && s2.getEndTime().toLocalTime().isAfter(now));

                    if (s1IsFuture && !s2IsFuture) {
                        return -1;
                    } else if (!s1IsFuture && s2IsFuture) {
                        return 1;
                    } else {
                        int dateComparison = s1.getDate().compareTo(s2.getDate());
                        if (dateComparison != 0) {
                            return dateComparison;
                        } else {
                            return s1.getStartTime().toLocalTime().compareTo(s2.getStartTime().toLocalTime());
                        }
                    }
                });
                scheduleData.setAll(mySchedule);
                if (mySchedule.isEmpty()) {
                    LOGGER.info("No schedule data found for trainer ID: " + currentTrainer.getId());
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading schedule data for trainer " + currentTrainer.getId(), e);
                showAlert("Ошибка загрузки", "Не удалось загрузить расписание.");
            }
        });
    }

    @FXML
    private void handleCreateSchedule() {
        LocalDate date = datePickerField.getValue();
        String startTimeText = startTimeField.getText();
        String endTimeText = endTimeField.getText();

        if (date == null || startTimeText.isEmpty() || endTimeText.isEmpty()) {
            showAlert("Ошибка ввода", "Пожалуйста, заполните все поля (Дата, Время начала, Время окончания).");
            return;
        }

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("HH:mm"));
            endTime = LocalTime.parse(endTimeText, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert("Ошибка формата времени", "Пожалуйста, введите время в формате HH:MM (например, 09:00).");
            return;
        }

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            showAlert("Ошибка времени", "Время начала должно быть раньше времени окончания.");
            return;
        }

        if (currentTrainer == null) {
            showAlert("Ошибка", "Данные тренера не загружены.");
            return;
        }

        Schedule newSchedule = new Schedule();
        newSchedule.setDate(date);
        newSchedule.setStartTime(Time.valueOf(startTime));
        newSchedule.setEndTime(Time.valueOf(endTime));
        newSchedule.setTrainer(Long.valueOf(currentTrainer.getId()));

        try {
            trainerService.createSchedule(newSchedule);
            showAlert("Успех", "Занятие успешно создано!");
            handleClearForm();
            loadScheduleData();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при создании занятия", e);
            showAlert("Ошибка создания", "Не удалось создать занятие: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        datePickerField.setValue(LocalDate.now());
        startTimeField.clear();
        endTimeField.clear();
    }

    @FXML
    private void handleRefresh() {
        loadScheduleData();
    }

    @FXML
    private void handleFilter() {
        LocalDate filterDate = filterDatePicker.getValue();
        if (filterDate == null) {
            showAlert("Ошибка фильтра", "Выберите дату для фильтрации.");
            return;
        }
        if (currentTrainer == null) {
            LOGGER.warning("Trainer is null, cannot filter schedule data.");
            return;
        }

        Platform.runLater(() -> {
            try {
                List<Schedule> allSchedule = trainerService.getMySchedule(Long.valueOf(currentTrainer.getId()));
                List<Schedule> filteredSchedule = allSchedule.stream()
                        .filter(s -> s.getDate().equals(filterDate))
                        .collect(java.util.stream.Collectors.toList());
                scheduleData.setAll(filteredSchedule);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error filtering schedule data", e);
                showAlert("Ошибка фильтрации", "Не удалось отфильтровать расписание.");
            }
        });
    }

    @FXML
    private void handleClearFilter() {
        filterDatePicker.setValue(null);
        loadScheduleData();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleEditSchedule(Schedule schedule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/edit_schedule_dialog.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            EditScheduleDialogController controller = loader.getController();
            controller.setSchedule(schedule);
            controller.setParentController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактировать занятие");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(scheduleTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при загрузке окна редактирования занятия", e);
            showAlert("Ошибка", "Не удалось загрузить окно редактирования занятия.");
        }
    }

    private void handleDeleteSchedule(Schedule schedule) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (schedule.getDate().isBefore(today) || (schedule.getDate().equals(today) && schedule.getEndTime().toLocalTime().isBefore(now))) {
            showAlert("Ошибка удаления", "Нельзя удалить прошедшее занятие.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Вы уверены, что хотите удалить занятие на " +
                schedule.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                " в " + schedule.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    trainerService.deleteScheduleById(schedule.getId());
                    showAlert("Успех", "Занятие успешно удалено!");
                    loadScheduleData();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Ошибка при удалении занятия ID: " + schedule.getId(), e);
                    showAlert("Ошибка удаления", "Не удалось удалить занятие: " + e.getMessage());
                }
            }
        });
    }
}