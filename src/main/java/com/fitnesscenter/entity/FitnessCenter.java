package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fitness_center")
@Data
public class FitnessCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Fitness_center")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Address")
    private String address;
}