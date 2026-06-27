package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Client;
import com.fitnesscenter.service.ClientService;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

@Component
public class ClientProfileController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField patronymicField;
    @FXML private TextField phoneField;
    @FXML private TextField genderField;
    @FXML private TextField emailField;
    @FXML private TextField passportField;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ClientService clientService;
    private Long currentClientId;
    private Client currentClient;

    public void initializeData(Long clientId, ClientService service) {
        this.currentClientId = clientId;
        this.clientService = service;
        loadProfileData();
    }

    @FXML
    public void initialize() {
        setFieldsEditable(false);
    }

    private void loadProfileData() {
        if (clientService == null || currentClientId == null) return;
        try {
            currentClient = clientService.getClientProfile(currentClientId);
            firstNameField.setText(currentClient.getFirstname());
            lastNameField.setText(currentClient.getLastname());
            patronymicField.setText(currentClient.getPatronymic());
            phoneField.setText(currentClient.getPhone());
            genderField.setText(currentClient.getGender());
            emailField.setText(currentClient.getEmail());
            passportField.setText(currentClient.getPassport() != null ? currentClient.getPassport() : "");
        } catch (Exception e) {
            showAlert("Ошибка загрузки", "Не удалось загрузить профиль клиента.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditProfile() {
        setFieldsEditable(true);
    }

    @FXML
    private void handleSaveProfile() {
        if (!validateProfileData()) {
            return;
        }
        currentClient.setFirstname(firstNameField.getText());
        currentClient.setLastname(lastNameField.getText());
        currentClient.setPatronymic(patronymicField.getText());
        currentClient.setPhone(phoneField.getText());
        currentClient.setGender(genderField.getText());
        currentClient.setEmail(emailField.getText());
        currentClient.setPassport(passportField.getText());

        try {
            clientService.updateClientProfile(currentClient);
            showAlert("Успех", "Данные профиля обновлены.");
            loadProfileData();
        } catch (Exception e) {
            showAlert("Ошибка сохранения", "Не удалось обновить профиль.");
            e.printStackTrace();
        }
        setFieldsEditable(false);
    }

    @FXML
    private void handleCancelEdit() {
        loadProfileData();
        setFieldsEditable(false);
    }

    private void setFieldsEditable(boolean editable) {
        firstNameField.setEditable(editable);
        lastNameField.setEditable(editable);
        patronymicField.setEditable(editable);
        phoneField.setEditable(editable);
        genderField.setEditable(editable);
        emailField.setEditable(editable);
        passportField.setEditable(editable);

        if (editable) {
            fadeOut(editButton, () -> editButton.setVisible(false));
            fadeIn(saveButton, () -> saveButton.setVisible(true));
            fadeIn(cancelButton, () -> cancelButton.setVisible(true));
        } else {
            fadeIn(editButton, () -> editButton.setVisible(true));
            fadeOut(saveButton, () -> saveButton.setVisible(false));
            fadeOut(cancelButton, () -> cancelButton.setVisible(false));
        }
    }

    private void fadeIn(Button button, Runnable onFinishedAction) {
        button.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), button);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void fadeOut(Button button, Runnable onFinishedAction) {
        if (!button.isVisible()) return;

        FadeTransition ft = new FadeTransition(Duration.millis(300), button);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        if (onFinishedAction != null) {
            ft.setOnFinished(e -> onFinishedAction.run());
        } else {
            ft.setOnFinished(e -> button.setVisible(false));
        }
        ft.play();
    }

    private boolean validateProfileData() {
        boolean isValid = true;
        if (firstNameField.getText().isEmpty()) {
            firstNameField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            firstNameField.getStyleClass().remove("error-field");
        }
        if (lastNameField.getText().isEmpty()) {
            lastNameField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            lastNameField.getStyleClass().remove("error-field");
        }
        if (!phoneField.getText().matches("\\+?[0-9]{10,15}")) {
            phoneField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            phoneField.getStyleClass().remove("error-field");
        }
        if (!emailField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            emailField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            emailField.getStyleClass().remove("error-field");
        }
        if (!isValid) {
            showAlert("Ошибка валидации", "Пожалуйста, исправьте ошибки в полях.");
        }
        return isValid;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}