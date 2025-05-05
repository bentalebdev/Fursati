package com.ismagi.Fursati.service;

import com.ismagi.Fursati.dto.*;
import com.ismagi.Fursati.entity.*;
import com.ismagi.Fursati.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class CandidatProfileService {
    private static final Logger logger = Logger.getLogger(CandidatProfileService.class.getName());


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
    public void updateLanguages(Long candidatId, LanguageListDTO languageListDTO) {
        logger.info("Updating language for candidate ID: " + candidatId);

        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat not found with id: " + candidatId));

        if (languageListDTO.getLanguages() != null && !languageListDTO.getLanguages().isEmpty()) {
            LanguageDTO languageDTO = languageListDTO.getLanguages().get(0);
            logger.info("Processing language: " + languageDTO);

            if (languageDTO.getId() != null && languageDTO.getId() > 0) {
                // Update existing language
                logger.info("Updating existing language with ID: " + languageDTO.getId());

                Language language = languageRepository.findById(languageDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Language not found with id: " + languageDTO.getId()));

                language.setName(languageDTO.getName());
                language.setLevel(languageDTO.getLevel());

                languageRepository.save(language);
                logger.info("Language updated successfully");
            } else {
                // Add new language
                logger.info("Adding new language");

                Language language = new Language();
                language.setName(languageDTO.getName());
                language.setLevel(languageDTO.getLevel());
                language.setCandidat(candidat);

                candidat.getLanguages().add(language);
                candidatRepository.save(candidat);
                logger.info("New language added successfully");
            }
        } else {
            logger.warning("No language data provided");
        }
    }

    public void deleteLanguage(Long languageId) {
        languageRepository.deleteById(languageId);
    }


}