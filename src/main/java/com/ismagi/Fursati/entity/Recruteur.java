// Recruteur.java
package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Data
public class Recruteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecruteur;

    @OneToMany(mappedBy = "recruteur", cascade = CascadeType.ALL)
    @ToString.Exclude // Add this annotation to exclude from toString()
    private List<Offre> offres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = true)
    @ToString.Exclude // Add this annotation to exclude from toString()
    private Company company;


}