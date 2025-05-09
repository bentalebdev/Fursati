package com.ismagi.Fursati.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String level; // Keep original field name
    private Integer proficiencyLevel; // Add this new field

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    @JsonIgnoreProperties({"skills", "languages", "experiences", "educations"})
    private Candidat candidat;
}