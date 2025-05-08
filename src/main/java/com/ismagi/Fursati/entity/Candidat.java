package com.ismagi.Fursati.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Candidat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    @Column(name = "birthdate")
    private LocalDate birthdate;
    private String address;
    private String profilePicture;
    private String summary;
    private String password;

    // Ajout d'un champ status
    private String status = "active"; // Valeur par défaut

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("candidat")
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("candidat")
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("candidat")
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("candidat")
    private List<Language> languages = new ArrayList<>();
}