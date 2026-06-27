package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

@Entity
@Table(name = "schedule")
@Data
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Entry")
    private Long id;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Start_time", nullable = false)
    private Time startTime;

    @Column(name = "End_time", nullable = false)
    private Time endTime;

    @Column(name = "Trainer_ID_Trainer", nullable = false)
    private Long trainer;


}