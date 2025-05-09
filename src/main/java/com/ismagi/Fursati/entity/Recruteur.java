// Recruteur.java
package com.ismagi.Fursati.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Getter
@Setter
public class Recruteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecruteur;

    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    private String password;


    private String poste;
    private String status; // ACTIVE, INACTIVE, PENDING
    private LocalDate dateInscription;

    @OneToMany(mappedBy = "recruteur", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonManagedReference("recruteur-offres") // Utiliser un label pour l'éviter les conflits
    private List<Offre> offres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = true)
    @ToString.Exclude
    @JsonBackReference("company-recruteurs") // Utiliser un label pour l'éviter les conflits
    private Company company;
}