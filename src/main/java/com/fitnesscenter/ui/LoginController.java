package com.fitnesscenter.ui;

import com.fitnesscenter.entity.User;
import com.fitnesscenter.repository.UserRepository;
import com.fitnesscenter.service.AdminService;
import com.fitnesscenter.service.ClientService;
import com.fitnesscenter.service.TrainerService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private AdminService adminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button forgotPasswordButton;

    @FXML
    private VBox loginBox;

    @FXML
    public void initialize() {
        LOGGER.info("Initializing LoginController components...");
        if (loginBox == null) {
            LOGGER.severe("FXML injection failed: loginBox is null");
            return;
        }
        setupEventHandlers();
        Platform.runLater(this::setupAnimation);
    }

    private void setupEventHandlers() {
        if (loginButton != null) {
            loginButton.setOnAction(this::handleLogin);
        } else {
            LOGGER.severe("Login button is null");
        }

        if (registerButton != null) {
            registerButton.setOnAction(this::handleRegister);
        } else {
            LOGGER.severe("Register button is null");
        }

        if (forgotPasswordButton != null) {
            forgotPasswordButton.setOnAction(this::handleForgotPassword);
        } else {
            LOGGER.severe("Forgot password button is null");
        }
    }

    private void setupAnimation() {
        try {
            loginBox.setOpacity(0);
            loginBox.setTranslateY(20);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), loginBox);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);

            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), loginBox);
            translateTransition.setFromY(20);
            translateTransition.setToY(0);

            new ParallelTransition(fadeTransition, translateTransition).play();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Animation setup failed", e);
        }
    }

    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка ввода", "Пожалуйста, введите логин и пароль");
            return;
        }

        try {
            User user = adminService.findUserByUsername(username);
            if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
                showAlert("Ошибка аутентификации", "Неверный логин или пароль");
                return;
            }

            openUserDashboard(user);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login error", e);
            showAlert("Ошибка системы", "Произошла ошибка при входе в систему");
        }
    }

    private void openUserDashboard(User user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            String fxmlPath = getDashboardFxmlPath(user.getRole());

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            Object controller = loader.getController();

            if (user.getRole().equals("CLIENT") && controller instanceof ClientMainViewController clientController) {

                if (user.getClient() == null) {
                    LOGGER.severe("У пользователя нет связанного клиента!");
                    showAlert("Ошибка", "Данные клиента не найдены");
                    return;
                }
                ClientService clientService = springContext.getBean(ClientService.class);
                clientController.initializeData(
                        user.getClient().getId(),
                        user.getClient().getFirstname(),
                        clientService
                );

                LOGGER.info("Данные клиента успешно инициализированы");
            }

            if (user.getRole().equals("TRAINER") && controller instanceof TrainerMainViewController trainerController) {

                if (user.getTrainer() == null) {
                    LOGGER.severe("У пользователя нет связанного тренера!");
                    showAlert("Ошибка", "Данные тренера не найдены");
                    return;
                }
                trainerController.setTrainer(user.getTrainer());

                LOGGER.info("Данные тренера успешно инициализированы");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Fitness Pro — " + user.getRole());
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки интерфейса", e);
            showAlert("Ошибка", "Не удалось загрузить панель управления");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Общая ошибка инициализации", e);
            showAlert("Ошибка", "Произошла непредвиденная ошибка");
        }
    }

    private String getDashboardFxmlPath(String role) {
        return switch (role) {
            case "ADMIN" -> "/ui/dashboard-admin.fxml";
            case "MANAGER" -> "/ui/manager-view.fxml";
            case "CLIENT" -> "/ui/client-main-view.fxml";
            case "TRAINER" -> "/ui/trainer-main-view.fxml";
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }

    private void handleRegister(ActionEvent event) {
        try {
            Stage stage = (Stage) registerButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/register.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Fitness Pro — Регистрация");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Registration error", e);
            showAlert("Ошибка", "Не удалось открыть форму регистрации");
        }
    }

    private void handleForgotPassword(ActionEvent event) {
        showAlert("Восстановление пароля", "Для восстановления пароля обратитесь к администратору");
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}