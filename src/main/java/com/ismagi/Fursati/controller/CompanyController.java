package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Company;
import com.ismagi.Fursati.service.AuthService;
import com.ismagi.Fursati.service.CompanyService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/company")
public class CompanyController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @Autowired
    private CompanyService companyService;

    // Path where company logos will be stored
    private static final String UPLOAD_DIR = "./uploads/company-logos/";

    /**
     * Display company profile page
     */
    @GetMapping("/profile")
    public String showCompanyProfile(Model model, HttpServletRequest request) {
        // Verify user type is RECRUTEUR
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.RECRUTEUR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Get recruiter ID from authenticated user
        Long recruiterId = getUserIdAsLong(request);

        Company company = companyService.getComapnyByRecruteurId(recruiterId);
        model.addAttribute("company", company);
        return "company-profile";
    }

    /**
     * Update company profile information
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateCompanyProfile(@RequestBody Company updatedCompany,
                                                  HttpServletRequest request) {
        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
            }

            // Get recruiter ID from authenticated user
            Long recruiterId = getUserIdAsLong(request);

            Company result = companyService.updateCompanyByRecruitId(recruiterId, updatedCompany);

            if (result != null) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Company not found for recruiter ID: " + recruiterId,
                        HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error updating company profile: ", e);
            return new ResponseEntity<>("An error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Upload company logo
     */
    @PostMapping("/upload-logo")
    public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file,
                                        HttpServletRequest request) {
        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
            }

            // Get recruiter ID from authenticated user
            Long recruiterId = getUserIdAsLong(request);

            // Get company
            Company company = companyService.getComapnyByRecruteurId(recruiterId);

            if (company == null) {
                return new ResponseEntity<>("Company not found", HttpStatus.NOT_FOUND);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Update company logo URL
            String logoUrl = "/uploads/company-logos/" + filename;
            company.setLogoUrl(logoUrl);

            // Save company with new logo URL
            Company updatedCompany = companyService.updateCompanyByRecruitId(recruiterId, company);

            return new ResponseEntity<>(updatedCompany, HttpStatus.OK);

        } catch (IOException e) {
            logger.error("Failed to upload logo: ", e);
            return new ResponseEntity<>("Failed to upload logo: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Error uploading logo: ", e);
            return new ResponseEntity<>("An error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete company logo
     */
    @DeleteMapping("/delete-logo")
    public ResponseEntity<?> deleteLogo(HttpServletRequest request) {
        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
            }

            // Get recruiter ID from authenticated user
            Long recruiterId = getUserIdAsLong(request);

            Company company = companyService.getComapnyByRecruteurId(recruiterId);

            if (company == null) {
                return new ResponseEntity<>("Company not found", HttpStatus.NOT_FOUND);
            }

            // Check if logo exists
            if (company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
                try {
                    // Delete file from storage
                    String filename = company.getLogoUrl().substring(company.getLogoUrl().lastIndexOf("/") + 1);
                    Path filePath = Paths.get(UPLOAD_DIR + filename);
                    Files.deleteIfExists(filePath);

                    // Update company record
                    company.setLogoUrl(null);
                    Company updatedCompany = companyService.updateCompanyByRecruitId(recruiterId, company);

                    return new ResponseEntity<>(updatedCompany, HttpStatus.OK);

                } catch (IOException e) {
                    logger.error("Failed to delete logo: ", e);
                    return new ResponseEntity<>("Failed to delete logo: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>("No logo found to delete", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Error deleting logo: ", e);
            return new ResponseEntity<>("An error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * REST API endpoint to update specific company field
     */
    @PatchMapping("/field/{fieldName}")
    public ResponseEntity<?> updateCompanyField(@PathVariable String fieldName,
                                                @RequestBody String fieldValue,
                                                HttpServletRequest request) {
        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
            }

            // Get recruiter ID from authenticated user
            Long recruiterId = getUserIdAsLong(request);

            Company company = companyService.getComapnyByRecruteurId(recruiterId);

            if (company == null) {
                return new ResponseEntity<>("Company not found", HttpStatus.NOT_FOUND);
            }

            // Create new company object with just the updated field
            Company updatedCompany = new Company();

            // Set the specific field based on fieldName
            switch (fieldName) {
                case "nomEntreprise":
                    updatedCompany.setNomEntreprise(fieldValue);
                    break;
                case "secteur":
                    updatedCompany.setSecteur(fieldValue);
                    break;
                case "tailleEntreprise":
                    updatedCompany.setTailleEntreprise(fieldValue);
                    break;
                case "anneeCreation":
                    try {
                        updatedCompany.setAnneeCreation(Integer.parseInt(fieldValue));
                    } catch (NumberFormatException e) {
                        return new ResponseEntity<>("Invalid year format", HttpStatus.BAD_REQUEST);
                    }
                    break;
                case "siteWeb":
                    updatedCompany.setSiteWeb(fieldValue);
                    break;
                case "emailContact":
                    updatedCompany.setEmailContact(fieldValue);
                    break;
                case "description":
                    updatedCompany.setDescription(fieldValue);
                    break;
                case "adresse":
                    updatedCompany.setAdresse(fieldValue);
                    break;
                case "ville":
                    updatedCompany.setVille(fieldValue);
                    break;
                case "pays":
                    updatedCompany.setPays(fieldValue);
                    break;
                case "codePostal":
                    updatedCompany.setCodePostal(fieldValue);
                    break;
                case "telephone":
                    updatedCompany.setTelephone(fieldValue);
                    break;
                case "numeroRC":
                    updatedCompany.setNumeroRC(fieldValue);
                    break;
                case "linkedinUrl":
                    updatedCompany.setLinkedinUrl(fieldValue);
                    break;
                case "twitterUrl":
                    updatedCompany.setTwitterUrl(fieldValue);
                    break;
                case "facebookUrl":
                    updatedCompany.setFacebookUrl(fieldValue);
                    break;
                case "instagramUrl":
                    updatedCompany.setInstagramUrl(fieldValue);
                    break;
                case "valeurs":
                    updatedCompany.setValeurs(fieldValue);
                    break;
                default:
                    return new ResponseEntity<>("Unknown field: " + fieldName, HttpStatus.BAD_REQUEST);
            }

            // Update only the specified field
            Company result = companyService.updateCompanyByRecruitId(recruiterId, updatedCompany);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating company field: ", e);
            return new ResponseEntity<>("An error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}