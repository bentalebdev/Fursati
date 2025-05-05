package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
@Data
@Entity
@Table(name = "demandes")
public class Demande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private Offre offre;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDemande;

    private String etat; // "En attente", "Acceptée", "Refusée", etc.

    private String message; // Optional message from candidate

    private String cv; // Path to CV file or identifier
}