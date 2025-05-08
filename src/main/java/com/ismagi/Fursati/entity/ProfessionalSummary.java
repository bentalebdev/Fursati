package com.ismagi.Fursati.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProfessionalSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String summary;

    @OneToOne
    @JoinColumn(name = "candidat_id")
    @JsonIgnoreProperties({"skills", "languages", "experiences", "educations"})
    private Candidat candidat;

    // Constructors
    public ProfessionalSummary() {}

    // Getters and Setters
}