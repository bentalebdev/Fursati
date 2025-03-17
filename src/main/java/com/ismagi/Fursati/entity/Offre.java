package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOffre;

    private String description;
    private String typeContrat;
    private String experience;
    private String formation;
    private String langues;
    private int nbRecruteurs;
    private Double salaire;
    private String specialite;
    private String villeTravail;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL)
    private List<Demande> demandes; // A job offer can have multiple applications

    @ManyToOne
    @JoinColumn(name = "recruteur_id")
    private Recruteur recruteur; // A job offer belongs to a recruiter

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin; // A job offer is posted by an admin
}
