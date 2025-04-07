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

    private String title;               // Job title (e.g., "Full Stack Developer")
    private String description;         // Job description (TEXT in PostgreSQL)
    private String companyName;        // Company name (e.g., "TechMagic SARL")
    private String location;           // Location (e.g., "Casablanca")
    private Double minSalary;          // Minimum salary (MAD)
    private Double maxSalary;          // Maximum salary (MAD)
    private String logoUrl;            // Logo path/URL (e.g., "/uploads/logo.png")
    private String contractType;       // Contract type (e.g., "CDI", "Freelance")
    private String experienceLevel;    // Experience (e.g., "3-5 years")
    private String workMode;           // Work mode (e.g., "Hybrid", "Remote")
    private String industry;           // Industry (e.g., "IT & Technology")
    private LocalDateTime postedAt;    // Posting timestamp

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL)
    private List<Demande> demandes; // A job offer can have multiple applications

    @ManyToOne
    @JoinColumn(name = "recruteur_id")
    private Recruteur recruteur; // A job offer belongs to a recruiter

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin; // A job offer is posted by an admin
}
