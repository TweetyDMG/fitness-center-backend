package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "subscription")
@Data
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Subscription")
    private Long id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Price", nullable = false)
    private Integer price;

    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "Validity")
    private LocalDate validity;

    @Column(name = "Number_of_visits")
    private Integer numberOfVisits;

    @Column(name = "Number_of_day")
    private Integer numberOfDay;

    @ManyToOne
    @JoinColumn(name = "Service_ID_Service", nullable = false)
    private FitnessService fitnessService;
}