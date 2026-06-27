package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Schedule;
import com.fitnesscenter.service.TrainerService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class EditScheduleDialogController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(EditScheduleDialogController.class.getName());

    @FXML
    private DatePicker datePickerField;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;

    @Autowired
    private TrainerService trainerService;

    private Stage dialogStage;
    private Schedule scheduleToEdit;
    @Getter
    private boolean saveClicked = false;

    private TrainerScheduleViewController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSchedule(Schedule schedule) {
        this.scheduleToEdit = schedule;
        if (scheduleToEdit != null) {
            datePickerField.setValue(scheduleToEdit.getDate());
            startTimeField.setText(scheduleToEdit.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            endTimeField.setText(scheduleToEdit.getEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

    public void setParentController(TrainerScheduleViewController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleSave() {
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

        if (scheduleToEdit == null) {
            showAlert("Ошибка", "Нет занятия для редактирования.");
            return;
        }

        scheduleToEdit.setDate(date);
        scheduleToEdit.setStartTime(Time.valueOf(startTime));
        scheduleToEdit.setEndTime(Time.valueOf(endTime));

        try {
            trainerService.createSchedule(scheduleToEdit);
            saveClicked = true;
            showAlert("Успех", "Занятие успешно обновлено!");
            if (parentController != null) {
                parentController.loadScheduleData();
            }
            dialogStage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при обновлении занятия ID: " + scheduleToEdit.getId(), e);
            showAlert("Ошибка обновления", "Не удалось обновить занятие: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}