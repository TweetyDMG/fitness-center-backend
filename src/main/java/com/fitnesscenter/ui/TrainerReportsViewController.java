package com.fitnesscenter.ui;

import com.fitnesscenter.dto.ReportConfigDto;
import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.service.ReportService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TrainerReportsViewController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TrainerReportsViewController.class.getName());

    @FXML
    private Label placeholderLabel;
    @FXML
    private TableView<Map<String, Object>> reportTable;
    @FXML
    private TableColumn<Map<String, Object>, String> clientLastnameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> clientFirstnameColumn;
    @FXML
    private TableColumn<Map<String, Object>, Long> totalVisitsColumn;
    @FXML
    private TableColumn<Map<String, Object>, LocalDate> lastVisitDateColumn;

    private Trainer currentTrainer;
    private List<Map<String, Object>> currentReportData;
    private ReportConfigDto currentReportConfig;

    @Autowired
    private ReportService reportService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.INFO, "TrainerReportsViewController initialized.");

        clientLastnameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("client_lastname")));
        clientFirstnameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("client_firstname")));
        totalVisitsColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("totalVisits");
            return new SimpleObjectProperty<>(value != null ? ((Number) value).longValue() : null);
        });
        lastVisitDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>((LocalDate) cellData.getValue().get("lastVisitDate")));

        lastVisitDateColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        reportTable.setItems(FXCollections.observableArrayList());
        placeholderLabel.setText("Отчёты готовы к генерации.");
    }

    public void setTrainer(Trainer trainer) {
        this.currentTrainer = trainer;
        if (this.currentTrainer != null) {
            LOGGER.log(Level.INFO, "Trainer data set in ReportsView: " + this.currentTrainer.getPhoneNumber());
            placeholderLabel.setText("Отчёты для тренера: " + this.currentTrainer.getFirstname() + " " + this.currentTrainer.getLastname());
            generateClientActivityReport();
        } else {
            LOGGER.log(Level.WARNING, "Current trainer is null in ReportsView.");
            placeholderLabel.setText("Отчёты (тренер не определён)");
            reportTable.getItems().clear();
            currentReportData = null;
            currentReportConfig = null;
        }
    }

    private void generateClientActivityReport() {
        if (currentTrainer == null) {
            LOGGER.log(Level.WARNING, "Cannot generate report: currentTrainer is null.");
            return;
        }

        try {
            ReportConfigDto config = new ReportConfigDto();
            config.setEntityName("RegistrationOfVisit");

            List<ReportConfigDto.SelectFieldDto> selectFields = new ArrayList<>();
            selectFields.add(new ReportConfigDto.SelectFieldDto("client.lastname", null, "client_lastname"));
            selectFields.add(new ReportConfigDto.SelectFieldDto("client.firstname", null, "client_firstname"));
            selectFields.add(new ReportConfigDto.SelectFieldDto("id", "COUNT", "totalVisits"));
            selectFields.add(new ReportConfigDto.SelectFieldDto("visitDate", "MAX", "lastVisitDate"));
            config.setSelectFields(selectFields);

            List<String> groupByFields = new ArrayList<>();
            groupByFields.add("client.lastname");
            groupByFields.add("client.firstname");
            config.setGroupByFields(groupByFields);

            List<ReportConfigDto.FilterConditionDto> filterConditions = new ArrayList<>();
            filterConditions.add(new ReportConfigDto.FilterConditionDto("schedule.trainer.id", "EQUALS", currentTrainer.getId()));
            config.setFilterConditions(filterConditions);

            List<ReportConfigDto.HavingConditionDto> havingConditions = new ArrayList<>();
            havingConditions.add(new ReportConfigDto.HavingConditionDto("totalVisits", "LESS_THAN", 5L));
            config.setHavingConditions(havingConditions);

            config.setOutputFormat("EXCEL");
            config.setReportName("Отчет по активности клиентов тренера " + currentTrainer.getFirstname() + " " + currentTrainer.getLastname());
            config.setCompanyName("Фитнес-центр");
            config.setReportDate(LocalDate.now());

            currentReportConfig = config;
            currentReportData = reportService.generateReportData(config);
            LOGGER.log(Level.INFO, "Report data generated: " + currentReportData.size() + " rows.");

            ObservableList<Map<String, Object>> observableReportData = FXCollections.observableArrayList(currentReportData);
            reportTable.setItems(observableReportData);

            if (currentReportData.isEmpty()) {
                placeholderLabel.setText("Отчёт сгенерирован, но данных нет.");
            } else {
                placeholderLabel.setText("Отчёт по активности клиентов для тренера " + currentTrainer.getFirstname() + " " + currentTrainer.getLastname() + " сгенерирован.");
            }

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error generating report: " + e.getMessage(), e);
            placeholderLabel.setText("Ошибка при генерации отчета: " + e.getMessage());
            reportTable.getItems().clear();
            currentReportData = null;
            currentReportConfig = null;
        }
    }

    @FXML
    private void handleExportExcel() {
        if (currentReportData == null || currentReportData.isEmpty() || currentReportConfig == null) {
            placeholderLabel.setText("Нет данных для экспорта в Excel. Сгенерируйте отчет сначала.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет в Excel");
        fileChooser.setInitialFileName(currentReportConfig.getReportName() + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        Stage stage = (Stage) reportTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                List<String> headers = getReportHeaders();
                byte[] excelReport = reportService.exportReportToExcel(currentReportData, headers,
                        currentReportConfig.getReportName(), currentReportConfig.getCompanyName(), currentReportConfig.getReportDate());
                fos.write(excelReport);
                placeholderLabel.setText("Отчет успешно экспортирован в Excel: " + file.getAbsolutePath());
                LOGGER.log(Level.INFO, "Excel report exported to: " + file.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error exporting Excel report: " + e.getMessage(), e);
                placeholderLabel.setText("Ошибка при экспорте в Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportWord() {
        if (currentReportData == null || currentReportData.isEmpty() || currentReportConfig == null) {
            placeholderLabel.setText("Нет данных для экспорта в Word. Сгенерируйте отчет сначала.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет в Word");
        fileChooser.setInitialFileName(currentReportConfig.getReportName() + ".docx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Documents", "*.docx"));

        Stage stage = (Stage) reportTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                List<String> headers = getReportHeaders();
                byte[] wordReport = reportService.exportReportToWord(currentReportData, headers,
                        currentReportConfig.getReportName(), currentReportConfig.getCompanyName(), currentReportConfig.getReportDate());
                fos.write(wordReport);
                placeholderLabel.setText("Отчет успешно экспортирован в Word: " + file.getAbsolutePath());
                LOGGER.log(Level.INFO, "Word report exported to: " + file.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error exporting Word report: " + e.getMessage(), e);
                placeholderLabel.setText("Ошибка при экспорте в Word: " + e.getMessage());
            }
        }
    }

    private List<String> getReportHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("client_lastname");
        headers.add("client_firstname");
        headers.add("totalVisits");
        headers.add("lastVisitDate");
        return headers;
    }
}