package com.ismagi.Fursati.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
public class Tracabilite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idArchive;

    @Temporal(TemporalType.DATE)
    private Date dateCreation;
}
