package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_of_visits")
@Data
public class RegistrationOfVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Visits")
    private Long id;

    @Column(name = "Visit_Date", nullable = false)
    private Timestamp visitDate;
    
    @ManyToOne
    @JoinColumn(name = "Schedule_ID_Entry", nullable = false)
    private Schedule schedule;

    @Column(name = "Sale_ID_Sale", nullable = false)
    private Long saleId;

}