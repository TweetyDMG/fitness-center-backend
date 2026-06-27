package com.fitnesscenter.ui;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.service.AdminService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Component("adminUiController")
public class AdminController {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private AdminService adminService;

    @FXML
    private VBox adminBox;
    @FXML
    private TabPane tabPane;

    @FXML
    private TextField userSearchField;
    @FXML
    private Button userSearchButton;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Long> userIdColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private Button addUserButton;
    @FXML
    private Button editUserButton;
    @FXML
    private Button deleteUserButton;
    private ObservableList<User> userList;

    @FXML
    private TextField clientSearchField;
    @FXML
    private Button clientSearchButton;
    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Long> clientIdColumn;
    @FXML
    private TableColumn<Client, String> clientFirstNameColumn;
    @FXML
    private TableColumn<Client, String> clientLastNameColumn;
    @FXML
    private TableColumn<Client, String> clientPhoneColumn;
    @FXML
    private Button addClientButton;
    @FXML
    private Button editClientButton;
    @FXML
    private Button deleteClientButton;
    private ObservableList<Client> clientList;

    @FXML
    private TextField trainerSearchField;
    @FXML
    private Button trainerSearchButton;
    @FXML
    private TableView<Trainer> trainerTable;
    @FXML
    private TableColumn<Trainer, Long> trainerIdColumn;
    @FXML
    private TableColumn<Trainer, String> trainerFirstNameColumn;
    @FXML
    private TableColumn<Trainer, String> trainerLastNameColumn;
    @FXML
    private TableColumn<Trainer, String> trainerPhoneColumn;
    @FXML
    private Button addTrainerButton;
    @FXML
    private Button editTrainerButton;
    @FXML
    private Button deleteTrainerButton;
    private ObservableList<Trainer> trainerList;

    @FXML
    private TextField subscriptionSearchField;
    @FXML
    private Button subscriptionSearchButton;
    @FXML
    private TableView<Subscription> subscriptionTable;
    @FXML
    private TableColumn<Subscription, Long> subscriptionIdColumn;
    @FXML
    private TableColumn<Subscription, String> subscriptionNameColumn;
    @FXML
    private TableColumn<Subscription, String> subscriptionPriceColumn;
    @FXML
    private Button addSubscriptionButton;
    @FXML
    private Button editSubscriptionButton;
    @FXML
    private Button deleteSubscriptionButton;
    private ObservableList<Subscription> subscriptionList;

    @FXML
    private Button logoutButton;

    public void initialize() {
        LOGGER.info("Initializing AdminController...");
        LOGGER.info("adminBox: " + (adminBox != null ? "Initialized" : "NULL"));

        setupUserTable();
        setupClientTable();
        setupTrainerTable();
        setupSubscriptionTable();

        loadUsers();
        loadClients();
        loadTrainers();
        loadSubscriptions();

        userSearchButton.setOnAction(this::handleUserSearch);
        addUserButton.setOnAction(this::handleAddUser);
        editUserButton.setOnAction(this::handleEditUser);
        deleteUserButton.setOnAction(this::handleDeleteUser);

        clientSearchButton.setOnAction(this::handleClientSearch);
        addClientButton.setOnAction(this::handleAddClient);
        editClientButton.setOnAction(this::handleEditClient);
        deleteClientButton.setOnAction(this::handleDeleteClient);

        trainerSearchButton.setOnAction(this::handleTrainerSearch);
        addTrainerButton.setOnAction(this::handleAddTrainer);
        editTrainerButton.setOnAction(this::handleEditTrainer);
        deleteTrainerButton.setOnAction(this::handleDeleteTrainer);

        subscriptionSearchButton.setOnAction(this::handleSubscriptionSearch);
        addSubscriptionButton.setOnAction(this::handleAddSubscription);
        editSubscriptionButton.setOnAction(this::handleEditSubscription);
        deleteSubscriptionButton.setOnAction(this::handleDeleteSubscription);

        logoutButton.setOnAction(this::handleLogout);

        setupAnimation();
    }

