package com.ismagi.Fursati.service;

import com.ismagi.Fursati.dto.*;
import com.ismagi.Fursati.entity.*;
import com.ismagi.Fursati.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CandidatProfileService {

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private LanguageRepository languageRepository;

    public Candidat getCandidatById(Long id) {
        return candidatRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Candidat not found with id: " + id));
    }



    @Transactional
    public Candidat updateBasicInfo(Long id, BasicInfoDTO basicInfoDTO) {
        Candidat candidat = getCandidatById(id);

        candidat.setFirstName(basicInfoDTO.getFirstName());
        candidat.setLastName(basicInfoDTO.getLastName());
        candidat.setEmail(basicInfoDTO.getEmail());
        candidat.setPhone(basicInfoDTO.getPhone());
        candidat.setBirthdate(basicInfoDTO.getBirthdate());

        String updatedAddress = basicInfoDTO.getCity();
        if (candidat.getAddress() != null && candidat.getAddress().contains(",")) {
            String[] parts = candidat.getAddress().split(",");
            if (parts.length > 1) {
                updatedAddress += ", " + parts[parts.length - 1].trim();
            }
        }
        candidat.setAddress(updatedAddress);

        return candidatRepository.save(candidat);
    }

    @Transactional
    public Candidat updateSummary(Long id, SummaryDTO summaryDTO) {
        Candidat candidat = getCandidatById(id);
        candidat.setSummary(summaryDTO.getSummary());
        return candidatRepository.save(candidat);
    }

    @Transactional
    public Candidat updateExperiences(Long id, ExperienceListDTO experienceListDTO) {
        Candidat candidat = getCandidatById(id);

        if (experienceListDTO.getExperiences() != null && !experienceListDTO.getExperiences().isEmpty()) {
            ExperienceDTO expDTO = experienceListDTO.getExperiences().get(0);

            if (expDTO.getId() != null) {
                Optional<Experience> existingExpOpt = candidat.getExperiences().stream()
                        .filter(e -> e.getId().equals(expDTO.getId()))
                        .findFirst();

                if (existingExpOpt.isPresent()) {
                    Experience existingExp = existingExpOpt.get();
                    updateExperienceFromDTO(existingExp, expDTO);
                }
            } else {
                Experience newExperience = new Experience();
                updateExperienceFromDTO(newExperience, expDTO);
                newExperience.setCandidat(candidat);
                candidat.getExperiences().add(newExperience);
            }
        }

        return candidatRepository.save(candidat);
    }

    private void updateExperienceFromDTO(Experience experience, ExperienceDTO dto) {
        experience.setJobTitle(dto.getPosition());
        experience.setCompany(dto.getCompanyName());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setCurrentJob(dto.getCurrentJob() != null && dto.getCurrentJob());
        experience.setDescription(dto.getDescription());
    }

    @Transactional
    public void deleteExperience(Long experienceId) {
        experienceRepository.deleteById(experienceId);
    }

    @Transactional
    public Candidat updateEducations(Long id, EducationListDTO educationListDTO) {
        Candidat candidat = getCandidatById(id);

        if (educationListDTO.getEducations() != null && !educationListDTO.getEducations().isEmpty()) {
            EducationDTO eduDTO = educationListDTO.getEducations().get(0);

            if (eduDTO.getId() != null) {
                Optional<Education> existingEduOpt = candidat.getEducations().stream()
                        .filter(e -> e.getId().equals(eduDTO.getId()))
                        .findFirst();

                if (existingEduOpt.isPresent()) {
                    Education existingEdu = existingEduOpt.get();
                    updateEducationFromDTO(existingEdu, eduDTO);
                }
            } else {
                Education newEducation = new Education();
                updateEducationFromDTO(newEducation, eduDTO);
                newEducation.setCandidat(candidat);
                candidat.getEducations().add(newEducation);
            }
        }

        return candidatRepository.save(candidat);
    }

    private void updateEducationFromDTO(Education education, EducationDTO dto) {
        education.setDegree(dto.getDegree());
        education.setSchool(dto.getSchoolName());
        education.setSchoolLocation(dto.getLocation());
        if (dto.getStartDate() != null) {
            education.setStartYear(dto.getStartDate().getYear());
        }
        if (dto.getEndDate() != null) {
            education.setEndYear(dto.getEndDate().getYear());
        }
        education.setFieldOfStudy(dto.getFieldOfStudy());
    }

    @Transactional
    public void deleteEducation(Long educationId) {
        educationRepository.deleteById(educationId);
    }

    @Transactional
    public Candidat updateSkillsAndLanguages(Long id, SkillsLanguagesDTO skillsLanguagesDTO) {
        Candidat candidat = getCandidatById(id);

        if (skillsLanguagesDTO.getSkills() != null) {
            candidat.getSkills().clear();

            for (SkillDTO skillDTO : skillsLanguagesDTO.getSkills()) {
                Skill skill = new Skill();
                skill.setName(skillDTO.getName());
                skill.setProficiency(skillDTO.getProficiency());
                skill.setCandidat(candidat);
                candidat.getSkills().add(skill);
            }
        }

        if (skillsLanguagesDTO.getLanguages() != null) {
            candidat.getLanguages().clear();

            for (LanguageDTO langDTO : skillsLanguagesDTO.getLanguages()) {
                Language language = new Language();
                language.setName(langDTO.getName());
                language.setProficiency(langDTO.getProficiency());
                language.setProficiencyLevel(langDTO.getProficiencyLevel());
                language.setCandidat(candidat);
                candidat.getLanguages().add(language);
            }
        }

        return candidatRepository.save(candidat);
    }
}