package com.ismagi.Fursati.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SkillsLanguagesDTO {
    private List<SkillDTO> skills = new ArrayList<>();
    private List<LanguageDTO> languages =new ArrayList<>();
}
