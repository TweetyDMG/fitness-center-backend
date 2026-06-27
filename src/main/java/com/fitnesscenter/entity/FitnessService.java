package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "service")
@Data
public class FitnessService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Service")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Start_time")
    private LocalTime startTime;

    @Column(name = "End_time")
    private LocalTime endTime;
}