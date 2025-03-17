package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Internaute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInternaute;

    private String nom;
    private String email;
    private String mdp;
}
