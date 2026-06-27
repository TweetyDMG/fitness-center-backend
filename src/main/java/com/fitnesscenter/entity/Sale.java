package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "sale")
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Sale")
    private Long id;

    @Column(name = "CardNumber", nullable = false)
    private String bankCardNum;

    @Column(name = "Start_date")
    private LocalDate startDate;

    @Column(name = "End_date")
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "Client_ID_Client")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "Subscription_ID_Subscription")
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "Discount_ID_Discount")
    private Discount discount;

    @ManyToOne
    @JoinColumn(name = "Fitness_center_ID_Fitness_center")
    private FitnessCenter fitnessCenter;

}