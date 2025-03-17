package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
public class Demande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDemande;

    @Temporal(TemporalType.DATE)
    private Date dateDemande;

    private String etat;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat; // A job application belongs to a candidate

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private Offre offre; // A job application is linked to a job offer
}
