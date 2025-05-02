package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String proficiency;
    private Integer proficiencyLevel; // 0-100

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;
}
