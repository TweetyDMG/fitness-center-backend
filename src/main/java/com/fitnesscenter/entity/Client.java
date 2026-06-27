package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "client")
@Data
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Client")
    private Long id;

    @Column(name = "Firstname", nullable = false)
    private String firstname;

    @Column(name = "Lastname", nullable = false)
    private String lastname;

    @Column(name = "Patronymic", nullable = false)
    private String patronymic;

    @Column(name = "Phone", nullable = false)
    private String phone;

    @Column(name = "Gender", nullable = false)
    private String gender;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "Passport")
    private String passport;

    public String getFullName() {
        return lastname + " " + firstname + (patronymic != null && !patronymic.isEmpty() ? " " + patronymic : "");
    }
}