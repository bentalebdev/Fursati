package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.dto.*;
import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.entity.Document;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.CandidatProfileService;
import com.ismagi.Fursati.service.CandidatService;
import com.ismagi.Fursati.service.DemandeService;
import com.ismagi.Fursati.service.DocumentService;
import com.ismagi.Fursati.service.OffreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/candidats")
public class CandidatController {
    private static final Logger logger = Logger.getLogger(CandidatController.class.getName());

    @Autowired
    private CandidatService candidatService;

    @Autowired
    private CandidatProfileService candidatProfileService;

    @Autowired
    private OffreService offreService;

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private DocumentService documentService;

    // ===============================
    // CANDIDAT API ENDPOINTS
    // ===============================

    @GetMapping("/api")
    @ResponseBody
    public List<Candidat> getAllCandidats() {
        return candidatService.getAllCandidats();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Candidat getCandidatById(@PathVariable Long id) {
        return candidatService.getCandidatById(id);
    }

    @PostMapping("/api")
    @ResponseBody
    public Candidat createCandidat(@RequestBody Candidat candidat) {
        return candidatService.saveCandidat(candidat);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public void deleteCandidat(@PathVariable Long id) {
        candidatService.deleteCandidat(id);
    }

    // ===============================
    // DASHBOARD & NAVIGATION
    // ===============================

    @GetMapping({"", "/"})
    public String redirectToDashboard() {
        return "redirect:/candidats/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        logger.info("Loading dashboard page...");
        model.addAttribute("activeTab", "dashboard");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

    // ===============================
    // JOBS SECTION
    // ===============================

    @GetMapping("/jobs")
    public String jobs(Model model) {
        logger.info("Loading jobs page...");
        List<Offre> offres = offreService.getAllOffres();

        // If no offers found, provide an empty list instead of null
        if (offres == null) {
            offres = new ArrayList<>();
        }

        model.addAttribute("offres", offres);
        model.addAttribute("activeTab", "jobs");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

    @GetMapping("/jobs/details/{id}")
    public String jobDetails(@PathVariable Long id, Model model) {
        try {
            // Utiliser la méthode getOffreById pour récupérer l'offre réelle
            Offre offre = offreService.getOffreById(id);

            if (offre == null) {
                // Si l'offre n'est pas trouvée, on peut ajouter un attribut pour le signaler
                model.addAttribute("offreNotFound", true);
            }

            // Récupérer des offres similaires (par exemple, même type de contrat ou même secteur)
            List<Offre> similarOffers = new ArrayList<>();
            if (offre != null && offre.getContractType() != null) {
                // On pourrait implémenter une méthode dans le service qui trouve des offres similaires
                // similarOffers = offreService.findSimilarOffers(offre);

                // Pour l'instant, utiliser une liste vide
                similarOffers = new ArrayList<>();
            }

            // Vérifier si le candidat a déjà postulé à cette offre
            boolean hasApplied = false;
            Long candidatId = 1L; // À remplacer par le candidat connecté
            List<Demande> demandes = demandeService.getAllDemandes();
            if (demandes != null && !demandes.isEmpty() && offre != null) {
                hasApplied = demandes.stream()
                        .anyMatch(d -> d.getCandidat() != null &&
                                d.getCandidat().getId() != null &&
                                d.getCandidat().getId().equals(candidatId) &&
                                d.getOffre() != null &&
                                d.getOffre().getId() != null &&
                                d.getOffre().getId().equals(offre.getId()));
            }

            model.addAttribute("offre", offre);
            model.addAttribute("similarOffers", similarOffers);
            model.addAttribute("hasApplied", hasApplied);
            model.addAttribute("activeTab", "jobsdetails");
            return "candidateboard";
        } catch (Exception e) {
            // Log l'exception complète
            e.printStackTrace();
            // Ajouter un message d'erreur à afficher à l'utilisateur
            model.addAttribute("errorMessage", "Une erreur s'est produite : " + e.getMessage());
            model.addAttribute("activeTab", "jobs");
            return "candidateboard";
        }
    }

    // ===============================
    // APPLICATIONS SECTION
    // ===============================

    @GetMapping("/applications")
    public String applications(Model model) {
        logger.info("Loading applications page...");

        // Récupérer les demandes du candidat connecté
        Long candidatId = 1L; // À remplacer par le candidat connecté
        List<Demande> demandes = demandeService.getAllDemandes();

        // Filtrer pour ne montrer que les demandes du candidat connecté
        List<Demande> candidatDemandes = new ArrayList<>();
        if (demandes != null) {
            candidatDemandes = demandes.stream()
                    .filter(d -> d.getCandidat() != null &&
                            d.getCandidat().getId() != null &&
                            d.getCandidat().getId().equals(candidatId))
                    .toList();
        }

        model.addAttribute("demandes", candidatDemandes);
        model.addAttribute("activeTab", "applications");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

    // ===============================
    // PROFILE SECTION
    // ===============================

    @GetMapping("/profile")
    public String profile(Model model) {
        logger.info("Loading profile page...");

        model.addAttribute("activeTab", "profile");

        Candidat c = candidatProfileService.getCandidatById(1L);

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }
        model.addAttribute("candidat", c);

        return "candidateboard";
    }


    @PostMapping("/profile/basic-info")
    public String updateBasicInfo(
            @ModelAttribute BasicInfoDTO basicInfoDTO,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            RedirectAttributes redirectAttributes) {

        try {
            candidatProfileService.updateBasicInfo(1L, basicInfoDTO, profilePicture);
            redirectAttributes.addFlashAttribute("message", "Informations personnelles mises à jour avec succès!");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        } catch (Exception e) {
            logger.severe("Error updating basic info: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la mise à jour: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/candidats/profile";
    }

    @GetMapping("/profile/profilePicture/{id}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable Long id) {
        try {
            byte[] imageData = candidatProfileService.getProfilePictureData(id);
            if (imageData != null) {
                ByteArrayResource resource = new ByteArrayResource(imageData);

                Candidat candidat = candidatProfileService.getCandidatById(id);
                String contentType = "image/jpeg"; // Default

                // Try to determine content type from filename if available
                if (candidat.getProfilePicture() != null) {
                    String fileName = candidat.getProfilePicture();
                    if (fileName.toLowerCase().endsWith(".png")) {
                        contentType = "image/png";
                    } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    }
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            }
        } catch (Exception e) {
            logger.severe("Error retrieving profile picture: " + e.getMessage());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/profile/summary")
    public String updateSummary(@ModelAttribute SummaryDTO summaryDTO) {
        candidatProfileService.updateSummary(1L, summaryDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/experience")
    public String updateExperience(@ModelAttribute ExperienceListDTO experienceListDTO) {
        candidatProfileService.updateExperiences(1L, experienceListDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/experience/delete")
    public String deleteExperience(@RequestParam Long experienceId) {
        candidatProfileService.deleteExperience(experienceId);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/education")
    public String updateEducation(@ModelAttribute EducationListDTO educationListDTO) {
        candidatProfileService.updateEducations(1L, educationListDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/education/delete")
    public String deleteEducation(@RequestParam Long educationId) {
        candidatProfileService.deleteEducation(educationId);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/language")
    public String updateLanguage(@ModelAttribute LanguageListDTO languageListDTO) {
        // Add debug logging
        logger.info("Received language update request");
        if (languageListDTO != null && languageListDTO.getLanguages() != null && !languageListDTO.getLanguages().isEmpty()) {
            LanguageDTO lang = languageListDTO.getLanguages().get(0);
            logger.info("Language data: ID=" + lang.getId() + ", Name=" + lang.getName() + ", Level=" + lang.getLevel());
        } else {
            logger.warning("No language data received");
        }

        // Process the update
        candidatProfileService.updateLanguages(1L, languageListDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/language/delete")
    public String deleteLanguage(@RequestParam Long languageId) {
        candidatProfileService.deleteLanguage(languageId);
        return "redirect:/candidats/profile";
    }

    // ===============================
    // DOCUMENTS SECTION
    // ===============================

    @GetMapping("/documents")
    public String documents(Model model) {
        logger.info("Loading documents page...");

        // Hardcoded candidat ID for now - should come from authentication
        Long candidatId = 1L;

        List<Document> documents = documentService.getAllDocumentsByCandidatId(candidatId);
        model.addAttribute("documents", documents);
        model.addAttribute("activeTab", "documents");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

    @PostMapping("/documents/upload")
    public String uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentTitle") String title,
            @RequestParam(value = "defaultDocument", required = false, defaultValue = "false") boolean isDefault,
            RedirectAttributes redirectAttributes) {

        try {
            // Hardcoded candidat ID for now - should come from authentication
            Long candidatId = 1L;

            documentService.saveDocument(candidatId, file, documentType, title, isDefault);
            redirectAttributes.addFlashAttribute("message", "Document téléchargé avec succès!");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");

        } catch (Exception e) {
            logger.severe("Error uploading document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Erreur lors du téléchargement: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/candidats/documents";
    }

    @GetMapping("/documents/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Document document = documentService.getDocumentById(id);
            if (document != null) {
                ByteArrayResource resource = new ByteArrayResource(documentService.getDocumentBytes(id));

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                        .contentType(MediaType.parseMediaType(document.getContentType()))
                        .contentLength(document.getFileSize())
                        .body(resource);
            }
        } catch (IOException e) {
            logger.severe("Error downloading document: " + e.getMessage());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/documents/delete/{id}")
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentService.deleteDocument(id);
            redirectAttributes.addFlashAttribute("message", "Document supprimé avec succès!");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        } catch (IOException e) {
            logger.severe("Error deleting document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la suppression: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/candidats/documents";
    }

    @PostMapping("/documents/default/{id}")
    public String setAsDefaultDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentService.setAsDefault(id);
            redirectAttributes.addFlashAttribute("message", "Document défini comme principal!");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        } catch (Exception e) {
            logger.severe("Error setting default document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/candidats/documents";
    }

    @PostMapping("/documents/rename/{id}")
    public String renameDocument(
            @PathVariable Long id,
            @RequestParam("newTitle") String newTitle,
            RedirectAttributes redirectAttributes) {

        try {
            documentService.renameDocument(id, newTitle);
            redirectAttributes.addFlashAttribute("message", "Document renommé avec succès!");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        } catch (Exception e) {
            logger.severe("Error renaming document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/candidats/documents";
    }

    // ===============================
    // SETTINGS SECTION
    // ===============================

    @GetMapping("/settings")
    public String settings(Model model) {
        logger.info("Loading settings page...");

        model.addAttribute("activeTab", "settings");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }
    /**
     * Endpoint to handle the job application process
     * This should be added to CandidatController.java
     */
    @GetMapping("/jobs/apply/{id}")
    public String applyToJob(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            // Get job details
            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                redirectAttributes.addFlashAttribute("error", "L'offre d'emploi n'a pas été trouvée");
                return "redirect:/candidats/jobs";
            }

            // Get current user/candidate
            // For now, using hardcoded ID (should come from authentication in production)
            Long candidatId = 1L; // This should be replaced with actual authenticated user ID
            Candidat candidat = candidatService.getCandidatById(candidatId);

            if (candidat == null) {
                redirectAttributes.addFlashAttribute("error", "Votre profil n'a pas été trouvé. Veuillez vous reconnecter.");
                return "redirect:/login";
            }

            // Check if candidate has already applied to this job
            List<Demande> existingApplications = demandeService.getAllDemandes();
            boolean alreadyApplied = existingApplications.stream()
                    .anyMatch(d -> d.getCandidat() != null &&
                            d.getCandidat().getId().equals(candidatId) &&
                            d.getOffre() != null &&
                            d.getOffre().getId().equals(id));

            if (alreadyApplied) {
                redirectAttributes.addFlashAttribute("warning", "Vous avez déjà postulé à cette offre d'emploi");
                return "redirect:/candidats/jobs/details/" + id;
            }

            // Create new application
            Demande demande = new Demande();
            demande.setCandidat(candidat);
            demande.setOffre(offre);
            demande.setDateDemande(new Date());
            demande.setEtat("PENDING"); // Initial status

            // Save application
            demandeService.saveDemande(demande);

            // Add success message
            redirectAttributes.addFlashAttribute("success", "Votre candidature a été envoyée avec succès!");
            return "redirect:/candidats/applications"; // Redirect to applications page

        } catch (Exception e) {
            // Log the error
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de l'envoi de votre candidature. Veuillez réessayer plus tard.");
            return "redirect:/candidats/jobs";
        }
    }
}