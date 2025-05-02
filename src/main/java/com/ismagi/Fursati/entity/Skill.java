package com.ismagi.Fursati.entity;

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
    private Candidat candidat;
}
