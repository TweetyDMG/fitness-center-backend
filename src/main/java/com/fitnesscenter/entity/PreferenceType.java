package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "preference_types")
@Data
public class PreferenceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Preference_Type")
    private Long id;

    @Column(name = "Preference_Name", nullable = false)
    private String preferenceName;
}