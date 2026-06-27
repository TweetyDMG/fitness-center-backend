package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.service.ClientService;
import com.fitnesscenter.service.TrainerService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TrainerMainViewController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TrainerMainViewController.class.getName());

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    TabPane tabPane;

    @Getter
    private Trainer currentTrainer;

    @Autowired
    private ApplicationContext springContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTabsContent();
    }

    public void setTrainer(Trainer trainer) {
        this.currentTrainer = trainer;
        updateWelcomeLabel();
        initializeTabsData();
    }

    private void updateWelcomeLabel() {
        if (currentTrainer != null) {
            welcomeLabel.setText("Панель тренера - Добро пожаловать, " +
                    currentTrainer.getFirstname() + " " + currentTrainer.getLastname() + "!");
        }
    }

    private void loadTabsContent() {
        try {
            FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/ui/trainer-dashboard-view.fxml"));
            dashboardLoader.setControllerFactory(springContext::getBean);
            Parent dashboardRoot = dashboardLoader.load();
            TrainerDashboardViewController dashboardController = dashboardLoader.getController();
            Tab dashboardTab = new Tab("Дашборд", dashboardRoot);
            dashboardTab.getProperties().put("controller", dashboardController);
            tabPane.getTabs().add(dashboardTab);

            FXMLLoader scheduleLoader = new FXMLLoader(getClass().getResource("/ui/trainer-schedule-view.fxml"));
            scheduleLoader.setControllerFactory(springContext::getBean);
            Parent scheduleRoot = scheduleLoader.load();
            TrainerScheduleViewController scheduleController = scheduleLoader.getController();
            Tab scheduleTab = new Tab("Расписание", scheduleRoot);
            scheduleTab.getProperties().put("controller", scheduleController);
            tabPane.getTabs().add(scheduleTab);

            FXMLLoader clientsLoader = new FXMLLoader(getClass().getResource("/ui/trainer-clients-view.fxml"));
            clientsLoader.setControllerFactory(springContext::getBean);
            Parent clientsRoot = clientsLoader.load();
            TrainerClientsViewController clientsController = clientsLoader.getController();
            Tab clientsTab = new Tab("Клиенты", clientsRoot);
            clientsTab.getProperties().put("controller", clientsController);
            tabPane.getTabs().add(clientsTab);

            FXMLLoader visitsLoader = new FXMLLoader(getClass().getResource("/ui/trainer-visits-view.fxml"));
            visitsLoader.setControllerFactory(springContext::getBean);
            Parent visitsRoot = visitsLoader.load();
            TrainerVisitsViewController visitsController = visitsLoader.getController();
            Tab visitsTab = new Tab("Посещения", visitsRoot);
            visitsTab.getProperties().put("controller", visitsController);
            tabPane.getTabs().add(visitsTab);

            FXMLLoader reportsLoader = new FXMLLoader(getClass().getResource("/ui/trainer-reports-view.fxml"));
            reportsLoader.setControllerFactory(springContext::getBean);
            Parent reportsRoot = reportsLoader.load();
            TrainerReportsViewController reportsController = reportsLoader.getController();
            Tab reportsTab = new Tab("Отчёты", reportsRoot);
            reportsTab.getProperties().put("controller", reportsController);
            tabPane.getTabs().add(reportsTab);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "ОШИБКА: Не удалось загрузить FXML для вкладки. Проверьте путь и содержимое FXML: " + e.getMessage(), e);
            showAlert("Ошибка загрузки FXML", "Не удалось загрузить содержимое одной из вкладок. См. логи для подробностей.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "ОБЩАЯ ОШИБКА: Произошла непредвиденная ошибка при загрузке содержимого вкладок: " + e.getMessage(), e);
            showAlert("Непредвиденная ошибка", "Произошла непредвиденная ошибка при загрузке вкладок. См. логи.");
        }
    }

    private void initializeTabsData() {
        if (currentTrainer == null) {
            LOGGER.warning("currentTrainer равен null при попытке инициализировать данные вкладок.");
            return;
        }

        try {
            TrainerDashboardViewController dashboardController =
                    (TrainerDashboardViewController) tabPane.getTabs().get(0).getProperties().get("controller");
            if (dashboardController != null) {
                dashboardController.setTrainer(currentTrainer);
            } else {
                LOGGER.severe("Dashboard controller not found in tab properties.");
            }

            TrainerScheduleViewController scheduleController =
                    (TrainerScheduleViewController) tabPane.getTabs().get(1).getProperties().get("controller");
            if (scheduleController != null) {
                scheduleController.setTrainer(currentTrainer);
            } else {
                LOGGER.severe("Schedule controller not found in tab properties.");
            }

            TrainerClientsViewController clientsController =
                    (TrainerClientsViewController) tabPane.getTabs().get(2).getProperties().get("controller");
            if (clientsController != null) {
                clientsController.setTrainer(currentTrainer);
            } else {
                LOGGER.severe("Clients controller not found in tab properties.");
            }

            TrainerVisitsViewController visitsController =
                    (TrainerVisitsViewController) tabPane.getTabs().get(3).getProperties().get("controller");
            if (visitsController != null) {
                visitsController.setTrainer(currentTrainer);
            } else {
                LOGGER.severe("Visits controller not found in tab properties.");
            }

            if (tabPane.getTabs().size() > 4) {
                TrainerReportsViewController reportsController =
                        (TrainerReportsViewController) tabPane.getTabs().get(4).getProperties().get("controller");
                if (reportsController != null) {
                    reportsController.setTrainer(currentTrainer);
                } else {
                    LOGGER.severe("Reports controller not found in tab properties.");
                }
            } else {
                LOGGER.severe("Reports tab not found or not added correctly.");
            }


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при инициализации данных вкладок", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            if (springContext == null) {
                showAlert("Критическая ошибка", "Не удалось настроить переход на экран входа.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            if (stage == null) {
                showAlert("Ошибка интерфейса", "Не удалось выполнить выход.");
                return;
            }

            Parent root = loader.load();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Fitness Pro — Вход");
            this.currentTrainer = null;
            if (this.welcomeLabel != null) {
                this.welcomeLabel.setText("Панель тренера - Добро пожаловать, !");
            }

        } catch (IOException e) {
            showAlert("Ошибка загрузки", "Не удалось загрузить экран входа.");
        } catch (Exception e) {
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