package com.fitnesscenter.ui;

import com.fitnesscenter.entity.Sale;
import com.fitnesscenter.entity.Subscription;
import com.fitnesscenter.service.ClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClientSubscriptionsController {

    @FXML private VBox currentSubscriptionCard;
    @FXML private Label currentCardTitleLabel;
    @FXML private Label subNameValue;
    @FXML private Label subStatusValue;
    @FXML private Label subEndDateValue;
    @FXML private Label subPurchaseDateValue;
    @FXML private Label subPriceValue;
    @FXML private Label subVisitsHeaderLabel;
    @FXML private Label subVisitsValue;

    @FXML private Label noSubscriptionMessageLabel;

    @FXML private VBox historyPane;
    @FXML private TableView<Sale> subscriptionsTable;
    @FXML private TableColumn<Sale, String> subNameColumn;
    @FXML private TableColumn<Sale, String> subPurchaseDateColumn;
    @FXML private TableColumn<Sale, String> subEndDateColumn;
    @FXML private TableColumn<Sale, String> subStatusColumn;

    private ClientService clientService;
    private Long currentClientId;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void initializeData(Long clientId, ClientService service) {
        this.currentClientId = clientId;
        this.clientService = service;
        loadSubscriptions();
    }

    @FXML
    public void initialize() {
        currentSubscriptionCard.setVisible(false);
        currentSubscriptionCard.setManaged(false);
        noSubscriptionMessageLabel.setVisible(false);
        noSubscriptionMessageLabel.setManaged(false);
        historyPane.setVisible(false);
        historyPane.setManaged(false);

        subNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSubscriptionName(cellData.getValue())));
        subPurchaseDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartDate() != null ? cellData.getValue().getStartDate().format(dateFormatter) : "N/A"));
        subEndDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate().format(dateFormatter) : "N/A"));
        subStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(determineStatus(cellData.getValue())));

        subscriptionsTable.setPlaceholder(new Label("История абонементов пуста."));
    }

    public void refreshSubscriptionsData() {
        if (clientService != null && currentClientId != null) {
            loadSubscriptions();
        } else {
            showUIState(UIState.ERROR, "Не удалось обновить абонементы: данные клиента или сервис недоступны.");
        }
    }

    private void loadSubscriptions() {
        if (clientService == null || currentClientId == null) {
            showUIState(UIState.ERROR, "Сервис недоступен или ID клиента не указан.");
            return;
        }

        List<Sale> sales = clientService.getClientSubscriptions(currentClientId);

        if (sales == null || sales.isEmpty()) {
            showUIState(UIState.NO_SUBSCRIPTIONS, "У вас пока нет абонементов.");
            return;
        }

        Optional<Sale> primarySubscriptionOpt = findPrimarySubscription(sales);

        if (primarySubscriptionOpt.isPresent()) {
            Sale primarySubscription = primarySubscriptionOpt.get();
            displayCurrentSubscriptionCard(primarySubscription);
            showUIState(UIState.SHOW_CARD, "Абонемент");

            List<Sale> historySales = sales.stream()
                    .filter(s -> !s.getId().equals(primarySubscription.getId()))
                    .sorted(Comparator.comparing(Sale::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            if (!historySales.isEmpty()) {
                subscriptionsTable.setItems(FXCollections.observableArrayList(historySales));
                historyPane.setVisible(true);
                historyPane.setManaged(true);
            } else {
                historyPane.setVisible(false);
                historyPane.setManaged(false);
            }
        } else {
            showUIState(UIState.NO_PRIMARY_SHOW_HISTORY, "Актуальный абонемент не найден. Показана вся история.");
            subscriptionsTable.setItems(FXCollections.observableArrayList(
                    sales.stream().sorted(Comparator.comparing(Sale::getStartDate, Comparator.nullsLast(Comparator.reverseOrder()))).toList()
            ));
        }
    }

    private enum UIState { SHOW_CARD, NO_SUBSCRIPTIONS, NO_PRIMARY_SHOW_HISTORY, ERROR }

    private void showUIState(UIState state, String message) {
        currentSubscriptionCard.setVisible(state == UIState.SHOW_CARD);
        currentSubscriptionCard.setManaged(state == UIState.SHOW_CARD);

        noSubscriptionMessageLabel.setText(message);
        noSubscriptionMessageLabel.setVisible(state == UIState.NO_SUBSCRIPTIONS || state == UIState.ERROR || state == UIState.NO_PRIMARY_SHOW_HISTORY);
        noSubscriptionMessageLabel.setManaged(state == UIState.NO_SUBSCRIPTIONS || state == UIState.ERROR || state == UIState.NO_PRIMARY_SHOW_HISTORY);

        historyPane.setVisible(state == UIState.NO_PRIMARY_SHOW_HISTORY || (state == UIState.SHOW_CARD && !subscriptionsTable.getItems().isEmpty()));
        historyPane.setManaged(state == UIState.NO_PRIMARY_SHOW_HISTORY || (state == UIState.SHOW_CARD && !subscriptionsTable.getItems().isEmpty()));

        if(state == UIState.NO_PRIMARY_SHOW_HISTORY) {
            noSubscriptionMessageLabel.setStyle("-fx-font-style: italic; -fx-font-size: 14px;");
        } else {
            noSubscriptionMessageLabel.setStyle("");
        }
    }


    private Optional<Sale> findPrimarySubscription(List<Sale> sales) {
        LocalDate today = LocalDate.now();

        List<Sale> validSales = sales.stream()
                .filter(s -> s != null && s.getStartDate() != null && s.getEndDate() != null && s.getSubscription() != null)
                .toList();

        Optional<Sale> activeSubscription = validSales.stream()
                .filter(s -> "Активен".equals(determineStatus(s)))
                .max(Comparator.comparing(Sale::getEndDate));
        if (activeSubscription.isPresent()) return activeSubscription;

        Optional<Sale> upcomingSubscription = validSales.stream()
                .filter(s -> "Еще не начался".equals(determineStatus(s)))
                .min(Comparator.comparing(Sale::getStartDate));
        if (upcomingSubscription.isPresent()) return upcomingSubscription;

        return validSales.stream()
                .filter(s -> "Истек".equals(determineStatus(s)))
                .max(Comparator.comparing(Sale::getEndDate));
    }

    private void displayCurrentSubscriptionCard(Sale sale) {
        String status = determineStatus(sale);
        currentCardTitleLabel.setText(getCardTitleForStatus(status));

        subNameValue.setText(getSubscriptionName(sale));
        subStatusValue.setText(status);
        applyStatusStyle(subStatusValue, status);

        subEndDateValue.setText(sale.getEndDate().format(dateFormatter));
        subPurchaseDateValue.setText(sale.getStartDate().format(dateFormatter));

        Subscription subscription = sale.getSubscription();
        if (subscription != null && subscription.getPrice() != null) {
            subPriceValue.setText(String.format("%d руб.", subscription.getPrice()).replace(",", " "));
        } else {
            subPriceValue.setText("N/A");
        }

        if (subscription != null && subscription.getNumberOfVisits() != null && subscription.getNumberOfVisits() > 0) {
            subVisitsHeaderLabel.setVisible(true);
            subVisitsHeaderLabel.setManaged(true);
            subVisitsValue.setVisible(true);
            subVisitsValue.setManaged(true);

            int allowed = subscription.getNumberOfVisits();
            int used = getUsedVisitsForSale(sale);
            int remaining = Math.max(0, allowed - used);

            if (allowed == Integer.MAX_VALUE) {
                subVisitsValue.setText("Без ограничений");
            } else {
                subVisitsValue.setText(String.format("%d / %d", used, allowed));
            }
        } else {
            subVisitsHeaderLabel.setVisible(false);
            subVisitsHeaderLabel.setManaged(false);
            subVisitsValue.setVisible(false);
            subVisitsValue.setManaged(false);
        }
    }

    private int getUsedVisitsForSale(Sale sale) {
        if (sale.getSubscription() != null && sale.getSubscription().getNumberOfVisits() != null && sale.getSubscription().getNumberOfVisits() > 0) {
            return clientService.getRemainingVisits(sale.getId());
        }
        return 0;
    }

    private String getCardTitleForStatus(String status) {
        return switch (status) {
            case "Активен" -> "Текущий Абонемент";
            case "Еще не начался" -> "Предстоящий Абонемент";
            case "Истек" -> "Последний Завершенный Абонемент";
            default -> "Информация об Абонементе";
        };
    }

    private String determineStatus(Sale sale) {
        if (sale == null || sale.getStartDate() == null || sale.getEndDate() == null) return "Ошибка данных";
        LocalDate today = LocalDate.now();
        if (sale.getEndDate().isBefore(today)) {
            return "Истек";
        } else if (sale.getStartDate().isAfter(today)) {
            return "Еще не начался";
        } else {
            return "Активен";
        }
    }

    private String getSubscriptionName(Sale sale) {
        return (sale.getSubscription() != null && sale.getSubscription().getName() != null && !sale.getSubscription().getName().isEmpty())
                ? sale.getSubscription().getName()
                : "Без названия";
    }

    private void applyStatusStyle(Label label, String status) {
        label.getStyleClass().removeAll("status-active", "status-expired", "status-upcoming", "status-error", "status-unknown");
        switch (status) {
            case "Активен" -> label.getStyleClass().add("status-active");
            case "Истек" -> label.getStyleClass().add("status-expired");
            case "Еще не начался" -> label.getStyleClass().add("status-upcoming");
            case "Ошибка данных" -> label.getStyleClass().add("status-error");
            default -> label.getStyleClass().add("status-unknown");
        }
    }
}