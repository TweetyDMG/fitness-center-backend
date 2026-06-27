package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "preferences")
@Data
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Preferences")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Preference_Type_ID")
    private PreferenceType preferenceType;

    @ManyToOne
    @JoinColumn(name = "Client_ID_Client")
    private Client client;
}