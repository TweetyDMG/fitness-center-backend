package com.fitnesscenter.controller;

import com.fitnesscenter.dto.ReportConfigDto;
import com.fitnesscenter.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // --- Report generation is temporarily disabled pending ReportService refactoring ---

    private List<String> determineHeaders(List<Map<String, Object>> reportData, ReportConfigDto config) {
        if (config.getSelectFields() != null && !config.getSelectFields().isEmpty()) {
            return config.getSelectFields().stream()
                    .map(selectField -> {
                        if (selectField.getAlias() != null && !selectField.getAlias().trim().isEmpty()) {
                            return selectField.getAlias().trim();
                        }
                        String fieldPath = selectField.getFieldPath();
                        if (fieldPath != null) {
                            if (fieldPath.contains(".")) {
                                return fieldPath.substring(fieldPath.lastIndexOf(".") + 1);
                            }
                            return fieldPath;
                        }
                        return "unknown_field";
                    })
                    .collect(Collectors.toList());
        }
        if (reportData != null && !reportData.isEmpty() && reportData.get(0) != null) {
            return new ArrayList<>(reportData.get(0).keySet());
        }
        return List.of();
    }
}