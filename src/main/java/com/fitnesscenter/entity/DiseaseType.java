package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "disease_types")
@Data
public class DiseaseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Disease_Type")
    private Long id;

    @Column(name = "Disease_Name", nullable = false)
    private String diseaseName;
}