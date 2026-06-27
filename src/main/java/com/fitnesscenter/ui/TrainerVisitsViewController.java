package com.fitnesscenter.ui;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.service.TrainerService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class TrainerVisitsViewController implements Initializable {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<Schedule> scheduleComboBox;
    @FXML private Button filterBtn;
    @FXML private Button clearFilterBtn;
    @FXML private Button refreshBtn;

    @FXML private Label totalVisitsLabel;
    @FXML private Label monthVisitsLabel;
    @FXML private Label uniqueClientsLabel;
    @FXML private Label averageAttendanceLabel;

    @FXML private TableView<RegistrationOfVisit> visitsTable;
    @FXML private TableColumn<RegistrationOfVisit, String> clientNameColumn;
    @FXML private TableColumn<RegistrationOfVisit, String> scheduleInfoColumn;
    @FXML private TableColumn<RegistrationOfVisit, String> visitDateColumn;
    @FXML private TableColumn<RegistrationOfVisit, String> visitTimeColumn;
    @FXML private TableColumn<RegistrationOfVisit, String> statusColumn;

    @FXML private VBox visitDetailsSection;
    @FXML private Label detailClientNameLabel;
    @FXML private Label detailClientContactLabel;
    //@FXML private Label detailScheduleInfoLabel;
    @FXML private Label detailScheduleTimeLabel;
    @FXML private Label detailVisitDateLabel;
    @FXML private Label detailVisitStatusLabel;

    @FXML private VBox editNotesDialog;

    private final TrainerService trainerService;
    private ObservableList<RegistrationOfVisit> visitsList;
    private ObservableList<Schedule> scheduleList;
    private Long currentTrainerId;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public TrainerVisitsViewController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    public void setTrainer(Trainer trainer) {
        if (trainer != null) {
            this.currentTrainerId = Long.valueOf(trainer.getId());
            loadSchedulesForComboBox();
            loadData();
        } else {
            this.currentTrainerId = null;
            visitsList.clear();
            scheduleList.clear();
            updateStatistics(new ArrayList<>());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTable();
        initializeComboBox();
        setupEventHandlers();
        hideVisitDetails();
        editNotesDialog.setVisible(false);
        editNotesDialog.setManaged(false);
    }

    private void initializeTable() {

        clientNameColumn.setCellValueFactory(cellData -> {
            try {
                Sale sale = trainerService.getSaleById(cellData.getValue().getSaleId());
                if (sale != null && sale.getClient() != null) {
                    return new SimpleStringProperty(sale.getClient().getFirstname() + " " + sale.getClient().getLastname());
                }
            } catch (Exception e) {
            }
            return new SimpleStringProperty("N/A");
        });

        scheduleInfoColumn.setCellValueFactory(cellData -> {
            try {
                Schedule schedule = trainerService.getScheduleById(cellData.getValue().getScheduleId());
                if (schedule != null) {
                    return new SimpleStringProperty("Занятие от " + schedule.getDate().format(dateFormatter));
                }
            } catch (Exception e) {
            }
            return new SimpleStringProperty("N/A");
        });

        visitDateColumn.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getVisitDate();
            return new SimpleStringProperty(ts != null ? ts.toLocalDateTime().format(dateFormatter) : "N/A");
        });

        visitTimeColumn.setCellValueFactory(cellData -> {
            try {
                Schedule schedule = trainerService.getScheduleById(cellData.getValue().getScheduleId());
                if (schedule != null && schedule.getStartTime() != null) {
                    return new SimpleStringProperty(schedule.getStartTime().toLocalTime().format(timeFormatter));
                }
            } catch (Exception e) {
            }
            return new SimpleStringProperty("N/A");
        });

        statusColumn.setCellValueFactory(cellData -> {
            boolean isAttended = isVisitAttended(cellData.getValue());
            return new SimpleStringProperty(getVisitStatusText(isAttended));
        });

        visitsList = FXCollections.observableArrayList();
        visitsTable.setItems(visitsList);

        visitsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showVisitDetails(newSelection);
                    } else {
                        hideVisitDetails();
                    }
                });
    }

    private void initializeComboBox() {
        scheduleList = FXCollections.observableArrayList();
        scheduleComboBox.setItems(scheduleList);

        scheduleComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Занятие: " + item.getDate().format(dateFormatter) + " " +
                            item.getStartTime().toLocalTime().format(timeFormatter));
                }
            }
        });

        scheduleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Выберите занятие");
                } else {
                    setText("Занятие: " + item.getDate().format(dateFormatter) + " " +
                            item.getStartTime().toLocalTime().format(timeFormatter));
                }
            }
        });
    }

    private void setupEventHandlers() {
        filterBtn.setOnAction(e -> handleFilter());
        clearFilterBtn.setOnAction(e -> handleClearFilter());
        refreshBtn.setOnAction(e -> handleRefresh());
    }

    private void loadSchedulesForComboBox() {
        if (currentTrainerId == null) return;
        try {
            List<Schedule> schedules = trainerService.getSchedulesByTrainerId(currentTrainerId);
            scheduleList.setAll(schedules);
        } catch (Exception e) {
            showAlert("Ошибка ComboBox", "Не удалось загрузить расписания: " + e.getMessage());
            scheduleList.clear();
        }
    }

    private void loadData() {
        if (currentTrainerId == null) {
            visitsList.clear();
            updateStatistics(new ArrayList<>());
            return;
        }
        try {
            List<Schedule> trainerSchedules = trainerService.getSchedulesByTrainerId(currentTrainerId);

            List<RegistrationOfVisit> allVisitsForTrainer = new ArrayList<>();
            for (Schedule s : trainerSchedules) {
                allVisitsForTrainer.addAll(trainerService.getVisitsByScheduleId(s.getId()));
            }

            visitsList.setAll(allVisitsForTrainer);
            updateStatistics(allVisitsForTrainer);

        } catch (Exception e) {
            showAlert("Ошибка загрузки данных", "Не удалось загрузить посещения: " + e.getMessage());
            visitsList.clear();
            updateStatistics(new ArrayList<>());
        }
    }

    @FXML
    private void handleFilter() {
        if (currentTrainerId == null) return;
        try {
            List<Schedule> trainerSchedules = trainerService.getSchedulesByTrainerId(currentTrainerId);
            List<RegistrationOfVisit> allVisitsForTrainer = new ArrayList<>();
            for (Schedule s : trainerSchedules) {
                allVisitsForTrainer.addAll(trainerService.getVisitsByScheduleId(s.getId()));
            }

            List<RegistrationOfVisit> filteredVisits = allVisitsForTrainer.stream()
                    .filter(visit -> {
                        boolean dateFilter = true;
                        boolean scheduleFilter = true;
                        LocalDate visitLocalDate = visit.getVisitDate().toLocalDateTime().toLocalDate();

                        if (fromDatePicker.getValue() != null) {
                            dateFilter = !visitLocalDate.isBefore(fromDatePicker.getValue());
                        }
                        if (toDatePicker.getValue() != null && dateFilter) {
                            dateFilter = !visitLocalDate.isAfter(toDatePicker.getValue());
                        }

                        Schedule selectedScheduleFromCombo = scheduleComboBox.getValue();
                        if (selectedScheduleFromCombo != null) {
                            scheduleFilter = visit.getScheduleId().equals(selectedScheduleFromCombo.getId());
                        }
                        return dateFilter && scheduleFilter;
                    })
                    .collect(Collectors.toList());

            visitsList.setAll(filteredVisits);
            updateStatistics(filteredVisits);

        } catch (Exception e) {
            showAlert("Ошибка фильтрации", "Не удалось применить фильтр: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        scheduleComboBox.setValue(null);
        if (currentTrainerId != null) {
            loadData();
        } else {
            visitsList.clear();
            updateStatistics(new ArrayList<>());
        }
    }

    @FXML
    private void handleRefresh() {
        if (currentTrainerId != null) {
            loadSchedulesForComboBox();
            loadData();
        } else {
            visitsList.clear();
            scheduleList.clear();
            updateStatistics(new ArrayList<>());
        }
        hideVisitDetails();
    }

    private void updateStatistics(List<RegistrationOfVisit> visits) {
        int totalVisits = visits.size();
        totalVisitsLabel.setText(String.valueOf(totalVisits));

        LocalDate now = LocalDate.now();
        int monthVisits = (int) visits.stream()
                .filter(visit -> {
                    LocalDateTime visitDateTime = visit.getVisitDate().toLocalDateTime();
                    return visitDateTime.getMonth() == now.getMonth() &&
                            visitDateTime.getYear() == now.getYear();
                })
                .count();
        monthVisitsLabel.setText(String.valueOf(monthVisits));

        long uniqueClients = visits.stream()
                .map(visit -> {
                    try {
                        Sale sale = trainerService.getSaleById(visit.getSaleId());
                        return (sale != null && sale.getClient() != null) ? sale.getClient().getId() : null;
                    } catch (Exception e) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
        uniqueClientsLabel.setText(String.valueOf(uniqueClients));

        if (totalVisits > 0) {
            long attendedVisits = visits.stream()
                    .filter(this::isVisitAttended)
                    .count();
            double averageAttendance = (double) attendedVisits / totalVisits * 100;
            averageAttendanceLabel.setText(String.format("%.1f%%", averageAttendance));
        } else {
            averageAttendanceLabel.setText("0%");
        }
    }

    private boolean isVisitAttended(RegistrationOfVisit visit) {
        if (visit == null || visit.getVisitDate() == null) {
            return false;
        }
        try {
            Schedule schedule = trainerService.getScheduleById(visit.getScheduleId());
            if (schedule != null && schedule.getDate() != null) {
                LocalDate visitLocalDate = visit.getVisitDate().toLocalDateTime().toLocalDate();
                LocalDate scheduleLocalDate = schedule.getDate();
                return visitLocalDate.isEqual(scheduleLocalDate);
            }
        } catch (Exception e) {
            System.err.println("Error determining visit attendance: " + e.getMessage());
        }
        return false;
    }

    private void showVisitDetails(RegistrationOfVisit visit) {
        if (visit == null) {
            hideVisitDetails();
            return;
        }
        try {
            Sale sale = trainerService.getSaleById(visit.getSaleId());
            Schedule schedule = trainerService.getScheduleById(visit.getScheduleId());

            if (sale != null && sale.getClient() != null) {
                Client client = sale.getClient();
                detailClientNameLabel.setText("Клиент: " + client.getFirstname() + " " + client.getLastname());
                detailClientContactLabel.setText("Контакт: " + client.getPhone());
            } else {
                detailClientNameLabel.setText("Клиент: N/A");
                detailClientContactLabel.setText("Контакт: N/A");
            }

            if (schedule != null) {
                detailScheduleTimeLabel.setText("Время: " +
                        (schedule.getStartTime() != null ? schedule.getStartTime().toLocalTime().format(timeFormatter) : "N/A") + " - " +
                        (schedule.getEndTime() != null ? schedule.getEndTime().toLocalTime().format(timeFormatter) : "N/A"));
            } else {
                detailScheduleTimeLabel.setText("Время: N/A");
            }

            detailVisitDateLabel.setText("Дата посещения: " + visit.getVisitDate().toLocalDateTime().format(dateFormatter));
            detailVisitStatusLabel.setText("Статус: " + getVisitStatusText(isVisitAttended(visit)));

            visitDetailsSection.setVisible(true);
            visitDetailsSection.setManaged(true);
        } catch (Exception e) {
            showAlert("Ошибка деталей", "Не удалось загрузить детали посещения: " + e.getMessage());
            hideVisitDetails();
        }
    }

    private void hideVisitDetails() {
        visitDetailsSection.setVisible(false);
        visitDetailsSection.setManaged(false);
    }

    private String getVisitStatusText(boolean attended) {
        return attended ? "Посещено" : "Не посещено";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}