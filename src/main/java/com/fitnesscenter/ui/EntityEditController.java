package com.fitnesscenter.ui;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.service.AdminService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class EntityEditController {

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private AdminService adminService;

    @FXML
    private VBox editBox;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox fieldsBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Map<String, TextField> fields = new HashMap<>();
    private Consumer<Map<String, String>> saveHandler;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initialize() {
        cancelButton.setOnAction(event -> stage.close());
    }

    public void setupForm(String entityType, Object entity, Consumer<Map<String, String>> saveHandler) {
        this.saveHandler = saveHandler;
        titleLabel.setText("Редактирование: " + entityType);
        subtitleLabel.setText("Заполните данные для " + entityType);

        fieldsBox.getChildren().clear();
        fields.clear();

        switch (entityType) {
            case "Пользователь":
                setupUserForm((User) entity);
                break;
            case "Клиент":
                setupClientForm((Client) entity);
                break;
            case "Тренер":
                setupTrainerForm((Trainer) entity);
                break;
            case "Абонемент":
                setupSubscriptionForm((Subscription) entity);
                break;
        }

        saveButton.setOnAction(event -> {
            Map<String, String> data = new HashMap<>();
            fields.forEach((key, field) -> data.put(key, field.getText()));
            saveHandler.accept(data);
            stage.close();
        });
    }

    private void setupUserForm(User user) {
        addField("username", "Имя пользователя", user != null ? user.getUsername() : "");
        addField("role", "Роль", user != null ? user.getRole() : "");
        addField("password", "Пароль (для создания)", "");
    }

    private void setupClientForm(Client client) {
        addField("firstname", "Имя", client != null ? client.getFirstname() : "");
        addField("lastname", "Фамилия", client != null ? client.getLastname() : "");
        addField("patronymic", "Отчество", client != null ? client.getPatronymic() : "");
        addField("phone", "Телефон", client != null ? client.getPhone() : "");
        addField("gender", "Пол", client != null ? client.getGender() : "");
        addField("email", "Email", client != null ? client.getEmail() : "");
        addField("passport", "Паспорт", client != null ? client.getPassport() : "");
    }

    private void setupTrainerForm(Trainer trainer) {
        addField("firstname", "Имя", trainer != null ? trainer.getFirstname() : "");
        addField("lastname", "Фамилия", trainer != null ? trainer.getLastname() : "");
        addField("patronymic", "Отчество", trainer != null ? trainer.getPatronymic() : "");
        addField("phoneNumber", "Телефон", trainer != null ? trainer.getPhoneNumber() : "");
        addField("address", "Адрес", trainer != null ? trainer.getAddress() : "");
    }

    private void setupSubscriptionForm(Subscription subscription) {
        addField("name", "Название", subscription != null ? subscription.getName() : "");
        addField("price", "Цена", subscription != null ? String.valueOf(subscription.getPrice()) : "");
        addField("duration", "Длительность", subscription != null ? String.valueOf(subscription.getDuration()) : "");
    }

    private void addField(String key, String prompt, String value) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setText(value);
        field.getStyleClass().add("text-input");
        fieldsBox.getChildren().add(field);
        fields.put(key, field);
    }
}