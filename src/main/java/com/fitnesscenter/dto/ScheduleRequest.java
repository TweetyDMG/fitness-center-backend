package com.fitnesscenter.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleRequest {
    private LocalDate date;
    private String startTime;
    private String endTime;
}