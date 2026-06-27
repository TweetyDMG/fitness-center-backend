package com.fitnesscenter.dto;

import lombok.Data;

@Data
public class ClientProfileResponse {
    private Long id;
    private String firstname;
    private String lastname;
    private String patronymic;
    private String phone;
    private String email;
    private String passport;
}
