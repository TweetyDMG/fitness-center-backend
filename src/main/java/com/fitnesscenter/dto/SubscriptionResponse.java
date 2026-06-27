package com.fitnesscenter.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionResponse {
    private Long id;
    private String name;
    private Integer price;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer visitsLeft;
    private String status;
}
