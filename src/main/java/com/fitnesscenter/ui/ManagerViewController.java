package com.fitnesscenter.ui;

import com.fitnesscenter.dto.*;
import com.fitnesscenter.entity.*;
import com.fitnesscenter.repository.RegistrationOfVisitRepository;
import com.fitnesscenter.repository.SaleRepository;
import com.fitnesscenter.service.ManagerService;
import com.fitnesscenter.service.ReportService;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ManagerViewController {

    private final ManagerService managerService;
    private final ReportService reportService;
    private final RegistrationOfVisitRepository visitRepository;
    private final ApplicationContext applicationContext;
    private final SaleRepository saleRepository;
    private List<Map<String, Object>> currentReportData;
    private List<String> currentReportHeaders;
    private ReportConfigDto lastGeneratedConfig;

    @FXML private VBox managerBox;
    @FXML private Button logoutButton;

    @FXML private TextField clientSearchField;
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, String> clientLastNameColumn;
    @FXML private TableColumn<Client, String> clientFirstNameColumn;
    @FXML private TableColumn<Client, String> clientPatronymicColumn;
    @FXML private TableColumn<Client, String> clientEmailColumn;
    @FXML private TableColumn<Client, String> clientPhoneColumn;
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();
    @FXML private Pagination clientPagination;

    @FXML private TextField saleSearchField;
    @FXML private TableView<Sale> saleTable;
    @FXML private TableColumn<Sale, String> saleClientColumn;
    @FXML private TableColumn<Sale, String> saleSubscriptionColumn;
    @FXML private TableColumn<Sale, String> saleFitnessCenterColumn;
    @FXML private TableColumn<Sale, LocalDate> saleStartDateColumn;
    @FXML private TableColumn<Sale, LocalDate> saleEndDateColumn;
    @FXML private TableColumn<Sale, BigDecimal> salePriceColumn;
    @FXML private TableColumn<Sale, String> saleDiscountColumn;
    private final ObservableList<Sale> saleData = FXCollections.observableArrayList();
    @FXML private Pagination salePagination;

    @FXML private TableView<Trainer> trainerTable;
    @FXML private TableColumn<Trainer, Long> trainerIdColumn;
    @FXML private TableColumn<Trainer, String> trainerFullNameColumn;
    @FXML private DatePicker scheduleDateFilterPicker;
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, Long> scheduleIdColumn;
    @FXML private TableColumn<Schedule, java.sql.Date> scheduleDateColumn;
    @FXML private TableColumn<Schedule, Time> scheduleStartTimeColumn;
    @FXML private TableColumn<Schedule, Time> scheduleEndTimeColumn;
    @FXML private TableColumn<Schedule, String> scheduleClientColumn;
    private final ObservableList<Trainer> trainerData = FXCollections.observableArrayList();
    private final ObservableList<Schedule> scheduleData = FXCollections.observableArrayList();

    @FXML private ComboBox<String> reportEntityComboBox;
    @FXML private DatePicker reportDateFromPicker;
    @FXML private DatePicker reportDateToPicker;
    @FXML private ComboBox<String> sortFieldCombo;
    @FXML private ComboBox<String> sortOrderCombo;
    @FXML private ComboBox<String> reportOutputFormatComboBox;
    @FXML private TextField companyNameField;
    @FXML private Button printReportButton;
    @FXML private TextArea reportStatusArea;
    @FXML private Button viewReportButton;
    @FXML private TreeView<String> fieldsTreeView;
    @FXML private ListView<String> selectedFieldsListView;
    @FXML private ListView<String> groupByListView;
    @FXML private ComboBox<FieldTranslation> filterFieldCombo;
    @FXML private ComboBox<String> filterOperatorCombo;
    @FXML private TextField filterValueField;
    @FXML private ListView<String> activeFiltersListView;

    private final Map<String, List<String>> entityDisplayFieldsMap = new HashMap<>();
    private final Map<String, List<String>> entityDisplayAggregatesMap = new HashMap<>();
    private List<String> sortTechnicalNames = new ArrayList<>();

    private static final Map<String, String> FIELD_DISPLAY_NAMES = Map.ofEntries(
            Map.entry("firstname", "Имя"),
            Map.entry("client_firstname", "Имя клиента"),
            Map.entry("lastname", "Фамилия"),
            Map.entry("client_lastname", "Фамилия клиента"),
            Map.entry("patronymic", "Отчество"),
            Map.entry("phone", "Телефон"),
            Map.entry("gender", "Пол"),
            Map.entry("email", "Email"),
            Map.entry("passport", "Паспорт"),

            Map.entry("startDate", "Дата начала абонемента"),
            Map.entry("endDate", "Дата окончания абонемента"),
            Map.entry("bankCardNum", "Номер банковской карты"),
            Map.entry("subscription.name", "Абонемент"),
            Map.entry("subscription.price", "Цена абонемента"),
            Map.entry("subscription_price", "Цена абонемента"),
            Map.entry("discount.name", "Название скидки"),
            Map.entry("discount_name", "Название скидки"),
            Map.entry("discount.percentage", "Размер скидки"),
            Map.entry("discount_percentage", "Размер скидки"),
            Map.entry("fitnessCenter.name", "Фитнес-центр"),
            Map.entry("fitnessCenter_name", "Фитнес-центр"),
            Map.entry("client.lastname", "Фамилия клиента"),
            Map.entry("client.firstname", "Имя клиента"),

            Map.entry("name", "Название абонемента"),
            Map.entry("price", "Стоимость абонемента"),
            Map.entry("numberOfVisits", "Количество посещений"),

            Map.entry("schedule.date", "Дата занятия"),
            Map.entry("schedule.startTime", "Время начала"),
            Map.entry("schedule.endTime", "Время окончания"),
            Map.entry("schedule.trainer.lastname as trainerLastname", "Фамилия тренера"),
            Map.entry("sale.client.lastname as clientLastname", "Фамилия клиента"),
            Map.entry("sale.subscription.name as subscriptionName", "Абонемент"),
            Map.entry("schedule.trainer.firstname", "Имя тренера"),
            Map.entry("schedule.trainer.patronymic", "Отчество тренера"),
            Map.entry("schedule.date as scheduleDate", "Дата посещения"),
            Map.entry("visitDate", "Дата посещения"),

            Map.entry("COUNT(id) as totalClients", "Всего клиентов"),
            Map.entry("COUNT(id) as totalSales", "Всего продаж"),
            Map.entry("SUM(subscription.price) as revenueBySubscriptionPrice", "Общая выручка по абонементам"),
            Map.entry("AVG(subscription.price) as averageSubscriptionPrice", "Средняя стоимость абонемента"),
            Map.entry("COUNT(id) as totalVisits", "Всего посещений"),
            Map.entry("totalSales", "Всего продаж"),
            Map.entry("totalClients", "Всего клиентов"),
            Map.entry("revenueBySubscriptionPrice","Общая выручка по абонементам"),
            Map.entry("averageSubscriptionPrice", "Средняя стоимость абонемента")
    );

    @Autowired
    public ManagerViewController(ManagerService managerService, ReportService reportService,
                                 RegistrationOfVisitRepository visitRepository, ApplicationContext applicationContext, SaleRepository saleRepository) {
        this.managerService = managerService;
        this.reportService = reportService;
        this.visitRepository = visitRepository;
        this.applicationContext = applicationContext;
        this.saleRepository = saleRepository;
    }

    @FXML
    public void initialize() {
        setupClientTab();
        setupSaleTab();
        setupTrainerScheduleTab();
        setupReportingTab();
        loadClientsPage(0);
        loadSalesPage(0);
        loadTrainers();
        clientSearchField.textProperty().addListener((obs, oldVal, newVal) -> loadClientsPage(0));
        saleSearchField.textProperty().addListener((obs, oldVal, newVal) -> loadSalesPage(0));
        if (viewReportButton != null) {
            viewReportButton.setDisable(true);
        }
        printReportButton.setDisable(true);

        addHoverScaleAnimation(logoutButton);
        addHoverScaleAnimation(printReportButton);
        addHoverScaleAnimation(viewReportButton);

        selectedFieldsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        groupByListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        groupByListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FIELD_DISPLAY_NAMES.getOrDefault(item, item));
                }
            }
        });

        selectedFieldsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String technicalNameForLookup = item;

                    int lastParenOpen = item.lastIndexOf("(");
                    int lastParenClose = item.lastIndexOf(")");
                    if (lastParenOpen != -1 && lastParenClose == item.length() - 1 && lastParenOpen < lastParenClose) {
                        String potentialTechnicalName = item.substring(lastParenOpen + 1, lastParenClose);
                        if (FIELD_DISPLAY_NAMES.containsKey(potentialTechnicalName)) {
                            technicalNameForLookup = potentialTechnicalName;
                        }
                    }
                    setText(FIELD_DISPLAY_NAMES.getOrDefault(technicalNameForLookup, item));
                }
            }
        });
    }

    private void setupClientTab() {
        clientLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        clientFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        clientPatronymicColumn.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        clientEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        clientPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientTable.setItems(clientData);

        clientPagination.setCurrentPageIndex(0);
        clientPagination.setPageCount(1);
        clientPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            loadClientsPage(newIndex.intValue());
        });
    }

    @FXML private void handleClientSearch() { loadClientsPage(0); }
    @FXML private void refreshClientTable() { loadClientsPage(clientPagination.getCurrentPageIndex());
    }

    private void loadClientsPage(int pageIndex) {
        ClientFilterRequest filter = getClientFilterRequest();
        int pageSize = 15;

        Task<Page<Client>> task = new Task<>() {
            @Override
            protected Page<Client> call() {
                return managerService.getAllClientsWithFilter(filter, pageIndex, pageSize);
            }
        };
        task.setOnSucceeded(event -> {
            Page<Client> clientPage = task.getValue();
            if (clientTable != null) clientTable.setOpacity(0);
            clientData.setAll(clientPage.getContent());
            clientPagination.setPageCount(clientPage.getTotalPages());
            clientPagination.setCurrentPageIndex(clientPage.getNumber());
            if (!clientPage.getContent().isEmpty()) {
                clientTable.scrollTo(0);
                fadeInNode(clientTable, 500);
            } else{
                if (clientTable != null) clientTable.setOpacity(1);
            }
        });
        task.setOnFailed(event -> {
            handleTaskFailure("загрузки клиентов", task.getException());
            if(clientTable != null) clientTable.setOpacity(1);
        });
        new Thread(task).start();
    }

    private ClientFilterRequest getClientFilterRequest() {
        ClientFilterRequest filter = new ClientFilterRequest();
        filter.setSearchQuery(clientSearchField.getText().trim());
        filter.setSortField("lastname");
        filter.setSortOrder("asc");
        return filter;
    }

    @FXML
    private void handleEditClient() {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            showEditDialog("/ui/client-edit-dialog.fxml", "Редактировать клиента", selectedClient, controller -> {
                ((ClientEditDialogController) controller).setClient(selectedClient);
            }, this::refreshClientTable);
        } else {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Пожалуйста, выберите клиента для редактирования.");
        }
    }

    private void setupSaleTab() {
        saleClientColumn.setCellValueFactory(cellData ->
                Optional.ofNullable(cellData.getValue().getClient())
                        .map(c -> c.getLastname() + " " + c.getFirstname())
                        .map(javafx.beans.property.SimpleStringProperty::new)
                        .orElse(new javafx.beans.property.SimpleStringProperty("-")));
        saleSubscriptionColumn.setCellValueFactory(cellData ->
                Optional.ofNullable(cellData.getValue().getSubscription())
                        .map(Subscription::getName)
                        .map(javafx.beans.property.SimpleStringProperty::new)
                        .orElse(new javafx.beans.property.SimpleStringProperty("-")));
        saleFitnessCenterColumn.setCellValueFactory(cellData ->
                Optional.ofNullable(cellData.getValue().getFitnessCenter())
                        .map(FitnessCenter::getName)
                        .map(SimpleStringProperty::new)
                        .orElse(new SimpleStringProperty("-")));
        saleStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        saleEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        salePriceColumn.setCellValueFactory(cellData -> {
            Sale sale = cellData.getValue();
            if (sale.getSubscription() != null && sale.getSubscription().getPrice() != null) {
                BigDecimal originalPrice = BigDecimal.valueOf(sale.getSubscription().getPrice());
                BigDecimal finalPrice = originalPrice;
                if (sale.getDiscount() != null && sale.getDiscount().getPercentage() != null) {
                    BigDecimal percentage = BigDecimal.valueOf(sale.getDiscount().getPercentage());
                    BigDecimal discountAmount = originalPrice.multiply(percentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    finalPrice = originalPrice.subtract(discountAmount);
                }
                return new javafx.beans.property.SimpleObjectProperty<>(finalPrice);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(BigDecimal.ZERO);
        });

        saleDiscountColumn.setCellValueFactory(cellData ->
                Optional.ofNullable(cellData.getValue().getDiscount())
                        .map(d -> d.getName() + " (" + d.getPercentage() + "%)")
                        .map(javafx.beans.property.SimpleStringProperty::new)
                        .orElse(new javafx.beans.property.SimpleStringProperty("Нет")));
        saleTable.setItems(saleData);

        salePagination.setCurrentPageIndex(0);
        salePagination.setPageCount(1);
        salePagination.currentPageIndexProperty().addListener((obs, oldI, newI) -> loadSalesPage(newI.intValue()));
    }

    @FXML private void refreshSaleTable() { loadSalesPage(salePagination.getCurrentPageIndex()); }
    @FXML private void handleSaleSearch() { loadSalesPage(0); }

    private void loadSalesPage(int pageIndex) {
        SaleFilterRequest filter = new SaleFilterRequest();
        filter.setSearchTerm(saleSearchField.getText());
        filter.setSortField("startDate");
        filter.setSortOrder("desc");
        int pageSize = 15;
        Task<Page<Sale>> task = new Task<>() {
            @Override protected Page<Sale> call() {
                return managerService.getAllSalesWithFilter(filter, pageIndex, pageSize);
            }
        };
        task.setOnSucceeded(e -> {
            Page<Sale> salePage = task.getValue();
            saleData.setAll(salePage.getContent());
            salePagination.setPageCount(salePage.getTotalPages());
            salePagination.setCurrentPageIndex(salePage.getNumber());
            if (!salePage.getContent().isEmpty()) saleTable.scrollTo(0);
        });
        task.setOnFailed(e -> handleTaskFailure("загрузки продаж", task.getException()));
        new Thread(task).start();
    }

    @FXML
    private void handleCreateSale() {
        showEditDialog("/ui/sale-create-dialog.fxml", "Оформить продажу", null, controller -> {
            List<Client> clients = managerService.getAllClients();
            List<Subscription> subscriptions = managerService.getAllSubscriptions();
            List<Discount> discounts = managerService.getAllDiscounts();
            List<FitnessCenter> centers = managerService.getAllFitnessCenters();
            ((SaleCreateDialogController) controller).initData(clients, subscriptions, discounts, centers, managerService);
        }, this::refreshSaleTable);
    }

    private void setupTrainerScheduleTab() {
        trainerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        trainerFullNameColumn.setCellValueFactory(cellData ->
                Optional.ofNullable(cellData.getValue())
                        .map(t -> t.getLastname() + " " + t.getFirstname() + (t.getPatronymic() != null ? " " + t.getPatronymic() : ""))
                        .map(javafx.beans.property.SimpleStringProperty::new)
                        .orElse(new javafx.beans.property.SimpleStringProperty("")));
        trainerTable.setItems(trainerData);

        scheduleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        scheduleDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        scheduleStartTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        scheduleEndTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        scheduleClientColumn.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            List<RegistrationOfVisit> visits = visitRepository.findByScheduleId(schedule.getId());
            if (!visits.isEmpty()) {
                RegistrationOfVisit visit = visits.get(0);
                Sale sale = saleRepository.findById(visit.getSaleId()).orElse(null);
                if (sale != null && sale.getClient() != null) {
                    String clientName = sale.getClient().getFirstname() + " " + sale.getClient().getLastname();
                    return new javafx.beans.property.SimpleStringProperty(clientName);
                }
            }
            return new javafx.beans.property.SimpleStringProperty("Свободно");
        });
        scheduleTable.setItems(scheduleData);

        trainerTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> loadTrainerSchedule(newSelection));
    }

    private void loadTrainers() {
        Task<List<Trainer>> task = new Task<>() {
            @Override protected List<Trainer> call() { return managerService.getAllTrainers(); }
        };
        task.setOnSucceeded(e -> trainerData.setAll(task.getValue()));
        task.setOnFailed(e -> handleTaskFailure("загрузки тренеров", task.getException()));
        new Thread(task).start();
    }

    private void loadTrainerSchedule(Trainer trainer) {
        scheduleData.clear();
        if (trainer == null) return;
        LocalDate filterDate = scheduleDateFilterPicker.getValue();

        Task<List<Schedule>> task = new Task<>() {
            @Override protected List<Schedule> call() {
                List<Schedule> schedules = managerService.getScheduleByTrainerId(Long.valueOf(trainer.getId()));
                if (filterDate != null) {
                    return schedules.stream()
                            .filter(s -> s.getDate() != null && s.getDate().equals(filterDate))
                            .collect(Collectors.toList());
                }
                return schedules;
            }
        };
        task.setOnSucceeded(e -> {
            scheduleData.setAll(task.getValue());
            if (!scheduleData.isEmpty()) scheduleTable.scrollTo(0);
        });
        task.setOnFailed(e -> handleTaskFailure("загрузки расписания", task.getException()));
        new Thread(task).start();
    }

    @FXML private void handleFilterTrainerScheduleByDate() {
        Trainer selectedTrainer = trainerTable.getSelectionModel().getSelectedItem();
        loadTrainerSchedule(selectedTrainer);
    }

    @FXML
    private void handleAddScheduleEntry() {
        Trainer selectedTrainer = trainerTable.getSelectionModel().getSelectedItem();
        if (selectedTrainer == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Выберите тренера для добавления расписания.");
            return;
        }
        showEditDialog("/ui/schedule-add-dialog.fxml", "Добавить занятие", null, controller -> {
            ((ScheduleAddDialogController) controller).initData(selectedTrainer, managerService);
        }, () -> loadTrainerSchedule(selectedTrainer));
    }

    @FXML
    private void handleAssignClientToTrainer() {
        Trainer selectedTrainer = trainerTable.getSelectionModel().getSelectedItem();
        if (selectedTrainer == null) {
            showAlert(Alert.AlertType.WARNING, "Тренер не выбран", "Пожалуйста, выберите тренера.");
            return;
        }

        List<Schedule> availableSlots = managerService.getAvailableFutureSlots(Long.valueOf(selectedTrainer.getId()));
        if (availableSlots.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Нет доступных слотов", "Для выбранного тренера нет свободных будущих слотов.");
            return;
        }

        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Запись клиента к тренеру");
        dialog.setHeaderText("Выберите клиента и слот для записи к тренеру: " + selectedTrainer.getFullName());

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по имени или телефону...");
        searchField.getStyleClass().add("text-input");

        TableView<Client> clientTable = new TableView<>();
        clientTable.getStyleClass().add("table-view");

        TableColumn<Client, String> nameColumn = new TableColumn<>("ФИО");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        nameColumn.setPrefWidth(200);

        TableColumn<Client, String> phoneColumn = new TableColumn<>("Телефон");
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        phoneColumn.setPrefWidth(150);

        clientTable.getColumns().addAll(nameColumn, phoneColumn);

        ObservableList<Client> allClients = FXCollections.observableList(managerService.getAllClients());
        FilteredList<Client> filteredClients = new FilteredList<>(allClients, p -> true);
        SortedList<Client> sortedClients = new SortedList<>(filteredClients);
        clientTable.setItems(sortedClients); // Связываем таблицу с отфильтрованным списком

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredClients.setPredicate(client -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return client.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                        client.getPhone().contains(newVal);
            });
        });

        ComboBox<Schedule> scheduleComboBox = new ComboBox<>(FXCollections.observableList(availableSlots));
        scheduleComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Schedule schedule, boolean empty) {
                super.updateItem(schedule, empty);
                if (schedule != null && !empty) {
                    setText("Дата: " + schedule.getDate() + ", Время: " + schedule.getStartTime() + "-" + schedule.getEndTime());
                } else {
                    setText("");
                }
            }
        });
        scheduleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Schedule schedule, boolean empty) {
                super.updateItem(schedule, empty);
                if (schedule != null && !empty) {
                    setText("Дата: " + schedule.getDate() + ", Время: " + schedule.getStartTime() + "-" + schedule.getEndTime());
                } else {
                    setText("Выберите слот");
                }
            }
        });

        VBox content = new VBox(10,
                new Label("Клиент:"), searchField, clientTable,
                new Label("Слот:"), scheduleComboBox
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().addAll("btn-primary", "btn-small");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().addAll("btn-secondary", "btn-small");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
                Schedule selectedSchedule = scheduleComboBox.getValue();
                if (selectedClient == null) {
                    showAlert(Alert.AlertType.WARNING, "Ошибка", "Не выбран клиент.");
                    return null;
                }
                if (selectedSchedule == null) {
                    showAlert(Alert.AlertType.WARNING, "Ошибка", "Не выбран слот.");
                    return null;
                }
                return selectedClient;
            }
            return null;
        });

        dialog.setOnShowing(event -> {
            Scene scene = dialog.getDialogPane().getScene();
            String cssPath = getClass().getResource("/ui/style.css").toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            }
        });

        dialog.showAndWait().ifPresent(client -> {
            try {
                managerService.assignClientToTrainer(
                        Long.valueOf(selectedTrainer.getId()),
                        client.getId(),
                        scheduleComboBox.getValue().getId()
                );
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Клиент " + client.getFullName() + " записан к тренеру.");
                loadTrainerSchedule(selectedTrainer);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка записи", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleCancelVisit() {
        Schedule selectedSchedule = scheduleTable.getSelectionModel().getSelectedItem();
        if (selectedSchedule == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Выберите занятие для отмены.");
            return;
        }

        List<RegistrationOfVisit> visits = visitRepository.findByScheduleId(selectedSchedule.getId());
        if (!visits.isEmpty()) {
            RegistrationOfVisit visitToCancel = visits.get(0);
            Sale sale = saleRepository.findById(visitToCancel.getSaleId()).orElse(null);
            String clientName = (sale != null && sale.getClient() != null) ? sale.getClient().getFullName() : "Неизвестный клиент";
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "Отменить визит клиента " + clientName +
                            " на " + selectedSchedule.getDate() + " " + selectedSchedule.getStartTime() + "?",
                    ButtonType.YES, ButtonType.NO);
            confirmation.setTitle("Подтверждение отмены");
            confirmation.setHeaderText("Отмена визита");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        managerService.cancelClientVisit(visitToCancel.getId());
                        showAlert(Alert.AlertType.INFORMATION, "Успех", "Визит успешно отменен.");
                        loadTrainerSchedule(trainerTable.getSelectionModel().getSelectedItem());
                    } catch (RuntimeException ex) {
                        showAlert(Alert.AlertType.ERROR, "Ошибка отмены", ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Информация", "Этот слот в расписании не является зарегистрированным визитом клиента или визит уже отменен.");
        }
    }

    private void setupReportingTab() {
        reportEntityComboBox.setItems(FXCollections.observableArrayList("Продажи", "Клиенты", "Посещения"));
        sortOrderCombo.setItems(FXCollections.observableArrayList("ASC", "DESC"));
        reportOutputFormatComboBox.setItems(FXCollections.observableArrayList("На экране", "Excel", "Word"));
        reportOutputFormatComboBox.getSelectionModel().selectFirst();
        companyNameField.setText("ООО Фитнес ПРО");

        fieldsTreeView.setCellFactory(tv -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<String> treeItem = getTreeItem();

                if (empty || item == null || treeItem == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Node graphic = treeItem.getGraphic();
                    if (graphic != null) {
                        setGraphic(graphic);
                        setText(null);
                    } else {
                        setGraphic(null);
                        setText(item);
                    }
                }
            }
        });

        entityDisplayFieldsMap.put("Продажи", Arrays.asList(
                "startDate", "endDate", "bankCardNum",
                "client.lastname", "client.firstname",
                "subscription.name", "subscription.price",
                "discount.name", "discount.percentage",
                "fitnessCenter.name"
        ));
        entityDisplayAggregatesMap.put("Продажи", Arrays.asList(
                "COUNT(id) as totalSales",
                "SUM(subscription.price) as revenueBySubscriptionPrice",
                "AVG(subscription.price) as averageSubscriptionPrice"
        ));
        entityDisplayFieldsMap.put("Клиенты", Arrays.asList("id", "firstname", "lastname", "patronymic", "phone", "gender", "email", "passport"));
        entityDisplayAggregatesMap.put("Клиенты", List.of("COUNT(id) as totalClients"));
        entityDisplayFieldsMap.put("Посещения", Arrays.asList(
                "visitDate", "schedule.date as scheduleDate",
                "schedule.trainer.lastname as trainerLastname",
                "sale.client.lastname as clientLastname",
                "sale.subscription.name as subscriptionName"
        ));
        entityDisplayAggregatesMap.put("Посещения", List.of("COUNT(id) as totalVisits"));

        reportEntityComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateFieldTree(newVal);
            selectedFieldsListView.getItems().clear();
            groupByListView.getItems().clear();
        });
        if (!reportEntityComboBox.getItems().isEmpty()) {
            reportEntityComboBox.getSelectionModel().selectFirst();
            updateFieldTree(reportEntityComboBox.getValue());
        }

        selectedFieldsListView.getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (String addedItem : c.getAddedSubList()) {
                        setCheckBoxStateInTreeView(addedItem, true, false);
                    }
                }
            }
            updateSortByComboBoxWithSelectedAndGroupFields();
        });

        groupByListView.getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            updateSortByComboBoxWithSelectedAndGroupFields();
        });

        setupDragDrop();
        initFilterControls();

    }

    private void updateFieldTree(String entityName) {
        TreeItem<String> rootItem = new TreeItem<>("Доступные поля");
        rootItem.setExpanded(true);

        if (entityName == null || entityName.isEmpty()) {
            fieldsTreeView.setRoot(rootItem);
            fieldsTreeView.setShowRoot(false);
            return;
        }

        List<String> fields = entityDisplayFieldsMap.getOrDefault(entityName, new ArrayList<>());
        List<String> aggregates = entityDisplayAggregatesMap.getOrDefault(entityName, new ArrayList<>());

        List<String> filteredFields = fields.stream()
                .filter(field -> !field.equals("id"))
                .toList();

        TreeItem<String> fieldsCategoryItem = new TreeItem<>("Поля");
        fieldsCategoryItem.setExpanded(true);
        filteredFields.forEach(field -> fieldsCategoryItem.getChildren().add(createCheckBoxTreeItem(field)));
        if (!filteredFields.isEmpty()) rootItem.getChildren().add(fieldsCategoryItem);

        TreeItem<String> aggregatesCategoryItem = new TreeItem<>("Агрегаты");
        aggregatesCategoryItem.setExpanded(true);
        aggregates.forEach(agg -> aggregatesCategoryItem.getChildren().add(createCheckBoxTreeItem(agg)));
        if (!aggregates.isEmpty()) rootItem.getChildren().add(aggregatesCategoryItem);

        fieldsTreeView.setRoot(rootItem);
        fieldsTreeView.setShowRoot(false);
    }


    private void updateSortByComboBoxWithSelectedAndGroupFields() {
        List<String> selectedForSelect = new ArrayList<>(selectedFieldsListView.getItems());
        List<String> selectedForGroupBy = new ArrayList<>(groupByListView.getItems());

        List<String> availableForSortTechnical = new ArrayList<>();
        List<String> availableForSortLocalized = new ArrayList<>();

        selectedForSelect.forEach(item -> {
            String technicalName = parseAliasFromFieldString(item);
            if (!availableForSortTechnical.contains(technicalName)) {
                availableForSortTechnical.add(technicalName);
                String displayName = FIELD_DISPLAY_NAMES.getOrDefault(technicalName, technicalName);
                availableForSortLocalized.add(displayName);
            }
        });

        selectedForGroupBy.forEach(item -> {
            String technicalName = parseAliasFromFieldString(item);
            if (!availableForSortTechnical.contains(technicalName)) {
                availableForSortTechnical.add(technicalName);
                String displayName = FIELD_DISPLAY_NAMES.getOrDefault(technicalName, technicalName);
                availableForSortLocalized.add(displayName);
            }
        });

        if (availableForSortTechnical.isEmpty() && reportEntityComboBox.getValue() != null
                && entityDisplayFieldsMap.containsKey(reportEntityComboBox.getValue())) {

            entityDisplayFieldsMap.get(reportEntityComboBox.getValue()).forEach(field -> {
                String technicalName = parseAliasFromFieldString(field);
                if (!availableForSortTechnical.contains(technicalName)) {
                    availableForSortTechnical.add(technicalName);
                    String displayName = FIELD_DISPLAY_NAMES.getOrDefault(technicalName, technicalName);
                    availableForSortLocalized.add(displayName);
                }
            });
        }

        this.sortTechnicalNames = availableForSortTechnical;

        String previouslySelectedDisplayName = sortFieldCombo.getValue();
        sortFieldCombo.setItems(FXCollections.observableArrayList(availableForSortLocalized));

        if (previouslySelectedDisplayName != null && availableForSortLocalized.contains(previouslySelectedDisplayName)) {
            sortFieldCombo.setValue(previouslySelectedDisplayName);
        } else if (!availableForSortLocalized.isEmpty()) {
            sortFieldCombo.getSelectionModel().selectFirst();
        } else {
            sortFieldCombo.getSelectionModel().clearSelection();
            sortFieldCombo.setValue(null);
        }
    }

    private ReportConfigDto.SelectFieldDto parseSelectedFieldString(String selectedString) {
        Pattern aggregatePattern = Pattern.compile("(\\w+)\\(([^)]+)\\)(\\s+as\\s+(\\w+))?");
        Matcher matcher = aggregatePattern.matcher(selectedString);

        String fieldPath;
        String aggregateFunction = null;
        String alias = null;
        if (matcher.matches()) {
            aggregateFunction = matcher.group(1).trim().toUpperCase();
            fieldPath = matcher.group(2).trim();
            if (matcher.group(4) != null) {
                alias = matcher.group(4).trim();
            } else {
                alias = (aggregateFunction + "_" + fieldPath.replace(".", "_")).toLowerCase();
            }
        } else {
            fieldPath = selectedString.trim();
            if (selectedString.toLowerCase().contains(" as ")) {
                alias = selectedString.substring(selectedString.toLowerCase().indexOf(" as ") + 4).trim();
                fieldPath = selectedString.substring(0, selectedString.toLowerCase().indexOf(" as ")).trim();
            } else {
                alias = fieldPath.replace(".", "_");
            }
        }
        return new ReportConfigDto.SelectFieldDto(fieldPath, aggregateFunction, alias);
    }

    private String parseAliasFromFieldString(String selectedString) {
        Pattern aliasPattern = Pattern.compile(".*\\s+as\\s+(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = aliasPattern.matcher(selectedString);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        if(selectedString.contains("(")) {
            return selectedString.replace(".", "_").replace("(", "_").replace(")", "").toLowerCase();
        }
        return selectedString.replace(".", "_");
    }


    @FXML
    private void handleGenerateReport() {

        List<String> havingFilters = activeFiltersListView.getItems();

        boolean hasNonAggregatedField = selectedFieldsListView.getItems().stream()
                .anyMatch(item -> !item.contains("SUM(") && !item.contains("COUNT(") && !item.contains("AVG(")); // и т.д.

        List<String> havingConditionsError = activeFiltersListView.getItems();

        if (!hasNonAggregatedField && !havingConditionsError.isEmpty()) {
            reportStatus("Нельзя использовать HAVING без группировки. Добавьте хотя бы одно поле для группировки!");
            return;
        }

        ReportConfigDto config = new ReportConfigDto();
        String selectedEntityDisplay = reportEntityComboBox.getValue();
        if (selectedEntityDisplay == null || selectedEntityDisplay.isEmpty()) {
            reportStatus("Ошибка: Сущность для отчета не выбрана.");
            return;
        }

        switch (selectedEntityDisplay) {
            case "Продажи" -> config.setEntityName("Sale");
            case "Клиенты" -> config.setEntityName("Client");
            case "Посещения" -> config.setEntityName("RegistrationOfVisit");
            default -> {
                reportStatus("Ошибка: Неизвестный тип сущности: " + selectedEntityDisplay);
                return;
            }
        }
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        config.setReportName("Отчет от " + formattedDate);
        config.setCompanyName(companyNameField.getText());

        List<ReportConfigDto.SelectFieldDto> selectFields = selectedFieldsListView.getItems().stream()
                .map(this::parseSelectedFieldString)
                .collect(Collectors.toList());
        if (selectFields.isEmpty()) {
            reportStatus("Ошибка: Поля для отображения (SELECT) не выбраны.");
            return;
        }
        config.setSelectFields(selectFields);
        config.setGroupByFields(new ArrayList<>(groupByListView.getItems()));

        config.setDateFrom(reportDateFromPicker.getValue());
        config.setDateTo(reportDateToPicker.getValue());


        List<ReportConfigDto.HavingConditionDto> havingConditions = new ArrayList<>();

        Pattern havingPattern = Pattern.compile("(\\w+)\\s*([><=!]+)\\s*(\\S+)");
        for (String havingText : havingFilters) {
            if (havingText != null && !havingText.trim().isEmpty()) {
                Matcher matcher = havingPattern.matcher(havingText.trim());
                if (matcher.matches()) {
                    String alias = matcher.group(1);
                    String operator = matcher.group(2);
                    String valueStr = matcher.group(3);
                    valueStr = valueStr.replace("'", "").replace("\"", "");
                    Object value = valueStr;
                    try { value = Long.parseLong(valueStr); }
                    catch (NumberFormatException e) {
                        try { value = Double.parseDouble(valueStr); }
                        catch (NumberFormatException ignored) {}
                    }

                    String dtoOperator = convertOperator(operator);
                    if (dtoOperator != null) {
                        havingConditions.add(new ReportConfigDto.HavingConditionDto(alias, dtoOperator, value));
                    } else {
                        reportStatus("Предупреждение: Неизвестный оператор в HAVING: " + operator);
                    }
                } else {
                    reportStatus("Предупреждение: Неверный формат HAVING. Используйте 'алиас ОПЕРАТОР значение'.");
                }
            }
        }
        if (!havingConditions.isEmpty()) {
            config.setHavingConditions(havingConditions);
        }


        String sortByFieldDisplayName = sortFieldCombo.getValue();
        if (sortByFieldDisplayName != null && !sortByFieldDisplayName.isEmpty()) {
            int selectedIndex = sortFieldCombo.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < sortTechnicalNames.size()) {
                String sortByFieldTechnicalName = sortTechnicalNames.get(selectedIndex);
                String sortOrder = Optional.ofNullable(sortOrderCombo.getValue()).orElse("ASC");
                config.setSortOrders(Collections.singletonList(
                        new ReportConfigDto.SortOrderDto(sortByFieldTechnicalName, sortOrder)
                ));
            } else {
                System.err.println("Неверный индекс выбранного поля сортировки");
            }
        }

        String outputFormatSelection = reportOutputFormatComboBox.getValue();
        if ("Excel".equals(outputFormatSelection)) config.setOutputFormat("excel");
        else if ("Word".equals(outputFormatSelection)) config.setOutputFormat("word");
        else config.setOutputFormat("json");

        reportStatusArea.setText("Генерация отчета для '" + selectedEntityDisplay + "'...");
        printReportButton.setDisable(true);
        if (viewReportButton != null) {
            viewReportButton.setDisable(true);
        }
        currentReportData = null;
        currentReportHeaders = null;
        lastGeneratedConfig = config;

        Task<Object> reportTask = createReportGenerationTask(config);

        reportTask.setOnSucceeded(event -> {
            Object result = reportTask.getValue();
            if (config.getOutputFormat().equals("json")) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> reportMap = (Map<String, Object>) result;

                    List<String> rawHeaders = (List<String>) reportMap.get("headers");
                    List<String> translatedHeaders = rawHeaders.stream()
                            .map(this::translateField)
                            .toList();

                    List<Map<String, Object>> rawData = (List<Map<String, Object>>) reportMap.get("data");
                    List<Map<String, Object>> translatedData = rawData.stream()
                            .map(row -> {
                                Map<String, Object> translatedRow = new LinkedHashMap<>();
                                row.forEach((key, value) ->
                                        translatedRow.put(translateField(key), value));
                                return translatedRow;
                            })
                            .toList();

                    currentReportHeaders = translatedHeaders;
                    currentReportData = translatedData;

                    if (currentReportData != null && !currentReportData.isEmpty()) {
                        reportStatusArea.setText(config.getReportName() + " успешно сформирован. Нажмите 'Просмотреть отчёт'.");
                        printReportButton.setDisable(false);
                        if (viewReportButton != null) {
                            viewReportButton.setDisable(false);
                        }
                    } else {
                        reportStatusArea.setText(config.getReportName() + " не содержит данных.");
                        printReportButton.setDisable(true);
                        if (viewReportButton != null) {
                            viewReportButton.setDisable(true);
                        }
                    }
                } else {
                    reportStatusArea.setText("Ошибка: Неверный формат данных отчета для отображения.");
                    printReportButton.setDisable(true);
                    if (viewReportButton != null) {
                        viewReportButton.setDisable(true);
                    }
                }
            } else {
                if (result instanceof byte[]) {
                    reportStatusArea.setText(config.getReportName() + " успешно сгенерирован в файл. Нажмите 'Экспорт' для сохранения.");
                    printReportButton.setDisable(false);
                    if (viewReportButton != null) {
                        viewReportButton.setDisable(true);
                    }
                } else {
                    reportStatusArea.setText("Ошибка генерации файла отчета.");
                    printReportButton.setDisable(true);
                    if (viewReportButton != null) {
                        viewReportButton.setDisable(true);
                    }
                }
            }
        });
        reportTask.setOnFailed(event -> {
            Throwable exception = reportTask.getException();
            reportStatusArea.setText("Ошибка генерации отчета: " + exception.getMessage());
            exception.printStackTrace();
            printReportButton.setDisable(true);
            if (viewReportButton != null) {
                viewReportButton.setDisable(true);
            }
        });
        new Thread(reportTask).start();
    }

    private String translateField(String technicalName) {
        return FIELD_DISPLAY_NAMES.getOrDefault(technicalName, technicalName);
    }

    private String convertOperator(String op) {
        return switch (op) {
            case "=" -> "EQUALS";
            case "!=" -> "NOT_EQUALS";
            case ">" -> "GREATER_THAN";
            case ">=" -> "GREATER_THAN_OR_EQUALS";
            case "<" -> "LESS_THAN";
            case "<=" -> "LESS_THAN_OR_EQUALS";
            default -> null;
        };
    }

    private Task<Object> createReportGenerationTask(ReportConfigDto config) {
        return new Task<>() {
            @Override
            protected Object call() throws Exception {
                String companyName = config.getCompanyName();
                LocalDate reportDate = config.getReportDate();
                String reportName = config.getReportName();
                List<Map<String, Object>> rawData = reportService.generateReportData(config);
                List<String> technicalHeaders = config.getSelectFields().stream()
                        .map(ReportConfigDto.SelectFieldDto::getAlias)
                        .collect(Collectors.toList());
                List<String> translatedHeaders = technicalHeaders.stream()
                        .map(ManagerViewController.this::translateField)
                        .collect(Collectors.toList());
                List<Map<String, Object>> translatedData = rawData.stream()
                        .map(row -> {
                            Map<String, Object> translatedRow = new LinkedHashMap<>();
                            for (String techHeader : technicalHeaders) {
                                String displayHeader = ManagerViewController.this.translateField(techHeader);
                                Object value = row.get(techHeader);
                                translatedRow.put(displayHeader, value);
                            }
                            return translatedRow;
                        })
                        .collect(Collectors.toList());

                if ("json".equals(config.getOutputFormat())) {
                    return Map.of("headers", technicalHeaders, "data", rawData);
                } else if ("excel".equals(config.getOutputFormat())) {
                    return reportService.exportReportToExcel(
                            translatedData,
                            translatedHeaders,
                            reportName,
                            companyName,
                            reportDate
                    );
                } else if ("word".equals(config.getOutputFormat())) {
                    if (companyName == null || companyName.trim().isEmpty()) {
                        companyName = "Неизвестная компания";
                    }
                    if (reportDate == null) {
                        reportDate = LocalDate.now();
                    }
                    return reportService.exportReportToWord(
                            translatedData,
                            translatedHeaders,
                            reportName,
                            companyName,
                            reportDate
                    );
                }
                throw new IllegalArgumentException("Неизвестный формат вывода: " + config.getOutputFormat());
            }
        };
    }

    private TreeItem<String> createCheckBoxTreeItem(String technicalName) {
        String displayName = FIELD_DISPLAY_NAMES.getOrDefault(technicalName, technicalName);
        CheckBox checkBox = new CheckBox(displayName);

        boolean isSelected = selectedFieldsListView.getItems().contains(technicalName)
                || groupByListView.getItems().contains(technicalName);
        checkBox.setSelected(isSelected);

        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!selectedFieldsListView.getItems().contains(technicalName) &&
                        !groupByListView.getItems().contains(technicalName)) {

                    selectedFieldsListView.getItems().add(technicalName);
                }
            } else {
                selectedFieldsListView.getItems().remove(technicalName);
                groupByListView.getItems().remove(technicalName);
            }
        });

        TreeItem<String> treeItem = new TreeItem<>(technicalName);
        treeItem.setGraphic(checkBox);
        return treeItem;
    }



    private void setCheckBoxStateInTreeView(String itemName, boolean selected, boolean fireSelectionLogic) {
        if (fieldsTreeView.getRoot() == null) return;

        for (TreeItem<String> categoryNode : fieldsTreeView.getRoot().getChildren()) {
            for (TreeItem<String> fieldNode : categoryNode.getChildren()) {
                if (fieldNode.getValue() != null && fieldNode.getValue().equals(itemName) && fieldNode.getGraphic() instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) fieldNode.getGraphic();
                    if (checkBox.isSelected() != selected) {
                        if (fireSelectionLogic) {
                            boolean existsInAnyList = selectedFieldsListView.getItems().contains(itemName)
                                    || groupByListView.getItems().contains(itemName);
                            if (existsInAnyList != checkBox.isSelected()) {
                                checkBox.setSelected(existsInAnyList);
                            }
                        } else {
                            checkBox.setSelected(selected);
                        }
                    }
                    return;
                }
            }
        }
    }


    @FXML
    private void handleAddField() {
        TreeItem<String> selectedTreeItem = fieldsTreeView.getSelectionModel().getSelectedItem();
        if (selectedTreeItem != null && selectedTreeItem.getGraphic() instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) selectedTreeItem.getGraphic();
            if (!checkBox.isSelected()) {
                checkBox.setSelected(true);
            }
        } else if (selectedTreeItem != null && selectedTreeItem.getValue() != null) {
            String itemText = selectedTreeItem.getValue();
            if (!selectedFieldsListView.getItems().contains(itemText)) {
                selectedFieldsListView.getItems().add(itemText);
                setCheckBoxStateInTreeView(itemText, true, true);
            }
        }
    }

    @FXML
    private void handleRemoveField() {
        String selectedItem = selectedFieldsListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedFieldsListView.getItems().remove(selectedItem);
            setCheckBoxStateInTreeView(selectedItem, false, true);
        }
    }

    @FXML
    private void handleClearFields() {
        List<String> itemsToClear = new ArrayList<>(selectedFieldsListView.getItems());
        selectedFieldsListView.getItems().clear();
        groupByListView.getItems().clear();
        for (String item : itemsToClear) {
            setCheckBoxStateInTreeView(item, false, true);
        }
    }


    private void initFilterControls() {

        activeFiltersListView.setPlaceholder(new Label("Нажмите на фильтр, чтобы удалить"));

        filterOperatorCombo.setItems(FXCollections.observableArrayList(
                "=", "!=", ">", "<", ">=", "<=", "содержит", "начинается с"
        ));

        reportEntityComboBox.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            populateFilterFieldCombo(nv);
        });

        activeFiltersListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                int selectedIndex = activeFiltersListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    activeFiltersListView.getItems().remove(selectedIndex);
                }
            }
        });

        if (reportEntityComboBox.getValue() != null) {
            populateFilterFieldCombo(reportEntityComboBox.getValue());
        }
    }

    private void populateFilterFieldCombo(String entityName) {
        filterFieldCombo.getItems().clear();

        if (entityName != null && !entityName.isEmpty()) {
            List<String> technicalFields = entityDisplayFieldsMap.getOrDefault(entityName, new ArrayList<>());

            List<FieldTranslation> translatedFields = technicalFields.stream()
                    .map(field -> new FieldTranslation(
                            field,
                            FIELD_DISPLAY_NAMES.getOrDefault(field, field)
                    ))
                    .distinct()
                    .toList();

            filterFieldCombo.setItems(FXCollections.observableArrayList(translatedFields));

            filterFieldCombo.setCellFactory(new Callback<>() {
                @Override
                public ListCell<FieldTranslation> call(ListView<FieldTranslation> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(FieldTranslation item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText(null);
                            } else {
                                setText(item.getDisplayName());
                            }
                        }
                    };
                }
            });

            if (!translatedFields.isEmpty()) {
                filterFieldCombo.getSelectionModel().selectFirst();
            }
        }
    }

    @FXML
    private void handleAddGroupBy() {
        TreeItem<String> selectedTreeItem = fieldsTreeView.getSelectionModel().getSelectedItem();
        if (selectedTreeItem != null && selectedTreeItem.getGraphic() instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) selectedTreeItem.getGraphic();
            if (!checkBox.isSelected()) {
                checkBox.setSelected(true);
            }
        } else if (selectedTreeItem != null && selectedTreeItem.getValue() != null) {
            String itemText = selectedTreeItem.getValue();
            String currentEntity = reportEntityComboBox.getValue();
            List<String> simpleFieldsForEntity = entityDisplayFieldsMap.getOrDefault(currentEntity, Collections.emptyList());

            if (simpleFieldsForEntity.contains(itemText)) {
                if (!groupByListView.getItems().contains(itemText)) {
                    groupByListView.getItems().add(itemText);
                    setCheckBoxStateInTreeView(itemText, true, true);
                }
            }
        }
    }

    @FXML
    private void handleRemoveGroupBy() {
        String selectedItem = groupByListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            groupByListView.getItems().remove(selectedItem);
            setCheckBoxStateInTreeView(selectedItem, false, true);
        }
    }

    @FXML
    private void handleClearGroupBy() {
        List<String> itemsToClear = new ArrayList<>(groupByListView.getItems());
        groupByListView.getItems().clear();
        for (String item : itemsToClear) {
            setCheckBoxStateInTreeView(item, false, true);
        }
    }

    @FXML
    private void handleClearSort() {
        sortFieldCombo.getSelectionModel().clearSelection();
        sortFieldCombo.setValue(null);
        sortOrderCombo.getSelectionModel().clearSelection();
        sortOrderCombo.setValue(null);

        updateSortByComboBoxWithSelectedAndGroupFields();
    }

    public class FieldTranslation {
        private final String technicalName;
        private final String displayName;

        public FieldTranslation(String technicalName, String displayName) {
            this.technicalName = technicalName;
            this.displayName = displayName;
        }

        public String getTechnicalName() { return technicalName; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() {
            return displayName;
        }
    }


    @FXML
    private void handleAddFilter() {
        FieldTranslation selectedField = filterFieldCombo.getValue();
        String operator = filterOperatorCombo.getValue();
        String value = filterValueField.getText();

        if (selectedField != null && operator != null && value != null && !value.isEmpty()) {
            String displayField = selectedField.getDisplayName();
            activeFiltersListView.getItems().add(
                    String.format("%s %s '%s'", displayField, operator, value)
            );
        }
    }

    private void setupDragDrop() {
        fieldsTreeView.setOnDragDetected(event -> {
            TreeItem<String> selectedItem = fieldsTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() != null) {
                Dragboard db = fieldsTreeView.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedItem.getValue());
                db.setContent(content);
                event.consume();
            }
        });

        handleDragOver(selectedFieldsListView);
        handleDragOver(groupByListView);

        handleDragDropped(selectedFieldsListView, false);
        handleDragDropped(groupByListView, true);
    }

    private void handleDragOver(ListView<String> targetListView) {
        targetListView.setOnDragOver(event -> {
            if (event.getGestureSource() != targetListView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
    }

    private void handleDragDropped(ListView<String> targetListView, boolean isGroupBy) {
        targetListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String draggedText = db.getString();
                if (isGroupBy) {
                    String currentEntity = reportEntityComboBox.getValue();
                    List<String> simpleFieldsForEntity = entityDisplayFieldsMap.getOrDefault(currentEntity, Collections.emptyList());
                    if (simpleFieldsForEntity.contains(draggedText)) {
                        if (!targetListView.getItems().contains(draggedText)) {
                            targetListView.getItems().add(draggedText);
                        }
                        success = true;
                    }
                } else {
                    if (!targetListView.getItems().contains(draggedText)) {
                        targetListView.getItems().add(draggedText);
                    }
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handlePrintReport() {
        if (lastGeneratedConfig == null) {
            showAlert(Alert.AlertType.WARNING, "Нет отчета", "Сначала сгенерируйте отчет.");
            return;
        }

        String outputFormat = lastGeneratedConfig.getOutputFormat();
        String reportName = lastGeneratedConfig.getReportName();
        String companyName = lastGeneratedConfig.getCompanyName();
        LocalDate reportDate = lastGeneratedConfig.getReportDate();

        Task<ExportResult> exportTask = new Task<>() {

            @Override
            protected ExportResult call() throws Exception {
                String selectedExportFormat;
                if ("json".equals(outputFormat) && currentReportData != null && currentReportHeaders != null) {
                    final ButtonType[] selectedType = new ButtonType[1];
                    final ButtonType excelButton = new ButtonType("Excel");
                    final ButtonType wordButton = new ButtonType("Word");
                    final ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
                    CountDownLatch latch = new CountDownLatch(1);

                    Platform.runLater(() -> {
                        Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
                        choiceDialog.setTitle("Выберите формат экспорта");
                        choiceDialog.setHeaderText("Экспортировать текущий отображаемый отчет?");
                        choiceDialog.getButtonTypes().setAll(excelButton, wordButton, cancelButton);

                        Optional<ButtonType> result = choiceDialog.showAndWait();
                        result.ifPresent(type -> selectedType[0] = type);
                        latch.countDown();
                    });

                    latch.await();
                    if (selectedType[0] != null) {
                        if (excelButton.equals(selectedType[0])) {
                            selectedExportFormat = "excel";
                            byte[] data = reportService.exportReportToExcel(
                                    currentReportData,
                                    currentReportHeaders,
                                    reportName,
                                    companyName,
                                    reportDate
                            );
                            return new ExportResult(data, "excel");
                        } else if (wordButton.equals(selectedType[0])) {
                            selectedExportFormat = "word";
                            byte[] data = reportService.exportReportToWord(
                                    currentReportData,
                                    currentReportHeaders,
                                    reportName,
                                    companyName,
                                    reportDate
                            );
                            return new ExportResult(data, "word");
                        }
                    }
                    return new ExportResult(null, null);
                } else if ("excel".equals(outputFormat) || "word".equals(outputFormat)) {
                    List<Map<String, Object>> rawData = reportService.generateReportData(lastGeneratedConfig);
                    List<String> technicalHeaders = lastGeneratedConfig.getSelectFields().stream()
                            .map(ReportConfigDto.SelectFieldDto::getAlias)
                            .collect(Collectors.toList());
                    List<String> translatedHeaders = technicalHeaders.stream()
                            .map(ManagerViewController.this::translateField)
                            .collect(Collectors.toList());
                    List<Map<String, Object>> translatedData = rawData.stream()
                            .map(row -> {
                                Map<String, Object> translatedRow = new LinkedHashMap<>();
                                for (String techHeader : technicalHeaders) {
                                    String displayHeader = ManagerViewController.this.translateField(techHeader);
                                    Object value = row.get(techHeader);
                                    translatedRow.put(displayHeader, value);
                                }
                                return translatedRow;
                            })
                            .collect(Collectors.toList());

                    if ("excel".equals(outputFormat)) {
                        selectedExportFormat = "excel";
                        byte[] fileData = reportService.exportReportToExcel(
                                translatedData,
                                translatedHeaders,
                                reportName,
                                companyName,
                                reportDate
                        );
                        return new ExportResult(fileData, "excel");
                    } else if ("word".equals(outputFormat)) {
                        selectedExportFormat = "word";
                        byte[] fileData = reportService.exportReportToWord(
                                translatedData,
                                translatedHeaders,
                                reportName,
                                companyName,
                                reportDate
                        );
                        return new ExportResult(fileData, "word");
                    }
                }
                return new ExportResult(null, null);
            }
        };

        exportTask.setOnSucceeded(event -> {
            ExportResult result = exportTask.getValue();
            byte[] fileBytes = result.getData();
            String effectiveOutputFormat = result.getFormat();

            if (fileBytes != null && effectiveOutputFormat != null) {
                FileChooser fileChooser = new FileChooser();
                String defaultFileName = reportName;
                String extension;

                if ("excel".equalsIgnoreCase(effectiveOutputFormat)) {
                    defaultFileName += ".xlsx";
                    extension = "*.xlsx";
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Excel файл (*.xlsx)", extension));
                } else if ("word".equalsIgnoreCase(effectiveOutputFormat)) {
                    defaultFileName += ".docx";
                    extension = "*.docx";
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Word файл (*.docx)", extension));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Неподдерживаемый формат для сохранения.");
                    return;
                }

                fileChooser.setInitialFileName(defaultFileName);
                File file = fileChooser.showSaveDialog(managerBox.getScene().getWindow());
                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(fileBytes);
                        reportStatusArea.setText("Отчет сохранен в: " + file.getAbsolutePath());
                    } catch (IOException e) {
                        reportStatusArea.setText("Ошибка сохранения файла: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else if (!"json".equals(outputFormat) && !"excel".equals(outputFormat) && !"word".equals(outputFormat)) {
                reportStatusArea.setText("Экспорт отчета отменен или не выбран формат.");
            } else if (("excel".equals(outputFormat) || "word".equals(outputFormat)) && fileBytes == null) {
                reportStatusArea.setText("Ошибка генерации файла для экспорта.");
            }
        });

        exportTask.setOnFailed(event -> {
            Throwable exception = exportTask.getException();
            reportStatusArea.setText("Ошибка экспорта отчета: " + exception.getMessage());
            exception.printStackTrace();
        });
        new Thread(exportTask).start();
    }

    @Getter
    private static class ExportResult {
        private final byte[] data;
        private final String format;

        public ExportResult(byte[] data, String format) {
            this.data = data;
            this.format = format;
        }

    }

    @FXML
    private void handleLogout() {
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginRoot = loader.load();
            Scene loginScene = new Scene(loginRoot);
            currentStage.setScene(loginScene);
            currentStage.setTitle("Вход в систему - Фитнес Центр");
            currentStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка выхода", "Не удалось загрузить окно входа.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void reportStatus(String message) {
        Platform.runLater(() -> reportStatusArea.setText(message));
    }

    private void handleTaskFailure(String action, Throwable exception) {
        String errorMessage = "Ошибка " + action + ": " + (exception != null ? exception.getMessage() : "Неизвестная ошибка");
        System.err.println(errorMessage);
        if (exception != null) exception.printStackTrace();
        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Ошибка", errorMessage));
    }

    private void showEditDialog(String fxmlPath, String title, Object entity, DialogInitializer initializer, Runnable onSaveCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent page = loader.load();

            page.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), page);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(managerBox.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            if (initializer != null) {
                initializer.init(loader.getController());
            }

            Object dialogController = loader.getController();
            if (dialogController instanceof SaveableController) {
                ((SaveableController) dialogController).setOnSave(() -> {
                    dialogStage.close();
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                });
                ((SaveableController) dialogController).setOnCancel(dialogStage::close);
            } else {
                System.err.println("Диалоговый контроллер " + dialogController.getClass().getName() + " не реализует SaveableController");
            }

            dialogStage.setOnShowing((event -> fadeIn.play()));

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки", "Не удалось загрузить диалоговое окно: " + fxmlPath);
        }
    }

    @FunctionalInterface
    interface DialogInitializer {
        void init(Object controller);
    }
    public interface SaveableController {
        void setOnSave(Runnable onSaveAction);
        void setOnCancel(Runnable onCancelAction);
    }

    @FXML
    private void handleViewReport() {
        if (currentReportHeaders == null || currentReportData == null) {
            reportStatus("Ошибка: Нет данных для просмотра.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/report-view.fxml"));
            Parent root = loader.load();

            root.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ReportViewController controller = loader.getController();
            controller.setReportData(currentReportHeaders, currentReportData);
            Stage stage = new Stage();
            stage.setTitle("Результат отчёта");
            Scene scene = new Scene(root);
            String cssPath = getClass().getResource("/ui/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            stage.setScene(scene);
            stage.setOnShown(event -> fadeIn.play());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            reportStatus("Ошибка: Не удалось открыть окно отчета.");
        }
    }

    private void fadeInNode(Node node, double durationMillis) {
        if (node == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(durationMillis), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void addHoverScaleAnimation(Button button) {
        if (button == null) return; // Добавим проверку на null
        ScaleTransition stOver = new ScaleTransition(Duration.millis(100), button);
        stOver.setToX(1.05);
        stOver.setToY(1.05);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(100), button);
        stOut.setToX(1.0);
        stOut.setToY(1.0);

        button.setOnMouseEntered(e -> stOver.playFromStart());
        button.setOnMouseExited(e -> stOut.playFromStart());
    }
}