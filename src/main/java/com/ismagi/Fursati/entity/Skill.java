package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

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

    @Column(nullable = false)
    private Integer level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    public enum SkillType {
        TECHNICAL, LANGUAGE
    }
}