    private void setupAnimation() {
        if (adminBox == null) {
            LOGGER.severe("Cannot setup animation: adminBox is null");
            return;
        }
        adminBox.setOpacity(0);
        adminBox.setTranslateY(20);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), adminBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), adminBox);
        translateTransition.setFromY(20);
        translateTransition.setToY(0);
        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, translateTransition);
        parallelTransition.play();
    }

    private void setupUserTable() {
        userIdColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        userList = FXCollections.observableArrayList();
        userTable.setItems(userList);
    }

    private void setupClientTable() {
        clientIdColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        clientFirstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstname()));
        clientLastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastname()));
        clientPhoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        clientList = FXCollections.observableArrayList();
        clientTable.setItems(clientList);
    }

    private void setupTrainerTable() {
        trainerIdColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        trainerFirstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstname()));
        trainerLastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastname()));
        trainerPhoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        trainerList = FXCollections.observableArrayList();
        trainerTable.setItems(trainerList);
    }

    private void setupSubscriptionTable() {
        subscriptionIdColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        subscriptionNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        subscriptionPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPrice())));
        subscriptionList = FXCollections.observableArrayList();
        subscriptionTable.setItems(subscriptionList);
    }

    private void loadUsers() {
        try {
            userList.setAll(adminService.getAllUsers());
        } catch (Exception e) {
            LOGGER.severe("Error loading users: " + e.getMessage());
            showAlert("Ошибка", "Не удалось загрузить пользователей");
        }
    }

    private void loadClients() {
        try {
            clientList.setAll(adminService.getAllClients());
        } catch (Exception e) {
            LOGGER.severe("Error loading clients: " + e.getMessage());
            showAlert("Ошибка", "Не удалось загрузить клиентов");
        }
    }

    private void loadTrainers() {
        try {
            trainerList.setAll(adminService.getAllTrainers());
        } catch (Exception e) {
            LOGGER.severe("Error loading trainers: " + e.getMessage());
            showAlert("Ошибка", "Не удалось загрузить тренеров");
        }
    }

    private void loadSubscriptions() {
        try {
            subscriptionList.setAll(adminService.getAllSubscriptions());
        } catch (Exception e) {
            LOGGER.severe("Error loading subscriptions: " + e.getMessage());
            showAlert("Ошибка", "Не удалось загрузить абонементы");
        }
    }

    private void handleUserSearch(ActionEvent event) {
        String searchText = userSearchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadUsers();
        } else {
            userList.setAll(adminService.getAllUsers().stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(searchText))
                    .toList());
        }
    }

    private void handleClientSearch(ActionEvent event) {
        String searchText = clientSearchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadClients();
        } else {
            clientList.setAll(adminService.getAllClients().stream()
                    .filter(client -> client.getFirstname().toLowerCase().contains(searchText) ||
                            client.getLastname().toLowerCase().contains(searchText))
                    .toList());
        }
    }

    private void handleTrainerSearch(ActionEvent event) {
        String searchText = trainerSearchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadTrainers();
        } else {
            trainerList.setAll(adminService.getAllTrainers().stream()
                    .filter(trainer -> trainer.getFirstname().toLowerCase().contains(searchText) ||
                            trainer.getLastname().toLowerCase().contains(searchText))
                    .toList());
        }
    }

    private void handleSubscriptionSearch(ActionEvent event) {
        String searchText = subscriptionSearchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadSubscriptions();
        } else {
            subscriptionList.setAll(adminService.getAllSubscriptions().stream()
                    .filter(subscription -> subscription.getName().toLowerCase().contains(searchText))
                    .toList());
        }
    }

    private void handleAddUser(ActionEvent event) {
        openEditForm("Пользователь", null, data -> {
            try {
                adminService.createUser(
                        data.get("username"),
                        data.get("password"),
                        data.get("role"),
                        null, null, null, null, null, null, null
                );
                loadUsers();
                showSuccess("Успех", "Пользователь создан");
            } catch (Exception e) {
                LOGGER.severe("Error creating user: " + e.getMessage());
                showAlert("Ошибка", "Не удалось создать пользователя: " + e.getMessage());
            }
        });
    }

    private void handleAddClient(ActionEvent event) {
        openEditForm("Клиент", null, data -> {
            try {
                Client client = new Client();
                client.setFirstname(data.get("firstname"));
                client.setLastname(data.get("lastname"));
                client.setPatronymic(data.get("patronymic"));
                client.setPhone(data.get("phone"));
                client.setGender(data.get("gender"));
                client.setEmail(data.get("email"));
                client.setPassport(data.get("passport"));
                adminService.createClient(client);
                loadClients();
                showSuccess("Успех", "Клиент создан");
            } catch (Exception e) {
                LOGGER.severe("Error creating client: " + e.getMessage());
                showAlert("Ошибка", "Не удалось создать клиента: " + e.getMessage());
            }
        });
    }

    private void handleAddTrainer(ActionEvent event) {
        openEditForm("Тренер", null, data -> {
            try {
                Trainer trainer = new Trainer();
                trainer.setFirstname(data.get("firstname"));
                trainer.setLastname(data.get("lastname"));
                trainer.setPatronymic(data.get("patronymic"));
                trainer.setPhoneNumber(data.get("phoneNumber"));
                trainer.setAddress(data.get("address"));
                adminService.createTrainer(trainer);
                loadTrainers();
                showSuccess("Успех", "Тренер создан");
            } catch (Exception e) {
                LOGGER.severe("Error creating trainer: " + e.getMessage());
                showAlert("Ошибка", "Не удалось создать тренера: " + e.getMessage());
            }
        });
    }

    private void handleAddSubscription(ActionEvent event) {
        openEditForm("Абонемент", null, data -> {
            try {
                Subscription subscription = new Subscription();
                subscription.setName(data.get("name"));
                subscription.setPrice((int) Double.parseDouble(data.get("price")));
                subscription.setDuration(Integer.parseInt(data.get("duration")));
                adminService.createSubscription(subscription);
                loadSubscriptions();
                showSuccess("Успех", "Абонемент создан");
            } catch (Exception e) {
                LOGGER.severe("Error creating subscription: " + e.getMessage());
                showAlert("Ошибка", "Не удалось создать абонемент: " + e.getMessage());
            }
        });
    }

    private void handleEditUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Ошибка", "Выберите пользователя для редактирования");
            return;
        }
        openEditForm("Пользователь", selectedUser, data -> {
            try {
                adminService.updateUser(
                        selectedUser.getId(),
                        data.get("username"),
                        data.get("role"),
                        selectedUser.getClient()
                );
                loadUsers();
                showSuccess("Успех", "Пользователь обновлен");
            } catch (Exception e) {
                LOGGER.severe("Error updating user: " + e.getMessage());
                showAlert("Ошибка", "Не удалось обновить пользователя: " + e.getMessage());
            }
        });
    }

    private void handleEditClient(ActionEvent event) {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Ошибка", "Выберите клиента для редактирования");
            return;
        }
        openEditForm("Клиент", selectedClient, data -> {
            try {
                Client client = new Client();
                client.setFirstname(data.get("firstname"));
                client.setLastname(data.get("lastname"));
                client.setPatronymic(data.get("patronymic"));
                client.setPhone(data.get("phone"));
                client.setGender(data.get("gender"));
                client.setEmail(data.get("email"));
                client.setPassport(data.get("passport"));
                adminService.updateClient(selectedClient.getId(), client);
                loadClients();
                showSuccess("Успех", "Клиент обновлен");
            } catch (Exception e) {
                LOGGER.severe("Error updating client: " + e.getMessage());
                showAlert("Ошибка", "Не удалось обновить клиента: " + e.getMessage());
            }
        });
    }

    private void handleEditTrainer(ActionEvent event) {
        Trainer selectedTrainer = trainerTable.getSelectionModel().getSelectedItem();
        if (selectedTrainer == null) {
            showAlert("Ошибка", "Выберите тренера для редактирования");
            return;
        }
        openEditForm("Тренер", selectedTrainer, data -> {
            try {
                Trainer trainer = new Trainer();
                trainer.setFirstname(data.get("firstname"));
                trainer.setLastname(data.get("lastname"));
                trainer.setPatronymic(data.get("patronymic"));
                trainer.setPhoneNumber(data.get("phoneNumber"));
                trainer.setAddress(data.get("address"));
                adminService.updateTrainer(Long.valueOf(selectedTrainer.getId()), trainer);
                loadTrainers();
                showSuccess("Успех", "Тренер обновлен");
            } catch (Exception e) {
                LOGGER.severe("Error updating trainer: " + e.getMessage());
                showAlert("Ошибка", "Не удалось обновить тренера: " + e.getMessage());
            }
        });
    }

    private void handleEditSubscription(ActionEvent event) {
        Subscription selectedSubscription = subscriptionTable.getSelectionModel().getSelectedItem();
        if (selectedSubscription == null) {
            showAlert("Ошибка", "Выберите абонемент для редактирования");
            return;
        }
        openEditForm("Абонемент", selectedSubscription, data -> {
            try {
                Subscription subscription = new Subscription();
                subscription.setName(data.get("name"));
                subscription.setPrice((int) Double.parseDouble(data.get("price")));
                subscription.setDuration(Integer.parseInt(data.get("duration")));
                adminService.updateSubscription(selectedSubscription.getId(), subscription);
                loadSubscriptions();
                showSuccess("Успех", "Абонемент обновлен");
            } catch (Exception e) {
                LOGGER.severe("Error updating subscription: " + e.getMessage());
                showAlert("Ошибка", "Не удалось обновить абонемент: " + e.getMessage());
            }
        });
    }

    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Ошибка", "Выберите пользователя для удаления");
            return;
        }
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = adminService.findUserByUsername(currentUsername);
        if (currentUser != null && currentUser.getId().equals(selectedUser.getId())) {
            showAlert("Ошибка", "Вы не можете удалить самого себя");
            return;
        }
        try {
            adminService.deleteUser(selectedUser.getId());
            loadUsers();
            showSuccess("Успех", "Пользователь удален");
        } catch (Exception e) {
            LOGGER.severe("Error deleting user: " + e.getMessage());
            showAlert("Ошибка", "Не удалось удалить пользователя: " + e.getMessage());
        }
    }

    private void handleDeleteClient(ActionEvent event) {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Ошибка", "Выберите клиента для удаления");
            return;
        }
        try {
            List<Sale> sales = adminService.getAllSales().stream()
                    .filter(sale -> sale.getClient() != null && sale.getClient().getId().equals(selectedClient.getId()))
                    .toList();
            List<RegistrationOfVisit> visits = adminService.getAllVisits().stream()
                    .filter(visit -> visit.getSaleId() != null && adminService.getAllSales().stream()
                            .anyMatch(sale -> sale.getId().equals(visit.getSaleId()) && sale.getClient() != null && sale.getClient().getId().equals(selectedClient.getId())))
                    .toList();
            List<Disease> diseases = adminService.getAllDiseases().stream()
                    .filter(disease -> disease.getClient() != null && disease.getClient().getId().equals(selectedClient.getId()))
                    .toList();

            if (!sales.isEmpty() || !visits.isEmpty() || !diseases.isEmpty()) {
                showAlert("Ошибка", "Нельзя удалить клиента, связанного с продажами, посещениями или заболеваниями");
                return;
            }

            adminService.deleteClient(selectedClient.getId());
            loadClients();
            showSuccess("Успех", "Клиент удален");
        } catch (Exception e) {
            LOGGER.severe("Error deleting client: " + e.getMessage());
            showAlert("Ошибка", "Не удалось удалить клиента: " + e.getMessage());
        }
    }

    private void handleDeleteTrainer(ActionEvent event) {
        Trainer selectedTrainer = trainerTable.getSelectionModel().getSelectedItem();
        if (selectedTrainer == null) {
            showAlert("Ошибка", "Выберите тренера для удаления");
            return;
        }
        try {
            adminService.deleteTrainer(Long.valueOf(selectedTrainer.getId()));
            loadTrainers();
            showSuccess("Успех", "Тренер удален");
        } catch (Exception e) {
            LOGGER.severe("Error deleting trainer: " + e.getMessage());
            showAlert("Ошибка", "Не удалось удалить тренера: " + e.getMessage());
        }
    }

    private void handleDeleteSubscription(ActionEvent event) {
        Subscription selectedSubscription = subscriptionTable.getSelectionModel().getSelectedItem();
        if (selectedSubscription == null) {
            showAlert("Ошибка", "Выберите абонемент для удаления");
            return;
        }
        try {
            adminService.deleteSubscription(selectedSubscription.getId());
            loadSubscriptions();
            showSuccess("Успех", "Абонемент удален");
        } catch (Exception e) {
            LOGGER.severe("Error deleting subscription: " + e.getMessage());
            showAlert("Ошибка", "Не удалось удалить абонемент: " + e.getMessage());
        }
    }

    private void openEditForm(String entityType, Object entity, Consumer<Map<String, String>> saveHandler) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/edit-entity.fxml"));
            loader.setControllerFactory(springContext::getBean);
            VBox root = loader.load();
            EntityEditController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Редактирование: " + entityType);
            controller.setStage(stage);
            controller.setupForm(entityType, entity, saveHandler);

            stage.showAndWait();
        } catch (Exception e) {
            LOGGER.severe("Error opening edit form: " + e.getMessage());
            showAlert("Ошибка", "Не удалось открыть форму редактирования");
        }
    }

    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 800, 800);
            stage.setScene(scene);
            stage.setTitle("Fitness Pro — Вход");
        } catch (Exception e) {
            LOGGER.severe("Error logging out: " + e.getMessage());
            showAlert("Ошибка", "Не удалось выйти");
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
}