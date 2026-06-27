package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "discount")
@Data
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Discount")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Percentage")
    private Integer percentage;
}