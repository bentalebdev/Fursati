package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Diplome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDiplome;

    private String diplome;

    @ManyToMany(mappedBy = "diplomes")
    private List<Candidat> candidats; // A diploma can belong to multiple candidates
}
