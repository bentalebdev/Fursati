package com.ismagi.Fursati.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ismagi.Fursati.util.LocalDateYearMonthDeserializer;
import java.time.LocalDate;

@Data
public class ExperienceDTO {
    private Long id;

    @NotNull(message = "Position is required")
    private String position;

    @NotNull(message = "Company name is required")
    private String companyName;

    private String location;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private LocalDate endDate;

    private Boolean currentJob;

    private String description;
}
