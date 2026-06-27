package com.fitnesscenter.ui;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.service.TrainerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TrainerClientsViewController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TrainerClientsViewController.class.getName());

    @Autowired
    private TrainerService trainerService;
    @Autowired
    private ApplicationContext springContext;

    @FXML private TextField searchField;

    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, String> firstNameColumn;
    @FXML private TableColumn<Client, String> lastNameColumn;
    @FXML private TableColumn<Client, String> emailColumn;
    @FXML private TableColumn<Client, String> phoneColumn;
    @FXML private TableColumn<Client, Void> actionsColumn;

    private ObservableList<Client> allClients = FXCollections.observableArrayList();
    private ObservableList<Client> filteredClients = FXCollections.observableArrayList();
    private Trainer currentTrainer; // Тренер, который сейчас вошел в систему

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadClients();
    }

    public void setTrainer(Trainer trainer) {
        this.currentTrainer = trainer;
        loadClients();
    }

    private void setupTableColumns() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("Просмотр");

            {
                viewBtn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    showClientDetailsDialog(client);
                });
                viewBtn.getStyleClass().add("btn-small");
                viewBtn.getStyleClass().add("action-table-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            clientsTable.setItems(allClients);
        } else {
            filteredClients.clear();
            filteredClients.addAll(
                    allClients.stream()
                            .filter(client ->
                                    client.getFirstname().toLowerCase().contains(searchText) ||
                                            client.getLastname().toLowerCase().contains(searchText) ||
                                            client.getEmail().toLowerCase().contains(searchText))
                            .toList()
            );
            clientsTable.setItems(filteredClients);
        }
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        clientsTable.setItems(allClients);
    }

    @FXML
    private void handleRefresh() {
        loadClients();
    }

    private void loadClients() {
        try {
            List<Client> clients = trainerService.getAllClients();
            allClients.clear();
            allClients.addAll(clients);
            clientsTable.setItems(allClients);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Не удалось загрузить список клиентов: ", e);
            showAlert("Ошибка", "Не удалось загрузить список клиентов: " + e.getMessage());
        }
    }

    private void showClientDetailsDialog(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/client_details_dialog.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent parent = loader.load();

            ClientDetailsDialogController controller = loader.getController();
            controller.setClientAndTrainer(client, currentTrainer);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Карточка клиента: " + client.getFirstname() + " " + client.getLastname());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(clientsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(parent));

            controller.setDialogStage(dialogStage);
            dialogStage.setResizable(true);

            dialogStage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при загрузке окна деталей клиента", e);
            showAlert("Ошибка", "Не удалось открыть карточку клиента.");
        }
    }

    @FXML
    private void handleViewClientHistory() {
        if (currentTrainer == null) {
            showAlert("Предупреждение", "Данные тренера не загружены.");
            return;
        }
        showAlert("Информация", "Функция просмотра истории посещений будет реализована позже");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}