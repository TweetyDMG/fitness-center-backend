package com.fitnesscenter.ui;

import com.fitnesscenter.service.AdminService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component
public class RegisterController {

    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private AdminService adminService;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField patronymicField;
    @FXML private TextField phoneField;
    @FXML private ChoiceBox<String> genderChoiceBox;
    @FXML private TextField emailField;
    @FXML private TextField passportField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private VBox registerBox;

    @FXML private Label usernameErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label genderErrorLabel;
    @FXML private Label emailErrorLabel;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+7|8)[0-9]{10}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    public void initialize() {
        LOGGER.info("Initializing RegisterController...");
        LOGGER.info("registerBox: " + (registerBox != null ? "Initialized" : "NULL"));

        registerButton.setOnAction(e -> handleRegister());
        backButton.setOnAction(e -> goBack());
        genderChoiceBox.setValue("Мужской");
        setupAnimation();
        addValidationListeners();
    }

    private void setupAnimation() {
        if (registerBox == null) {
            LOGGER.severe("Cannot setup animation: registerBox is null");
            return;
        }

        registerBox.setOpacity(0);
        registerBox.setTranslateY(20);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), registerBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), registerBox);
        translateTransition.setFromY(20);
        translateTransition.setToY(0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, translateTransition);
        parallelTransition.play();
    }

    private void addValidationListeners() {
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                usernameField.getStyleClass().remove("error");
            }
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (PASSWORD_PATTERN.matcher(newVal).matches()) {
                passwordField.getStyleClass().remove("error");
            }
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(passwordField.getText()) && !newVal.trim().isEmpty()) {
                confirmPasswordField.getStyleClass().remove("error");
            }
        });

        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                firstNameField.getStyleClass().remove("error");
            }
        });

        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                lastNameField.getStyleClass().remove("error");
            }
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (PHONE_PATTERN.matcher(newVal.trim()).matches()) {
                phoneField.getStyleClass().remove("error");
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (EMAIL_PATTERN.matcher(newVal.trim()).matches()) {
                emailField.getStyleClass().remove("error");
            }
        });

        genderChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                genderChoiceBox.getStyleClass().remove("error");
            }
        });
    }

    private void handleRegister() {
        resetStyles();
        hideAllErrors();

        boolean hasErrors = false;

        if (usernameField.getText().trim().isEmpty()) {
            showErrorWithAnimation(usernameErrorLabel, "Имя пользователя обязательно");
            usernameField.getStyleClass().add("error");
            hasErrors = true;
        }
        if (passwordField.getText().trim().isEmpty()) {
            showErrorWithAnimation(passwordErrorLabel, "Пароль обязателен");
            passwordField.getStyleClass().add("error");
            hasErrors = true;
        } else if (!PASSWORD_PATTERN.matcher(passwordField.getText()).matches()) {
            showErrorWithAnimation(passwordErrorLabel, "Минимум 8 символов, включая буквы и цифры");
            passwordField.getStyleClass().add("error");
            hasErrors = true;
        }
        if (confirmPasswordField.getText().trim().isEmpty()) {
            showErrorWithAnimation(confirmPasswordErrorLabel, "Подтверждение пароля обязательно");
            confirmPasswordField.getStyleClass().add("error");
            hasErrors = true;
        }
        if (firstNameField.getText().trim().isEmpty()) {
            showErrorWithAnimation(firstNameErrorLabel, "Имя обязательно");
            firstNameField.getStyleClass().add("error");
            hasErrors = true;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showErrorWithAnimation(lastNameErrorLabel, "Фамилия обязательна");
            lastNameField.getStyleClass().add("error");
            hasErrors = true;
        }
        if (genderChoiceBox.getValue() == null) {
            showErrorWithAnimation(genderErrorLabel, "Выберите пол");
            genderChoiceBox.getStyleClass().add("error");
            hasErrors = true;
        }
        if (emailField.getText().trim().isEmpty()) {
            showErrorWithAnimation(emailErrorLabel, "Email обязателен");
            emailField.getStyleClass().add("error");
            hasErrors = true;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            showErrorWithAnimation(emailErrorLabel, "Введите корректный email");
            emailField.getStyleClass().add("error");
            hasErrors = true;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showErrorWithAnimation(phoneErrorLabel, "Телефон обязателен");
            phoneField.getStyleClass().add("error");
            hasErrors = true;
        } else if (!PHONE_PATTERN.matcher(phoneField.getText().trim()).matches()) {
            showErrorWithAnimation(phoneErrorLabel, "Формат: +7XXXXXXXXXX или 8XXXXXXXXXX");
            phoneField.getStyleClass().add("error");
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        if (!password.equals(confirm)) {
            showErrorWithAnimation(confirmPasswordErrorLabel, "Пароли не совпадают.");
            confirmPasswordField.getStyleClass().add("error");
            return;
        }

        try {
            adminService.createUser(
                    usernameField.getText(),
                    password,
                    "CLIENT",
                    firstNameField.getText(),
                    lastNameField.getText(),
                    patronymicField.getText().trim().isEmpty() ? null : patronymicField.getText(),
                    phoneField.getText(),
                    genderChoiceBox.getValue(),
                    emailField.getText(),
                    passportField.getText().trim().isEmpty() ? null : passportField.getText()
            );

            showSuccess("Успех", "Вы успешно зарегистрированы!");
            goBack();
        } catch (Exception ex) {
            showAlert("Ошибка", ex.getMessage());
            LOGGER.severe("Registration error: " + ex.getMessage());
        }
    }

    private void showErrorWithAnimation(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void hideError(Label errorLabel) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), errorLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        });
        fadeOut.play();
    }

    private void hideAllErrors() {
        usernameErrorLabel.setVisible(false);
        usernameErrorLabel.setText("");
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setVisible(false);
        confirmPasswordErrorLabel.setText("");
        firstNameErrorLabel.setVisible(false);
        firstNameErrorLabel.setText("");
        lastNameErrorLabel.setVisible(false);
        lastNameErrorLabel.setText("");
        phoneErrorLabel.setVisible(false);
        phoneErrorLabel.setText("");
        genderErrorLabel.setVisible(false);
        genderErrorLabel.setText("");
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setText("");
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Fitness Pro — Вход");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Error returning to login: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetStyles() {
        usernameField.getStyleClass().remove("error");
        passwordField.getStyleClass().remove("error");
        confirmPasswordField.getStyleClass().remove("error");
        firstNameField.getStyleClass().remove("error");
        lastNameField.getStyleClass().remove("error");
        genderChoiceBox.getStyleClass().remove("error");
        emailField.getStyleClass().remove("error");
        phoneField.getStyleClass().remove("error");
    }
}