package com.fitnesscenter.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SaleFilterRequest {

    private Long clientId;
    private Long fitnessCenterId;
    private LocalDate fromDate;
    private LocalDate toDate;

    private String searchTerm;

    private String sortField = "startDate";
    private String sortOrder = "desc";
}