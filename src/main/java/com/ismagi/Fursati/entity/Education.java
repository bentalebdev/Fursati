package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String degree;
    private String school;
    private String schoolLocation;
    private Integer startYear;
    private Integer endYear;
    private String fieldOfStudy;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;
}
