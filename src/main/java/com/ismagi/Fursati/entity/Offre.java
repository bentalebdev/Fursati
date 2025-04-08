package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String companyName;
    private String location;
    private Double minSalary;
    private Double maxSalary;
    private String logoUrl;
    private String contractType;
    private String experienceLevel;
    private String workMode;
    private String industry;
    private LocalDateTime postedAt;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL)
    private List<Demande> demandes; // A job offer can have multiple applications

    @ManyToOne
    @JoinColumn(name = "recruteur_id")
    private Recruteur recruteur; // A job offer belongs to a recruiter

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin; // A job offer is posted by an admin
}
