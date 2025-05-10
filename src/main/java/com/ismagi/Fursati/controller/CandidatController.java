package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.dto.*;
import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.entity.Document;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.AuthService;
import com.ismagi.Fursati.service.CandidatProfileService;
import com.ismagi.Fursati.service.CandidatService;
import com.ismagi.Fursati.service.DemandeService;
import com.ismagi.Fursati.service.DocumentService;
import com.ismagi.Fursati.service.OffreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/candidats")
public class CandidatController extends BaseController {
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
    public List<Candidat> getAllCandidats(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check if user is authenticated and has admin rights
        if (!isAuthenticated(request)) {
            response.sendRedirect("/login");
            return null;
        }

        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return candidatService.getAllCandidats();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Candidat getCandidatById(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify user is allowed to access this data
        try {
            verifyAuthenticatedUser(request, id);
            return candidatService.getCandidatById(id);
        } catch (ResponseStatusException e) {
            throw e;
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public Candidat createCandidat(@RequestBody Candidat candidat, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check if user is authenticated and has admin rights
        if (!checkAuthentication(request, response)) {
            return null;
        }

        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return candidatService.saveCandidat(candidat);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public void deleteCandidat(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check if user is authenticated and has admin rights
        if (!checkAuthentication(request, response)) {
            return;
        }

        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

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
    public String dashboard(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Loading dashboard page...");

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify user is a candidate
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.CANDIDAT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

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
    public String jobs(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String location,
                       @RequestParam(required = false) String[] contractType,
                       @RequestParam(required = false) String[] experienceLevel,
                       @RequestParam(required = false) String industry,
                       @RequestParam(required = false) String dateRange,
                       @RequestParam(required = false) String sortBy,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {

        logger.info("Loading jobs page...");

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify user is a candidate
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.CANDIDAT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Get all offers from the service
        List<Offre> offres = offreService.getAllOffres();

        // Apply filters if provided
        // Here you can implement your filtering logic based on the parameters
        // This is just a simplified example
        if (keyword != null && !keyword.isEmpty()) {
            offres = offres.stream()
                    .filter(o -> (o.getTitle() != null && o.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                            (o.getDescription() != null && o.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                    .collect( Collectors.toList());
        }

        // Calculate pagination
        int totalItems = offres.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Set page within bounds
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        // Get the subset of offers for the current page
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        List<Offre> pagedOffres = start < totalItems ? offres.subList(start, end) : new ArrayList<>();

        // Add all necessary attributes to the model
        model.addAttribute("offres", pagedOffres);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("activeTab", "jobs");

        return "candidateboard";
    }
    @GetMapping("/jobs/details/{id}")
    public String jobDetails(@PathVariable Long id, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Check authentication
            if (!checkAuthentication(request, response)) {
                return null;
            }

            // Verify user is a candidate
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.CANDIDAT) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get the authenticated candidate ID
            Long candidatId = getUserIdAsLong(request);

            // Get offer details
            Offre offre = offreService.getOffreById(id);

            if (offre == null) {
                // If the offer is not found, add an attribute to indicate this
                model.addAttribute("offreNotFound", true);
            }

            // Get similar offers
            List<Offre> similarOffers = new ArrayList<>();
            if (offre != null && offre.getContractType() != null) {
                // You could implement a method in the service that finds similar offers
                // similarOffers = offreService.findSimilarOffers(offre);
                similarOffers = new ArrayList<>();
            }

            // Check if the candidate has already applied for this job
            boolean hasApplied = false;
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
            // Log the complete exception
            e.printStackTrace();
            // Add an error message to display to the user
            model.addAttribute("errorMessage", "Une erreur s'est produite : " + e.getMessage());
            model.addAttribute("activeTab", "jobs");
            return "candidateboard";
        }
    }

    // ===============================
    // APPLICATIONS SECTION
    // ===============================

    @GetMapping("/applications")
    public String applications(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Loading applications page...");

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify user is a candidate
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.CANDIDAT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        // Get the candidate's applications
        List<Demande> demandes = demandeService.getAllDemandes();

        // Filter to show only the connected candidate's applications
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
    public String profile(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Loading profile page...");

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify user is a candidate
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.CANDIDAT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        model.addAttribute("activeTab", "profile");

        Candidat c = candidatProfileService.getCandidatById(candidatId);

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
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        try {
            candidatProfileService.updateBasicInfo(candidatId, basicInfoDTO, profilePicture);
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
    public ResponseEntity<Resource> getProfilePicture(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Verify user is allowed to access this picture
            verifyAuthenticatedUser(request, id);

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
    public String updateSummary(@ModelAttribute SummaryDTO summaryDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        candidatProfileService.updateSummary(candidatId, summaryDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/experience")
    public String updateExperience(@ModelAttribute ExperienceListDTO experienceListDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        candidatProfileService.updateExperiences(candidatId, experienceListDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/experience/delete")
    public String deleteExperience(@RequestParam Long experienceId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify the experience belongs to the authenticated user
        // This would require an additional service method to check ownership
        // For now, we'll just check authentication

        candidatProfileService.deleteExperience(experienceId);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/education")
    public String updateEducation(@ModelAttribute EducationListDTO educationListDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        candidatProfileService.updateEducations(candidatId, educationListDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/education/delete")
    public String deleteEducation(@RequestParam Long educationId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify the education belongs to the authenticated user
        // This would require an additional service method to check ownership
        // For now, we'll just check authentication

        candidatProfileService.deleteEducation(educationId);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/language")
    public String updateLanguage(@ModelAttribute LanguageListDTO languageListDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        // Add debug logging
        logger.info("Received language update request");
        if (languageListDTO != null && languageListDTO.getLanguages() != null && !languageListDTO.getLanguages().isEmpty()) {
            LanguageDTO lang = languageListDTO.getLanguages().get(0);
            logger.info("Language data: ID=" + lang.getId() + ", Name=" + lang.getName() + ", Level=" + lang.getLevel());
        } else {
            logger.warning("No language data received");
        }

        // Process the update
        candidatProfileService.updateLanguages(candidatId, languageListDTO);
        return "redirect:/candidats/profile";
    }

    @PostMapping("/profile/language/delete")
    public String deleteLanguage(@RequestParam Long languageId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify the language belongs to the authenticated user
        // This would require an additional service method to check ownership
        // For now, we'll just check authentication

        candidatProfileService.deleteLanguage(languageId);
        return "redirect:/candidats/profile";
    }

    // ===============================
    // DOCUMENTS SECTION
    // ===============================

    @GetMapping("/documents")
    public String documents(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Loading documents page...");

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify user is a candidate
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.CANDIDAT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

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
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Get the authenticated candidate ID
        Long candidatId = getUserIdAsLong(request);

        try {
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
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Check authentication
            if (!checkAuthentication(request, response)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Document document = documentService.getDocumentById(id);
            if (document != null) {
                // Verify the document belongs to the authenticated user
                Long candidatId = getUserIdAsLong(request);
                if (!document.getCandidat().getId().equals(candidatId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only download your own documents");
                }

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
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        try {
            // Verify the document belongs to the authenticated user
            Document document = documentService.getDocumentById(id);
            if (document != null) {
                Long candidatId = getUserIdAsLong(request);
                if (!document.getCandidat().getId().equals(candidatId)) {
                    redirectAttributes.addFlashAttribute("message", "Vous ne pouvez supprimer que vos propres documents!");
                    redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                    return "redirect:/candidats/documents";
                }

                documentService.deleteDocument(id);
                redirectAttributes.addFlashAttribute("message", "Document supprimé avec succès!");
                redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            }
        } catch (IOException e) {
            logger.severe("Error deleting document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la suppression: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/candidats/documents";
    }

    @PostMapping("/documents/default/{id}")
    public String setAsDefaultDocument(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        try {
            // Verify the document belongs to the authenticated user
            Document document = documentService.getDocumentById(id);
            if (document != null) {
                Long candidatId = getUserIdAsLong(request);
                if (!document.getCandidat().getId().equals(candidatId)) {
                    redirectAttributes.addFlashAttribute("message", "Vous ne pouvez modifier que vos propres documents!");
                    redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                    return "redirect:/candidats/documents";
                }

                documentService.setAsDefault(id);
                redirectAttributes.addFlashAttribute("message", "Document défini comme principal!");
                redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            }
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
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        try {
            // Verify the document belongs to the authenticated user
            Document document = documentService.getDocumentById(id);
            if (document != null) {
                Long candidatId = getUserIdAsLong(request);
                if (!document.getCandidat().getId().equals(candidatId)) {
                    redirectAttributes.addFlashAttribute("message", "Vous ne pouvez modifier que vos propres documents!");
                    redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                    return "redirect:/candidats/documents";
                }

                documentService.renameDocument(id, newTitle);
                redirectAttributes.addFlashAttribute("message", "Document renommé avec succès!");
                redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            }
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
    public String settings(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Loading settings page...");

        // Check authentication
        if (!checkAuthentication(request, response)) {
            return null;
        }

        // Verify user is a candidate
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.CANDIDAT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        model.addAttribute("activeTab", "settings");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

    /**
     * Endpoint to handle the job application process
     */
    @GetMapping("/jobs/apply/{id}")
    public String applyToJob(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Check authentication
            if (!checkAuthentication(request, response)) {
                return null;
            }

            // Verify user is a candidate
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.CANDIDAT) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get job details
            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                redirectAttributes.addFlashAttribute("error", "L'offre d'emploi n'a pas été trouvée");
                return "redirect:/candidats/jobs";
            }

            // Get current authenticated candidate
            Long candidatId = getUserIdAsLong(request);
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
            logger.severe("Error applying to job: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de l'envoi de votre candidature. Veuillez réessayer plus tard.");
            return "redirect:/candidats/jobs";
        }
    }
}