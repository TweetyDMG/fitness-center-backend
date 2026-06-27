package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation")
@Data
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_Recommendation;

    @Column(name = "Client_ID_Client", nullable = false)
    private Long clientId;

    @Column(name = "Trainer_ID_Trainer", nullable = false)
    private Long trainerId;

    @Column(name = "Text", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String text;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;
}