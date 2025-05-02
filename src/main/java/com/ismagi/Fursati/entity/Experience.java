package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;
    private String company;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean currentJob;
    private String description;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;
}
