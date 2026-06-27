package com.fitnesscenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "disease")
@Data
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Disease")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Disease_Type_ID")
    private DiseaseType diseaseType;

    @ManyToOne
    @JoinColumn(name = "Client_ID_Client", referencedColumnName = "ID_Client")
    private Client client;
}