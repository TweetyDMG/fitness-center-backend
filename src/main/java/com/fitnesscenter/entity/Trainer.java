package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "trainer")
@Data
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Trainer")
    private Integer id;

    @Column(name = "Firstname", nullable = false)
    private String firstname;

    @Column(name = "Patronymic", nullable = false)
    private String patronymic;

    @Column(name = "Lastname", nullable = false)
    private String lastname;

    @Column(name = "Phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "Address", nullable = false)
    private String address;

    public String getFullName(){
        return lastname + " " + firstname + (patronymic != null && !patronymic.isEmpty() ? " " + patronymic : " ");
    }
}