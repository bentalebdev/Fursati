package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Recruteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecruteur;

    private String nomEntreprise;
    private String secteur;
    private String offresPubliees;

    @OneToMany(mappedBy = "recruteur", cascade = CascadeType.ALL)
    private List<Offre> offres; // A recruiter can post multiple job offers
}
