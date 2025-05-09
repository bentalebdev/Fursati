package com.ismagi.Fursati.entity;

import com.ismagi.Fursati.entity.Education;
import com.ismagi.Fursati.entity.Experience;
import com.ismagi.Fursati.entity.Language;
import com.ismagi.Fursati.entity.Skill;
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



    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Language> languages = new ArrayList<>();

}
