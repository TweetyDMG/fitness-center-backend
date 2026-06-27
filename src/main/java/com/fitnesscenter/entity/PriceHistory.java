package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "price_history")
@Data
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Price_History")
    private Long id;

    @Column(name = "Subscription_ID_Subscription", nullable = false)
    private Long subscription;

    @Column(name = "Price", nullable = false)
    private Integer price;

    @Column(name = "Start_Date", nullable = false)
    private java.sql.Date startDate;

    @Column(name = "End_Date")
    private java.sql.Date endDate;
}