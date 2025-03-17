package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Candidat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidatId;

    private String nom;
    private String prenom;
    private String telephone;
    private int age;
    private String email;
    private String login;
    private String password;

    @Lob
    private byte[] cv;

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL)
    private List<Demande> demandes; // One candidate can make multiple job applications

    @ManyToMany
    @JoinTable(
            name = "candidat_diplome",
            joinColumns = @JoinColumn(name = "candidat_id"),
            inverseJoinColumns = @JoinColumn(name = "diplome_id")
    )
    private List<Diplome> diplomes; // Candidate can have multiple diplomas
}
