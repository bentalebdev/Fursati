package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    // If this skill is related to other entities, add relationships here
    // For example:
    /*
    @ManyToMany(mappedBy = "skills")
    private Set<CandidateProfile> candidateProfiles = new HashSet<>();
    */
}