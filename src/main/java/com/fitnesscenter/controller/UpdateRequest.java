package com.fitnesscenter.controller;

import com.fitnesscenter.entity.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @Pattern(regexp = "^(CLIENT|TRAINER|MANAGER|ADMIN)$", message = "Role must be CLIENT, TRAINER, MANAGER, or ADMIN")
    private String role;

    private Client client;
}