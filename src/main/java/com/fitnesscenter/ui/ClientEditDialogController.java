package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Client;
import com.fitnesscenter.service.ManagerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ClientEditDialogController implements DialogController, ManagerViewController.SaveableController {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+7|8)[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField patronymicField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField passportField;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private Client client;
    private ManagerService managerService;
    private boolean okClicked = false;
    private Runnable onSave;
    private Runnable onCancel;

    @Autowired
    public ClientEditDialogController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @FXML
    private void initialize() {
        genderComboBox.setItems(FXCollections.observableArrayList("Мужской", "Женский"));
        firstNameField.requestFocus();
    }

    public void setClient(Client client) {
        this.client = client;
        if (client != null) {
            firstNameField.setText(client.getFirstname());
            lastNameField.setText(client.getLastname());
            patronymicField.setText(client.getPatronymic());
            phoneField.setText(client.getPhone());
            emailField.setText(client.getEmail());
            genderComboBox.setValue(client.getGender());
            passportField.setText(client.getPassport());
        }
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            client.setFirstname(firstNameField.getText());
            client.setLastname(lastNameField.getText());
            client.setPatronymic(patronymicField.getText());
            client.setPhone(phoneField.getText());
            client.setEmail(emailField.getText());
            client.setGender(genderComboBox.getValue());
            client.setPassport(passportField.getText());

            try {
                managerService.updateClient(client.getId(), client);
                okClicked = true;
                if (onSave != null) {
                    onSave.run();
                }
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } catch (RuntimeException e) {
                showAlert("Ошибка сохранения", "Не удалось обновить клиента: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage += "Не указано имя!\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage += "Не указана фамилия!\n";
        }
        if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
            errorMessage += "Не указан телефон!\n";
        } else if (!PHONE_PATTERN.matcher(phoneField.getText().trim()).matches()) {
            errorMessage += "Некорректный формат телефона! Ожидается +7XXXXXXXXXX или 8XXXXXXXXXX.\n";
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorMessage += "Не указан email!\n";
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            errorMessage += "Некорректный формат email!\n";
        }
        if (genderComboBox.getValue() == null) {
            errorMessage += "Не указан пол!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Некорректный ввод", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

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
}