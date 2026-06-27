package com.fitnesscenter.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaleRequest {
    @NotBlank(message = "Bank card number is required")
    @Size(min = 13, max = 19, message = "Bank card number must be between 13 and 19 digits")
    @Pattern(regexp = "^\\d{13,19}$", message = "Bank card number must contain only digits")
    private String bankCardNum;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;

    private Long discountId;

    @NotNull(message = "Fitness Center ID is required")
    private Long fitnessCenterId;
}