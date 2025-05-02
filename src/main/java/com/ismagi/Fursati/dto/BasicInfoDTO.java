package com.ismagi.Fursati.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BasicInfoDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String city;
    private LocalDate birthdate;
}
