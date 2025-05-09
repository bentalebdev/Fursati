package com.ismagi.Fursati.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String proficiency;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    @JsonIgnoreProperties({"skills", "languages", "experiences", "educations"})
    private Candidat candidat;
}