package com.ismagi.Fursati.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExperienceDTO {
    private Long id;
    private String position;
    private String companyName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean currentJob;
    private String description;
}
