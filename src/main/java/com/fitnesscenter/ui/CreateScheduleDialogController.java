package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Schedule;
import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.service.TrainerService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class CreateScheduleDialogController {

    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> startHourSpinner;
    @FXML
    private Spinner<Integer> startMinuteSpinner;
    @FXML
    private Spinner<Integer> endHourSpinner;
    @FXML
    private Spinner<Integer> endMinuteSpinner;

    @Autowired
    private TrainerService trainerService;

    private Trainer currentTrainer;
    @Setter
    private TrainerDashboardViewController trainerDashboardViewController;

    @FXML
    public void initialize() {
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        datePicker.setValue(LocalDate.now());
    }

    public void setTrainer(Trainer trainer) {
        this.currentTrainer = trainer;
    }

    @FXML
    private void handleCreateSchedule() {
        LocalDate date = datePicker.getValue();
        LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());

        if (date == null || startTime == null || endTime == null) {
            showAlert("Ошибка", "Пожалуйста, заполните все поля.");
            return;
        }

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            showAlert("Ошибка", "Время начала должно быть раньше времени окончания.");
            return;
        }

        if (currentTrainer == null) {
            showAlert("Ошибка", "Данные тренера не загружены.");
            return;
        }

        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setStartTime(Time.valueOf(startTime));
        schedule.setEndTime(Time.valueOf(endTime));
        schedule.setTrainer(Long.valueOf(currentTrainer.getId()));

        try {
            trainerService.createSchedule(schedule);
            showAlert("Успех", "Занятие успешно создано!");
            if (trainerDashboardViewController != null) {
                trainerDashboardViewController.loadDashboardData();
            }
            closeDialog();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось создать занятие: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}