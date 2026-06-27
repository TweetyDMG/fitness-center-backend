package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.entity.Schedule;
import com.fitnesscenter.service.ManagerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class ScheduleAddDialogController implements DialogController, ManagerViewController.SaveableController {

    @FXML private Label trainerLabel;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private Button addButton;
    @FXML private Button cancelButton;

    @Setter
    private Stage dialogStage;
    private Trainer trainer;
    private ManagerService managerService;
    @Getter
    private boolean scheduleAdded = false;

    private boolean okClicked = false;
    private Runnable onSave;
    private Runnable onCancel;

    @Override
    public boolean isOkClicked() {
        return okClicked;
    }

    @Override
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @Override
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void initData(Trainer trainer, ManagerService managerService) {
        this.trainer = trainer;
        this.managerService = managerService;
        trainerLabel.setText("Добавление занятия для тренера: " + getTrainerFullName());
    }

    @FXML
    private void handleAdd() {
        if (validateInput()) {
            try {
                LocalDate date = datePicker.getValue();
                LocalTime startTime = LocalTime.parse(startTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(endTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));

                Schedule newSchedule = new Schedule();
                newSchedule.setDate(Date.valueOf(date).toLocalDate());
                newSchedule.setStartTime(Time.valueOf(startTime));
                newSchedule.setEndTime(Time.valueOf(endTime));
                newSchedule.setTrainer(Long.valueOf(trainer.getId()));

                managerService.addSchedule(newSchedule);
                scheduleAdded = true;
                okClicked = true;

                if (dialogStage != null) {
                    dialogStage.close();
                }

                if (onSave != null) {
                    onSave.run();
                }
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка формата", "Время должно быть в формате HH:mm.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить занятие: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
        if (onCancel != null) {
            onCancel.run();
        }
    }
    private boolean validateInput() {
        if (datePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Неверный ввод", "Пожалуйста, выберите дату.");
            return false;
        }

        String startTime = startTimeField.getText();
        String endTime = endTimeField.getText();
        if (startTime.isEmpty() || endTime.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Неверный ввод", "Укажите время начала и окончания.");
            return false;
        }

        try {
            LocalTime start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
            if (end.isBefore(start) || end.equals(start)) {
                showAlert(Alert.AlertType.WARNING, "Неверный ввод", "Время окончания должно быть позже времени начала.");
                return false;
            }
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.WARNING, "Неверный ввод", "Неверный формат времени. Используйте HH:mm (например, 14:30).");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }

    private String getTrainerFullName() {
        return trainer.getLastname() + " " + trainer.getFirstname() +
                (trainer.getPatronymic() != null ? " " + trainer.getPatronymic() : "");
    }
}