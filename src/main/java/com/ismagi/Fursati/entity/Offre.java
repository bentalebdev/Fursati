package com.ismagi.Fursati.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offres")
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private String industry;
    private String contractType;
    private String workMode;
    private String experienceLevel;

    private BigDecimal minSalary;
    private BigDecimal maxSalary;

    private LocalDateTime postedAt;
    private LocalDateTime expiresAt;

    @Column(name = "status")
    private String status = "DRAFT"; // Default status

    @Column(name = "views")
    private Long views = 0L; // Default views count

    // Relation avec Recruteur - le recruteur possède la référence à Company
    // Offre.java
// Ajoutez cette annotation pour la relation avec le recruteur
    @ManyToOne
    @JoinColumn(name = "recruteur_id")
    @JsonBackReference("recruteur-offres") // Utiliser le même label que dans Recruteur
    private Recruteur recruteur;

    @ElementCollection
    @CollectionTable(
            name = "offre_responsibilities",
            joinColumns = @JoinColumn(name = "offre_id")
    )
    @Column(name = "responsibility")
    private List<String> responsibilities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "offre_qualifications",
            joinColumns = @JoinColumn(name = "offre_id")
    )
    @Column(name = "qualification")
    private List<String> qualifications = new ArrayList<>();

    // Constructeur par défaut
    public Offre() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.maxSalary = maxSalary;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public List<String> getQualifications() {
        return qualifications;
    }

    public void setQualifications(List<String> qualifications) {
        this.qualifications = qualifications;
    }

    // Getter et Setter pour recruteur
    public Recruteur getRecruteur() {
        return recruteur;
    }

    public void setRecruteur(Recruteur recruteur) {
        this.recruteur = recruteur;
    }

    // Méthodes pratiques pour accéder aux informations de l'entreprise
    public String getCompanyName() {
        return recruteur != null && recruteur.getCompany() != null ?
                recruteur.getCompany().getNomEntreprise() : null;
    }

    public String getCompanyDescription() {
        return recruteur != null && recruteur.getCompany() != null ?
                recruteur.getCompany().getDescription() : null;
    }

    public String getCompanyWebsite() {
        return recruteur != null && recruteur.getCompany() != null ?
                recruteur.getCompany().getSiteWeb() : null;
    }

    public String getCompanySize() {
        return recruteur != null && recruteur.getCompany() != null ?
                recruteur.getCompany().getTailleEntreprise() : null;
    }

    public String getCompanyLogoUrl() {
        return recruteur != null && recruteur.getCompany() != null ?
                recruteur.getCompany().getLogoUrl() : null;
    }

    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", contractType='" + contractType + '\'' +
                '}';
    }
}