// Company.java
package com.ismagi.Fursati.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "companies")
@Data
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String nomEntreprise;

    @Column(name = "sector")
    private String secteur;

    @Column(name = "website")
    private String siteWeb;

    @Column(name = "contact_email")
    private String emailContact;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "founding_year")
    private Integer anneeCreation;

    @Column(name = "company_size")
    private String tailleEntreprise;

    @Column(name = "address")
    private String adresse;

    @Column(name = "city")
    private String ville;

    @Column(name = "country")
    private String pays;

    @Column(name = "postal_code")
    private String codePostal;

    @Column(name = "phone")
    private String telephone;

    @Column(name = "business_registration_number")
    private String numeroRC;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "company_values", length = 1000)
    private String valeurs;

    // Company.java
// Ajoutez cette annotation pour la liste des recruteurs
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference("company-recruteurs") // Utiliser le même label que dans Recruteur
    private List<Recruteur> recruteurs;

    // Update the field in Company.java to allow nulls initially
    @Column(name = "is_verified")
    private Boolean isVerified = false; // Provide a default value to prevent null
    public Company() {}

}