package com.ismagi.Fursati.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EducationDTO {
    private Long id;
    private String degree;
    private String schoolName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fieldOfStudy;
}
