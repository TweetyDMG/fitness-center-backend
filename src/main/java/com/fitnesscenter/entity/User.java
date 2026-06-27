package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_User")
    private Long id;

    @Column(name = "Username", unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "Role", nullable = false)
    private String role;

    @ManyToOne
    @JoinColumn(name = "Client_ID_Client")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "Trainer_ID_Trainer")
    private  Trainer trainer;
}