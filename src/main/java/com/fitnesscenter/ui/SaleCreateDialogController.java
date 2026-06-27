package com.fitnesscenter.ui;

import com.fitnesscenter.controller.SaleRequest;
import com.fitnesscenter.entity.Client;
import com.fitnesscenter.entity.Subscription;
import com.fitnesscenter.entity.Discount;
import com.fitnesscenter.entity.FitnessCenter;
import com.fitnesscenter.service.ManagerService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class SaleCreateDialogController implements DialogController, ManagerViewController.SaveableController {

    @FXML private ComboBox<Client> clientComboBox;
    @FXML private ComboBox<Subscription> subscriptionComboBox;
    @FXML private ComboBox<FitnessCenter> fitnessCenterComboBox;
    @FXML private ComboBox<Discount> discountComboBox;
    @FXML private TextField bankCardNumField;
    @FXML private DatePicker startDatePicker;
    @FXML private Label finalPriceLabel;

    @FXML private Button createButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private ManagerService managerService;
    private boolean saleCreated = false;
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
    public void setOnCancel(Runnable onCancel){
        this.onCancel = onCancel;
    }

    @FXML
    private void initialize() {
        clientComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Client client) {
                return client == null ? null : client.getLastname() + " " + client.getFirstname() + " (" + client.getPhone() + ")";
            }
            @Override public Client fromString(String string) { return null; /* Не используется для выбора */ }
        });

        subscriptionComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Subscription sub) {
                return sub == null ? null : sub.getName() + " (" + sub.getPrice() + " руб.)"; //getPrice() должен быть BigDecimal
            }
            @Override public Subscription fromString(String string) { return null; }
        });

        fitnessCenterComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(FitnessCenter center) {
                return center == null ? null : center.getName();
            }
            @Override public FitnessCenter fromString(String string) { return null; }
        });

        discountComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Discount disc) {
                return disc == null ? "Без скидки" : disc.getName() + " (" + disc.getPercentage() + "%)";
            }
            @Override public Discount fromString(String string) { return null; }
        });
        discountComboBox.getItems().add(null);
        subscriptionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateFinalPrice());
        discountComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateFinalPrice());

        startDatePicker.setValue(LocalDate.now());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void initData(List<Client> clients, List<Subscription> subscriptions,
                         List<Discount> discounts, List<FitnessCenter> centers, ManagerService managerService) {
        this.managerService = managerService;
        clientComboBox.setItems(FXCollections.observableArrayList(clients));
        subscriptionComboBox.setItems(FXCollections.observableArrayList(subscriptions));
        discountComboBox.getItems().addAll(FXCollections.observableArrayList(discounts));
        fitnessCenterComboBox.setItems(FXCollections.observableArrayList(centers));
    }

    public void setManagerService(ManagerService managerService) { // Альтернативный способ передать сервис
        this.managerService = managerService;
    }

    public boolean isSaleCreated() {
        return saleCreated;
    }

    @FXML
    private void handleCreateSale() {
        if (isInputValid()) {
            SaleRequest saleRequest = new SaleRequest();
            saleRequest.setClientId(clientComboBox.getValue().getId());
            saleRequest.setSubscriptionId(subscriptionComboBox.getValue().getId());
            saleRequest.setFitnessCenterId(fitnessCenterComboBox.getValue().getId());
            if (discountComboBox.getValue() != null) {
                saleRequest.setDiscountId(discountComboBox.getValue().getId());
            }
            saleRequest.setBankCardNum(bankCardNumField.getText());

            try {
                managerService.createSale(saleRequest);
                saleCreated = true;
                okClicked = true;
                if(dialogStage != null){
                    dialogStage.close();
                }
                if (onSave != null) {
                    onSave.run();
                }
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка создания продажи", "Не удалось оформить продажу: " + e.getMessage());
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

    private void calculateFinalPrice() {
        Subscription selectedSubscription = subscriptionComboBox.getValue();
        Discount selectedDiscount = discountComboBox.getValue();

        if (selectedSubscription != null && selectedSubscription.getPrice() != null) {
            BigDecimal price = BigDecimal.valueOf(selectedSubscription.getPrice());
            if (selectedDiscount != null && selectedDiscount.getPercentage() != null) {
                BigDecimal discountPercentage = BigDecimal.valueOf(selectedDiscount.getPercentage());
                BigDecimal discountAmount = price.multiply(discountPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                price = price.subtract(discountAmount);
            }
            finalPriceLabel.setText(String.format("%.2f руб.", price));
        } else {
            finalPriceLabel.setText("0.00 руб.");
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (clientComboBox.getValue() == null) {
            errorMessage += "Не выбран клиент!\n";
        }
        if (subscriptionComboBox.getValue() == null) {
            errorMessage += "Не выбран абонемент!\n";
        }
        if (fitnessCenterComboBox.getValue() == null) {
            errorMessage += "Не выбран фитнес-центр!\n";
        }
        if (bankCardNumField.getText() == null || bankCardNumField.getText().trim().isEmpty()) {
            errorMessage += "Не указан номер банковской карты!\n";
        } else if (!bankCardNumField.getText().matches("\\d{13,19}")) { // Простая валидация номера карты
            errorMessage += "Некорректный номер банковской карты (13-19 цифр)!\n";
        }
        if (startDatePicker.getValue() == null) {
            errorMessage += "Не указана дата начала!\n";
        }


        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Некорректный ввод", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }
}