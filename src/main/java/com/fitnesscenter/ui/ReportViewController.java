package com.fitnesscenter.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

public class ReportViewController {
    @FXML
    private TableView<Map<String, Object>> reportTable;

    public void setReportData(List<String> headers, List<Map<String, Object>> data) {
        reportTable.getColumns().clear();

        for (String header : headers) {
            TableColumn<Map<String, Object>, Object> column = new TableColumn<>(header);
            column.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().get(header))
            );
            column.setMinWidth(150);
            column.setMaxWidth(300);
            column.setResizable(true);
            reportTable.getColumns().add(column);
        }

        reportTable.setItems(FXCollections.observableArrayList(data));
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleClose() {
        reportTable.getScene().getWindow().hide();
    }
}