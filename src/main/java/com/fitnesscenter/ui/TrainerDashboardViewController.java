package com.fitnesscenter.ui;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.service.TrainerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class TrainerDashboardViewController implements Initializable {

    @FXML
    public Button createScheduleBtn;
    @FXML
    private Label totalClientsLabel;
    @FXML
    private Label todayClassesLabel;
    @FXML
    private Label recommendationsLabel;
    @FXML
    private VBox upcomingClassesContainer;

    @Autowired
    private TrainerService trainerService;
    @Autowired
    private ApplicationContext springContext;

    private Trainer currentTrainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setTrainer(Trainer trainer) {
        this.currentTrainer = trainer;
        loadDashboardData();
    }

    void loadDashboardData() {
        if (currentTrainer == null) return;

        Platform.runLater(() -> {
            try {
                loadStatistics();
                loadUpcomingClasses();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadStatistics() {
        try {
            List<Client> allClients = trainerService.getAllClients();
            totalClientsLabel.setText(String.valueOf(allClients.size()));

            List<Schedule> mySchedule = trainerService.getMySchedule(Long.valueOf(currentTrainer.getId()));
            long todayClasses = mySchedule.stream()
                    .filter(schedule -> schedule.getDate().equals(LocalDate.now()))
                    .count();
            todayClassesLabel.setText(String.valueOf(todayClasses));
            int totalRecommendations = 0;
            for (Client client : allClients) {
                List<Recommendation> clientRecommendations = trainerService.getClientRecommendations(client.getId());
                totalRecommendations += clientRecommendations.size();
            }
            recommendationsLabel.setText(String.valueOf(totalRecommendations));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUpcomingClasses() {
        try {
            upcomingClassesContainer.getChildren().clear();

            List<Schedule> mySchedule = trainerService.getMySchedule(Long.valueOf(currentTrainer.getId()));
            LocalDate today = LocalDate.now();

            mySchedule.stream()
                    .filter(schedule -> !schedule.getDate().isBefore(today))
                    .sorted(Comparator.comparing(Schedule::getDate))
                    .limit(5)
                    .forEach(this::createUpcomingClassCard);

            if (upcomingClassesContainer.getChildren().isEmpty()) {
                Label noClassesLabel = new Label("Нет запланированных занятий");
                noClassesLabel.getStyleClass().add("info-message");
                upcomingClassesContainer.getChildren().add(noClassesLabel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUpcomingClassCard(Schedule schedule) {
        HBox classCard = new HBox(15);
        classCard.getStyleClass().add("class-card");
        classCard.setPadding(new Insets(10));

        VBox dateTimeBox = new VBox(5);
        Label dateLabel = new Label(schedule.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        dateLabel.getStyleClass().add("class-date");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String startTimeStr = schedule.getStartTime().toLocalTime().format(timeFormatter);
        String endTimeStr = schedule.getEndTime().toLocalTime().format(timeFormatter);
        Label timeLabel = new Label(startTimeStr + " - " + endTimeStr);
        timeLabel.getStyleClass().add("class-time");
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        VBox detailsBox = new VBox(5);

        Button viewVisitsBtn = new Button("Посещения");
        viewVisitsBtn.getStyleClass().add("btn-small");
        viewVisitsBtn.setOnAction(e -> handleViewVisits(schedule.getId()));

        classCard.getChildren().addAll(dateTimeBox, detailsBox, viewVisitsBtn);
        upcomingClassesContainer.getChildren().add(classCard);
    }

    @FXML
    private void handleCreateSchedule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/create_schedule_dialog.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            CreateScheduleDialogController controller = loader.getController();
            controller.initialize();
            controller.setTrainer(currentTrainer);
            controller.setTrainerDashboardViewController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать занятие");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(createScheduleBtn.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить окно создания занятия.");
        }
    }

    @FXML
    private void handleViewClients() {
        switchToTab(2);
    }

    @FXML
    private void handleAddRecommendation() {
        switchToTab(4);
    }

    private void handleViewVisits(Long scheduleId) {
        switchToTab(3);
    }

    private void switchToTab(int tabIndex) {
        try {
            TrainerMainViewController mainController = getMainController();
            if (mainController != null) {
                mainController.tabPane.getSelectionModel().select(tabIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TrainerMainViewController getMainController() {
        return (TrainerMainViewController) createScheduleBtn.getScene().getWindow().getUserData();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}