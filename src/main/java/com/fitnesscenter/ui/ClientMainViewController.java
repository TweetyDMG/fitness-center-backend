package com.fitnesscenter.ui;

import com.fitnesscenter.service.ClientService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ClientMainViewController {
    private static final Logger LOGGER = Logger.getLogger(ClientMainViewController.class.getName());

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private TabPane tabPane;

    @FXML private ClientDashboardController dashboardTabController;
    @FXML private ClientProfileController profileTabController;
    @FXML private ClientSubscriptionsController subscriptionsTabController;
    @FXML private com.fitnesscenter.ui.ClientVisitsController visitsTabController;
    @FXML private ClientScheduleController scheduleTabController;

    private Long currentClientId;
    private String clientFirstName;

    @Autowired
    private ApplicationContext springContext;

    public ClientMainViewController() {
    }

    @FXML
    public void initialize() {
        LOGGER.info("ClientMainViewController FXML инициализирован.");
        welcomeLabel.setText("Добро пожаловать!");

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                refreshTabData(newTab);
            }
        });
    }

    private void refreshTabData(Tab selectedTab) {
        String tabText = selectedTab.getText();

        if ("Расписание занятий".equals(tabText)) {
            if (scheduleTabController != null) {
                scheduleTabController.refreshScheduleData();
            }
        } else if ("Мои абонементы".equals(tabText)) {
            if (subscriptionsTabController != null) {
                subscriptionsTabController.refreshSubscriptionsData();
            }
        }
        else if ("Мои посещения".equals(tabText)) {
            if (visitsTabController != null) {
                visitsTabController.refreshVisitsData();
            }
        }

        else if ("Панель управления".equals(tabText)) {
            if (dashboardTabController != null) {
                dashboardTabController.refreshDashboardData();
            }
        }

    }

    public void initializeData(Long clientId, String firstName, ClientService service) {
        this.currentClientId = clientId;
        this.clientFirstName = firstName;

        LOGGER.info("=== Инициализация данных клиента ===");
        LOGGER.info("ID клиента: " + clientId);
        LOGGER.info("Имя клиента: " + firstName);
        LOGGER.info("Сервис доступен: " + (service != null ? "Да" : "Нет"));
        LOGGER.info("welcomeLabel: " + (welcomeLabel != null ? "Инициализирован" : "NULL"));

        if (service == null) {
            LOGGER.log(Level.SEVERE, "ClientService не был передан в ClientMainViewController!");
            welcomeLabel.setText("Ошибка: Сервис не доступен!");
            return;
        }

        Platform.runLater(() -> {
            welcomeLabel.setText("Добро пожаловать, " + (this.clientFirstName != null ? this.clientFirstName : "Клиент") + "!");
        });
        if (dashboardTabController != null) {
            dashboardTabController.initializeData(currentClientId, service);
            dashboardTabController.setMainTabPane(tabPane);
        } else {
            LOGGER.log(Level.WARNING, "dashboardTabController не был внедрен!");
        }
        if (profileTabController != null) {
            profileTabController.initializeData(currentClientId, service);
        } else {
            LOGGER.log(Level.WARNING, "profileTabController не был внедрен!");
        }
        if (subscriptionsTabController != null) {
            subscriptionsTabController.initializeData(currentClientId, service);
        } else {
            LOGGER.log(Level.WARNING, "subscriptionsTabController не был внедрен!");
        }
        if (visitsTabController != null) {
            visitsTabController.initializeData(currentClientId, service);
        } else {
            LOGGER.log(Level.WARNING, "visitsTabController не был внедрен!");
        }
        if (scheduleTabController != null) {
            scheduleTabController.initializeData(currentClientId, service);
        } else {
            LOGGER.log(Level.WARNING, "scheduleTabController не был внедрен!");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LOGGER.info("Пользователь " + clientFirstName + " (ID: " + currentClientId + ") инициировал выход из системы.");
        try {
            if (springContext == null) {
                LOGGER.severe("Spring ApplicationContext не внедрен. Невозможно загрузить экран входа.");
                showAlert("Критическая ошибка", "Не удалось настроить переход на экран входа.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            if (stage == null) {
                LOGGER.severe("Не удалось получить текущий Stage для смены сцены.");
                showAlert("Ошибка интерфейса", "Не удалось выполнить выход.");
                return;
            }

            Parent root = loader.load();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Fitness Pro — Вход");
            this.currentClientId = null;
            this.clientFirstName = null;
            LOGGER.info("Успешный выход из системы и переход на экран входа.");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки FXML для экрана входа: " + e.getMessage(), e);
            showAlert("Ошибка загрузки", "Не удалось загрузить экран входа.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Непредвиденная ошибка при выходе из системы: " + e.getMessage(), e);
            showAlert("Ошибка", "Произошла непредвиденная ошибка при выходе.");
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}