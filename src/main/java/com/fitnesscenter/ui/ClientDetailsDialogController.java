package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Client;
import com.fitnesscenter.entity.Disease;
import com.fitnesscenter.entity.Preference;
import com.fitnesscenter.entity.Recommendation;
import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.service.TrainerService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ClientDetailsDialogController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ClientDetailsDialogController.class.getName());

    @Autowired
    private TrainerService trainerService;

    @FXML private Label clientNameLabel;
    @FXML private Label clientContactLabel;
    @FXML private Label clientAgeLabel;
    @FXML private VBox preferencesContainer;
    @FXML private VBox diseasesContainer;
    @FXML private VBox recommendationsContainer;

    @FXML private VBox recommendationDialog;
    @FXML private TextArea recommendationTextArea;

    private Client currentClient;
    private Trainer currentTrainer;
    @Setter
    private Stage dialogStage;


    public void setClientAndTrainer(Client client, Trainer trainer) {
        this.currentClient = client;
        this.currentTrainer = trainer;
        populateClientDetails();
        if (dialogStage != null) {
            dialogStage.sizeToScene();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recommendationDialog.setVisible(false);
        recommendationDialog.setManaged(false);
    }

    private void populateClientDetails() {
        if (currentClient == null) {
            return;
        }

        clientNameLabel.setText(currentClient.getFirstname() + " " + currentClient.getLastname());
        clientContactLabel.setText("Email: " + currentClient.getEmail() + " | Телефон: " + currentClient.getPhone());

        loadClientPreferences(currentClient.getId());
        loadClientDiseases(currentClient.getId());
        loadClientRecommendations(currentClient.getId());
    }

    private void loadClientPreferences(Long clientId) {
        preferencesContainer.getChildren().clear();
        try {
            List<Preference> preferences = trainerService.getClientPreferences(clientId);
            if (preferences.isEmpty()) {
                preferencesContainer.getChildren().add(createStyledLabel("Предпочтения не указаны", "info-value"));
            } else {
                for (Preference preference : preferences) {
                    preferencesContainer.getChildren().add(createStyledLabel("• " + preference.getPreferenceType(), "info-value"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки предпочтений для клиента ID: " + clientId, e);
            preferencesContainer.getChildren().add(createStyledLabel("Ошибка загрузки предпочтений", "error-label"));
        }
    }

    private void loadClientDiseases(Long clientId) {
        diseasesContainer.getChildren().clear();
        try {
            List<Disease> diseases = trainerService.getClientDiseases(clientId);
            if (diseases.isEmpty()) {
                diseasesContainer.getChildren().add(createStyledLabel("Противопоказания отсутствуют", "info-value"));
            } else {
                for (Disease disease : diseases) {
                    diseasesContainer.getChildren().add(createStyledLabel("• " + disease.getDiseaseType(), "info-value"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки противопоказаний для клиента ID: " + clientId, e);
            diseasesContainer.getChildren().add(createStyledLabel("Ошибка загрузки противопоказаний", "error-label"));
        }
    }

    private void loadClientRecommendations(Long clientId) {
        recommendationsContainer.getChildren().clear();
        try {
            List<Recommendation> recommendations = trainerService.getClientRecommendations(clientId);
            if (recommendations.isEmpty()) {
                recommendationsContainer.getChildren().add(createStyledLabel("Рекомендации отсутствуют", "info-value"));
            } else {
                for (Recommendation recommendation : recommendations) {
                    Label label = createStyledLabel("• " + recommendation.getText(), "info-value");
                    label.setWrapText(true);
                    recommendationsContainer.getChildren().add(label);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки рекомендаций для клиента ID: " + clientId, e);
            recommendationsContainer.getChildren().add(createStyledLabel("Ошибка загрузки рекомендаций", "error-label"));
        }
    }

    private Label createStyledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }


    @FXML
    private void handleAddRecommendation() {
        if (currentTrainer == null || currentTrainer.getId() == null) {
            showAlert("Ошибка", "Идентификатор текущего тренера не доступен. Невозможно добавить рекомендацию.");
            return;
        }
        recommendationTextArea.clear();
        recommendationDialog.setVisible(true);
        recommendationDialog.setManaged(true);
        if (dialogStage != null) {
            dialogStage.sizeToScene();
        }
    }

    @FXML
    private void handleSaveRecommendation() {
        String text = recommendationTextArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("Предупреждение", "Введите текст рекомендации");
            return;
        }

        if (currentClient == null || currentTrainer == null) {
            showAlert("Ошибка", "Данные клиента или тренера не загружены. Невозможно сохранить рекомендацию.");
            return;
        }

        try {
            Recommendation recommendation = new Recommendation();
            recommendation.setClientId(currentClient.getId());
            recommendation.setTrainerId(Long.valueOf(currentTrainer.getId()));
            recommendation.setText(text);
            recommendation.setDate(LocalDateTime.now());

            trainerService.addRecommendation(recommendation);

            loadClientRecommendations(currentClient.getId());

            handleCancelRecommendation();

            showAlert("Успех", "Рекомендация успешно добавлена");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при сохранении рекомендации", e);
            showAlert("Ошибка", "Не удалось сохранить рекомендацию: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelRecommendation() {
        recommendationDialog.setVisible(false);
        recommendationDialog.setManaged(false);
        recommendationTextArea.clear();
        if (dialogStage != null) {
            dialogStage.sizeToScene();
        }
    }

    @FXML
    private void handleViewClientHistory() {
        if (currentClient == null) {
            showAlert("Предупреждение", "Данные клиента не загружены для просмотра истории.");
            return;
        }
        showAlert("Информация", "Функция просмотра истории посещений для клиента " +
                currentClient.getFirstname() + " " + currentClient.getLastname() + " будет реализована позже.");
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}