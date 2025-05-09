package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.*;
import com.ismagi.Fursati.repository.CandidatRepository;
import com.ismagi.Fursati.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/recruteurs")
public class RecruteurController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(RecruteurController.class);

    @Autowired
    private OffreService offreService;

    @Autowired
    private RecruteurService recruteurService;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private CandidatService candidatService;

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private CompanyService companyService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        logger.info("Loading dashboard page...");

        try {
            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // 1. Récupérer les statistiques principales
            List<Offre> allOffres = offreService.findOffresByRecruteurIdRecruteur(recruteurId);
            long activeCount = allOffres.stream().filter(o -> "ACTIVE".equals(o.getStatus())).count();
            long draftCount = allOffres.stream().filter(o -> "DRAFT".equals(o.getStatus())).count();
            long expiredCount = allOffres.stream().filter(o -> "EXPIRED".equals(o.getStatus())).count();
            long closedCount = allOffres.stream().filter(o -> "CLOSED".equals(o.getStatus())).count();

            // 2. Récupérer les offres actives avec le nombre de candidatures
            List<Offre> activeOffres = allOffres.stream()
                    .filter(o -> "ACTIVE".equals(o.getStatus()))
                    .limit(4) // Limiter à 4 offres pour la page d'accueil
                    .collect(Collectors.toList());

            // 3. Récupérer les candidatures récentes
            List<Demande> allDemandes = demandeService.getDemandesByRecruitId(recruteurId);
            List<Demande> recentDemandes = allDemandes.stream()
                    .sorted((d1, d2) -> {
                        if (d1.getDateDemande() == null) return 1;
                        if (d2.getDateDemande() == null) return -1;
                        return d2.getDateDemande().compareTo(d1.getDateDemande());
                    })
                    .limit(5) // Limiter à 5 candidatures pour la page d'accueil
                    .collect(Collectors.toList());

            // 4. Récupérer les meilleurs candidats
            List<Candidat> allCandidats = candidatService.getAllCandidats();

            // 4.1 Dans une application réelle, vous utiliseriez un algorithme de correspondance
            // Ici, nous prenons simplement les 3 premiers pour la démo
            List<Candidat> topCandidats = allCandidats.stream()
                    .limit(3)
                    .collect(Collectors.toList());

            // 5. Calculer le nombre total de candidatures par statut
            long pendingCount = allDemandes.stream().filter(d -> "PENDING".equals(d.getEtat())).count();
            long reviewedCount = allDemandes.stream().filter(d -> "REVIEWED".equals(d.getEtat())).count();
            long interviewCount = allDemandes.stream().filter(d -> "INTERVIEW".equals(d.getEtat())).count();
            long rejectedCount = allDemandes.stream().filter(d -> "REJECTED".equals(d.getEtat())).count();
            long acceptedCount = allDemandes.stream().filter(d -> "ACCEPTED".equals(d.getEtat())).count();

            // 6. Ajouter le nombre de candidatures pour chaque offre active
            Map<Long, Long> offreApplicationCounts = new HashMap<>();
            for (Offre offre : activeOffres) {
                long count = allDemandes.stream()
                        .filter(d -> d.getOffre() != null && offre.getId().equals(d.getOffre().getId()))
                        .count();
                offreApplicationCounts.put(offre.getId(), count);
            }

            // 7. Ajouter tous ces éléments au modèle
            model.addAttribute("activeCount", activeCount);
            model.addAttribute("draftCount", draftCount);
            model.addAttribute("expiredCount", expiredCount);
            model.addAttribute("closedCount", closedCount);

            model.addAttribute("activeOffres", activeOffres);
            model.addAttribute("recentDemandes", recentDemandes);
            model.addAttribute("topCandidates", topCandidats);

            model.addAttribute("totalDemandes", allDemandes.size());
            model.addAttribute("totalCandidates", allCandidats.size());

            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("reviewedCount", reviewedCount);
            model.addAttribute("interviewCount", interviewCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("acceptedCount", acceptedCount);

            model.addAttribute("offreApplicationCounts", offreApplicationCounts);

            // Marquer le dashboard comme onglet actif
            model.addAttribute("activeTab", "dashboard");

            return "recruterboard";
        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("Error loading dashboard: ", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement du tableau de bord");
            model.addAttribute("activeTab", "dashboard");
            return "recruterboard";
        }
    }

    @GetMapping("/post-job")
    public String postjob(Model model, HttpServletRequest request) {
        logger.info("Loading post-job page...");

        // Verify user type is RECRUTEUR
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.RECRUTEUR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Get recruiter ID from authenticated user
        Long recruteurId = getUserIdAsLong(request);

        // Récupérer le recruteur
        Recruteur recruteur = recruteurService.getRecruteurById(recruteurId);

        // Récupérer l'entreprise associée au recruteur
        Company company = recruteur != null ? recruteur.getCompany() : null;

        // Créer une nouvelle offre
        Offre offre = new Offre();

        // Associer le recruteur à l'offre
        offre.setRecruteur(recruteur);

        // Ajouter les objets au modèle
        model.addAttribute("offre", offre);
        model.addAttribute("company", company);
        model.addAttribute("activeTab", "post-job");

        return "recruterboard";
    }

    @PostMapping("/create")
    public String createOffre(@ModelAttribute Offre offre,
                              @RequestParam(required = false) String responsibilitiesStr,
                              @RequestParam(required = false) String qualificationsStr,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            logger.debug("Starting createOffre method for recruiter ID: {}", recruteurId);
            logger.debug("Received job title: {}", offre.getTitle());

            // Récupérer le recruteur
            Recruteur recruteur = recruteurService.getRecruteurById(recruteurId);
            if (recruteur == null) {
                logger.error("CRITICAL ERROR - Recruiter with ID {} not found!", recruteurId);
                throw new RuntimeException("Recruiter not found");
            }

            // Récupérer l'entreprise associée au recruteur
            Company company = recruteur.getCompany();
            if (company == null) {
                throw new RuntimeException("Company information not found for this recruiter");
            }

            // Associer le recruteur à l'offre
            offre.setRecruteur(recruteur);

            // Process responsibilities
            if (responsibilitiesStr != null && !responsibilitiesStr.isEmpty()) {
                logger.debug("Processing responsibilities");
                List<String> responsibilities = new java.util.ArrayList<>();
                String[] respArray = responsibilitiesStr.split(",");
                logger.debug("Split responsibilities into {} items", respArray.length);
                for (String resp : respArray) {
                    if (!resp.trim().isEmpty()) {
                        responsibilities.add(resp.trim());
                    }
                }
                logger.debug("Final responsibilities count: {}", responsibilities.size());
                offre.setResponsibilities(responsibilities);
            }

            // Process qualifications
            if (qualificationsStr != null && !qualificationsStr.isEmpty()) {
                logger.debug("Processing qualifications");
                List<String> qualifications = new java.util.ArrayList<>();
                String[] qualArray = qualificationsStr.split(",");
                logger.debug("Split qualifications into {} items", qualArray.length);
                for (String qual : qualArray) {
                    if (!qual.trim().isEmpty()) {
                        qualifications.add(qual.trim());
                    }
                }
                logger.debug("Final qualifications count: {}", qualifications.size());
                offre.setQualifications(qualifications);
            }

            // Set default dates
            logger.debug("Setting dates");
            if (offre.getPostedAt() == null) {
                offre.setPostedAt(LocalDateTime.now());
            }
            if (offre.getExpiresAt() == null) {
                offre.setExpiresAt(LocalDateTime.now().plusDays(30));
            }

            // Save the offer
            logger.debug("About to save offer to database");
            Offre savedOffre = offreService.saveOffre(offre);
            logger.debug("Successfully saved offer with ID: {}", savedOffre.getId());

            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", "L'offre a été publiée avec succès.");
            return "redirect:/recruteurs/post-job";

        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("EXCEPTION TYPE: {}", e.getClass().getName());
            logger.error("EXCEPTION MESSAGE: {}", e.getMessage());
            logger.error("EXCEPTION STACKTRACE:", e);

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Une erreur s'est produite lors de l'enregistrement de l'offre: " + e.getMessage());
            return "redirect:/recruteurs/post-job";
        }
    }

    @GetMapping("/my-jobs")
    public String getMyOffres(Model model,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String status,
                              @RequestParam(defaultValue = "postedAt") String sortBy,
                              @RequestParam(defaultValue = "desc") String direction,
                              @RequestParam(required = false) String[] contractType,
                              @RequestParam(required = false) String[] workMode,
                              @RequestParam(required = false) String experienceLevel,
                              @RequestParam(required = false) String industry,
                              @RequestParam(required = false) String location,
                              @RequestParam(required = false) String dateFrom,
                              @RequestParam(required = false) String dateTo,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              HttpServletRequest request) {

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            // Add search parameters to model for form re-population
            model.addAttribute("currentKeyword", keyword);
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSort", sortBy);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            // Get all offers from service for this recruiter
            List<Offre> offres = offreService.findOffresByRecruteurIdRecruteur(recruteurId);

            // Apply filters if provided
            if (keyword != null && !keyword.isEmpty()) {
                offres = offres.stream()
                        .filter(o -> (o.getTitle() != null && o.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                                (o.getDescription() != null && o.getDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                                (o.getLocation() != null && o.getLocation().toLowerCase().contains(keyword.toLowerCase())))
                        .collect(Collectors.toList());
            }

            if (status != null && !status.isEmpty()) {
                offres = offres.stream()
                        .filter(o -> status.equals(o.getStatus()))
                        .collect(Collectors.toList());
            }

            if (contractType != null && contractType.length > 0) {
                offres = offres.stream()
                        .filter(o -> {
                            for (String type : contractType) {
                                if (type.equals(o.getContractType())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }

            if (workMode != null && workMode.length > 0) {
                offres = offres.stream()
                        .filter(o -> {
                            for (String mode : workMode) {
                                if (mode.equals(o.getWorkMode())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }

            if (experienceLevel != null && !experienceLevel.isEmpty()) {
                offres = offres.stream()
                        .filter(o -> experienceLevel.equals(o.getExperienceLevel()))
                        .collect(Collectors.toList());
            }

            if (industry != null && !industry.isEmpty()) {
                offres = offres.stream()
                        .filter(o -> industry.equals(o.getIndustry()))
                        .collect(Collectors.toList());
            }

            if (location != null && !location.isEmpty()) {
                offres = offres.stream()
                        .filter(o -> o.getLocation() != null && o.getLocation().toLowerCase().contains(location.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Apply date filters if provided
            if (dateFrom != null && !dateFrom.isEmpty()) {
                LocalDate fromDate = LocalDate.parse(dateFrom);
                offres = offres.stream()
                        .filter(o -> o.getPostedAt() != null && !o.getPostedAt().toLocalDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            }

            if (dateTo != null && !dateTo.isEmpty()) {
                LocalDate toDate = LocalDate.parse(dateTo);
                offres = offres.stream()
                        .filter(o -> o.getPostedAt() != null && !o.getPostedAt().toLocalDate().isAfter(toDate))
                        .collect(Collectors.toList());
            }

            // Add filtered job listings to the model
            model.addAttribute("offres", offres);
            model.addAttribute("activeTab", "my-jobs");

            // Add stats for different job statuses - calculate from the original list
            List<Offre> allOffres = offreService.findOffresByRecruteurIdRecruteur(recruteurId);
            long activeCount = allOffres.stream().filter(o -> "ACTIVE".equals(o.getStatus())).count();
            long draftCount = allOffres.stream().filter(o -> "DRAFT".equals(o.getStatus())).count();
            long expiredCount = allOffres.stream().filter(o -> "EXPIRED".equals(o.getStatus())).count();
            long closedCount = allOffres.stream().filter(o -> "CLOSED".equals(o.getStatus())).count();

            model.addAttribute("activeCount", activeCount);
            model.addAttribute("draftCount", draftCount);
            model.addAttribute("expiredCount", expiredCount);
            model.addAttribute("closedCount", closedCount);

            // Pagination data
            int totalItems = offres.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalItems);

            // If we have pagination, we need to subset the list
            if (totalItems > size) {
                offres = offres.subList(startIndex, endIndex);
                model.addAttribute("offres", offres);
            }

            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalOffres", totalItems);
            model.addAttribute("startIndex", startIndex);
            model.addAttribute("endIndex", endIndex);

            return "recruterboard";
        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("Error loading my-jobs: ", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement des offres d'emploi");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    @GetMapping("/candidates")
    public String candidates(Model model,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String[] skills,
                             @RequestParam(required = false) String experience,
                             @RequestParam(required = false) String location,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "8") int size,
                             HttpServletRequest request) {

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get recruiter ID from authenticated user - not used directly here
            // but good to have for authorization checks
            Long recruteurId = getUserIdAsLong(request);

            // Get all candidates
            List<Candidat> candidates = candidatService.getAllCandidats();

            // Apply filters if provided
            if (keyword != null && !keyword.isEmpty()) {
                candidates = candidates.stream()
                        .filter(c -> (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(keyword.toLowerCase())) ||
                                (c.getLastName() != null && c.getLastName().toLowerCase().contains(keyword.toLowerCase())) ||
                                (c.getSummary() != null && c.getSummary().toLowerCase().contains(keyword.toLowerCase())))
                        .collect(Collectors.toList());
            }

            if (skills != null && skills.length > 0) {
                candidates = candidates.stream()
                        .filter(c -> c.getSkills().stream()
                                .anyMatch(s -> {
                                    for (String skill : skills) {
                                        if (s.getName() != null && s.getName().equals(skill)) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }))
                        .collect(Collectors.toList());
            }

            if (experience != null && !experience.isEmpty()) {
                // Filter by experience level
                // Implementation depends on how you store/calculate experience
            }

            if (location != null && !location.isEmpty()) {
                candidates = candidates.stream()
                        .filter(c -> c.getAddress() != null && c.getAddress().toLowerCase().contains(location.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Pre-calculate values for all candidates to simplify template
            for (Candidat candidat : candidates) {
                // 1. Add a transient field to store the job title from the most recent experience
                if (candidat.getExperiences() != null && !candidat.getExperiences().isEmpty()) {
                    // Sort experiences by start date (most recent first)
                    candidat.getExperiences().sort((e1, e2) -> {
                        if (e1.getStartDate() == null) return 1;
                        if (e2.getStartDate() == null) return -1;
                        return e2.getStartDate().compareTo(e1.getStartDate());
                    });

                    // You can add these as attributes in the model instead of modifying the entity
                    model.addAttribute("currentJobTitle_" + candidat.getId(),
                            candidat.getExperiences().get(0).getJobTitle());

                    // 2. Calculate total experience years
                    int totalYears = 0;
                    for (Experience exp : candidat.getExperiences()) {
                        if (exp.getStartDate() != null) {
                            LocalDate endDate = exp.isCurrentJob() ?
                                    LocalDate.now() :
                                    (exp.getEndDate() != null ? exp.getEndDate() : LocalDate.now());

                            long years = java.time.temporal.ChronoUnit.YEARS.between(
                                    exp.getStartDate(), endDate);
                            totalYears += years;
                        }
                    }
                    model.addAttribute("totalExperienceYears_" + candidat.getId(), totalYears);
                }

                // 3. Find highest education
                if (candidat.getEducations() != null && !candidat.getEducations().isEmpty()) {
                    Education highestEdu = null;
                    int maxYear = -1;

                    for (Education edu : candidat.getEducations()) {
                        if (edu.getEndYear() != null && edu.getEndYear() > maxYear) {
                            maxYear = edu.getEndYear();
                            highestEdu = edu;
                        }
                    }

                    if (highestEdu != null) {
                        model.addAttribute("highestEducation_" + candidat.getId(), highestEdu);
                    }
                }
            }

            // Calculate pagination data
            int totalItems = candidates.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalItems);

            // Subset the list for pagination
            if (totalItems > size) {
                candidates = candidates.subList(startIndex, endIndex);
            }

            // Add all data to model
            model.addAttribute("candidates", candidates);
            model.addAttribute("totalCandidates", totalItems);
            model.addAttribute("startIndex", startIndex);
            model.addAttribute("endIndex", endIndex);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("currentKeyword", keyword);
            model.addAttribute("currentSkills", skills);
            model.addAttribute("currentExperience", experience);
            model.addAttribute("currentLocation", location);
            model.addAttribute("activeTab", "candidates");

            return "recruterboard";

        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("Error loading candidates page: ", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement des candidats");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    @GetMapping("/applications")
    public String applications(Model model,
                               @RequestParam(required = false) Long offreId,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String dateRange,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               HttpServletRequest request) {

        logger.info("Loading applications page...");

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            // Get all applications (demandes) for this recruiter
            List<Demande> demandes = demandeService.getDemandesByRecruitId(recruteurId);

            // Apply filters if provided
            if (offreId != null) {
                demandes = demandes.stream()
                        .filter(d -> d.getOffre().getId().equals(offreId))
                        .collect(Collectors.toList());
                model.addAttribute("filteredOffreId", offreId);
            }

            if (status != null && !status.isEmpty()) {
                demandes = demandes.stream()
                        .filter(d -> d.getEtat().equals(status))
                        .collect(Collectors.toList());
            }

            if (dateRange != null && !dateRange.isEmpty()) {
                // Filter by date range
                LocalDate now = LocalDate.now();
                LocalDate startDate = now;

                switch (dateRange) {
                    case "today":
                        // Keep startDate as today
                        break;
                    case "week":
                        startDate = now.minusWeeks(1);
                        break;
                    case "month":
                        startDate = now.minusMonths(1);
                        break;
                    case "quarter":
                        startDate = now.minusMonths(3);
                        break;
                }

                final LocalDate filterStartDate = startDate;
                demandes = demandes.stream()
                        .filter(d -> {
                            LocalDate demandDate = new java.sql.Date(d.getDateDemande().getTime()).toLocalDate();
                            return !demandDate.isBefore(filterStartDate);
                        })
                        .collect(Collectors.toList());
            }

            if (keyword != null && !keyword.isEmpty()) {
                demandes = demandes.stream()
                        .filter(d -> (d.getCandidat().getFirstName() != null &&
                                d.getCandidat().getFirstName().toLowerCase().contains(keyword.toLowerCase())) ||
                                (d.getCandidat().getLastName() != null &&
                                        d.getCandidat().getLastName().toLowerCase().contains(keyword.toLowerCase())) ||
                                (d.getCandidat().getEmail() != null &&
                                        d.getCandidat().getEmail().toLowerCase().contains(keyword.toLowerCase())))
                        .collect(Collectors.toList());
            }

            // Calculate statistics
            long nouveauCount = demandes.stream().filter(d -> "PENDING".equals(d.getEtat())).count();
            long enCoursCount = demandes.stream().filter(d -> "REVIEWED".equals(d.getEtat())).count();
            long refuseCount = demandes.stream().filter(d -> "REJECTED".equals(d.getEtat())).count();

            // Calculate match percentages for each application
            Map<Long, Integer> matchPercentages = new HashMap<>();
            for (Demande demande : demandes) {
                // Calculate a match percentage based on candidate skills vs job requirements
                // This is a simplified example - you would implement your own matching algorithm
                Candidat candidat = demande.getCandidat();
                Offre offre = demande.getOffre();

                // Simple match calculation based on skills count (for demonstration)
                double percentage = 50; // Base percentage

                if (candidat.getSkills() != null && !candidat.getSkills().isEmpty()) {
                    // Add up to 40% based on skills
                    percentage += Math.min(40, candidat.getSkills().size() * 5);
                }

                // Add some randomness for demonstration
                percentage += (Math.random() * 10);

                // Ensure it's between 0-100
                int matchPercentage = (int) Math.min(100, Math.max(0, percentage));
                matchPercentages.put(demande.getIdDemande(), matchPercentage);
            }

            // Pagination
            int totalItems = demandes.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalItems);

            if (totalItems > size) {
                demandes = demandes.subList(startIndex, endIndex);
            }

            // Get all job offers for filter - for this recruiter only
            List<Offre> offres = offreService.findOffresByRecruteurIdRecruteur(recruteurId);

            // Add all data to model
            model.addAttribute("demandes", demandes);
            model.addAttribute("offres", offres);
            model.addAttribute("totalDemandes", totalItems);
            model.addAttribute("nouveauCount", nouveauCount);
            model.addAttribute("enCoursCount", enCoursCount);
            model.addAttribute("refuseCount", refuseCount);
            model.addAttribute("matchPercentages", matchPercentages);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("activeTab", "applications");

            return "recruterboard";

        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("Error loading applications page: ", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement des candidatures");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    @GetMapping("/company")
    public String company(Model model, HttpServletRequest request) {
        logger.info("Loading company page...");

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            Company company = companyService.getComapnyByRecruteurId(recruteurId);

            logger.debug("Retrieved company: {}", company);
            model.addAttribute("company", company);
            model.addAttribute("activeTab", "company");
            return "recruterboard";

        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("Error loading company page: ", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement des informations de l'entreprise");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    // API endpoints for job actions
    @DeleteMapping("/api/offres/{id}")
    @ResponseBody
    public Map<String, Object> deleteOffreApi(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                response.put("success", false);
                response.put("error", "Access denied");
                return response;
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            // Verify that this offre belongs to the recruiter
            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                response.put("success", false);
                response.put("error", "Offre not found");
                return response;
            }

            if (offre.getRecruteur() == null || !recruteurId.equals(offre.getRecruteur().getIdRecruteur())) {
                response.put("success", false);
                response.put("error", "You can only delete your own job offers");
                return response;
            }

            offreService.deleteOffre(id);
            response.put("success", true);
            return response;

        } catch (Exception e) {
            logger.error("Error deleting offer: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    @PutMapping("/api/offres/{id}/status")
    @ResponseBody
    public Map<String, Object> updateOffreStatus(@PathVariable Long id,
                                                 @RequestBody StatusUpdateRequest request,
                                                 HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(httpRequest);
            if (userType != AuthService.UserType.RECRUTEUR) {
                response.put("success", false);
                response.put("error", "Access denied");
                return response;
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(httpRequest);

            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                response.put("success", false);
                response.put("error", "Offre non trouvée");
                return response;
            }

            // Verify that this offre belongs to the recruiter
            if (offre.getRecruteur() == null || !recruteurId.equals(offre.getRecruteur().getIdRecruteur())) {
                response.put("success", false);
                response.put("error", "You can only update your own job offers");
                return response;
            }

            offre.setStatus(request.getStatus());
            offreService.saveOffre(offre);
            response.put("success", true);
            return response;

        } catch (Exception e) {
            logger.error("Error updating offer status: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    @PostMapping("/api/offres/{id}/renew")
    @ResponseBody
    public Map<String, Object> renewOffre(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                response.put("success", false);
                response.put("error", "Access denied");
                return response;
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                response.put("success", false);
                response.put("error", "Offre non trouvée");
                return response;
            }

            // Verify that this offre belongs to the recruiter
            if (offre.getRecruteur() == null || !recruteurId.equals(offre.getRecruteur().getIdRecruteur())) {
                response.put("success", false);
                response.put("error", "You can only renew your own job offers");
                return response;
            }

            // Logique de renouvellement: réinitialiser le statut et mettre à jour les dates
            offre.setStatus("ACTIVE");
            offre.setPostedAt(java.time.LocalDateTime.now());
            offre.setExpiresAt(java.time.LocalDateTime.now().plusDays(30)); // 30 jours par défaut
            offreService.saveOffre(offre);

            response.put("success", true);
            return response;

        } catch (Exception e) {
            logger.error("Error renewing offer: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    @GetMapping("/duplicate-job/{id}")
    public String duplicateJob(@PathVariable Long id, Model model, HttpServletRequest request) {
        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            Offre original = offreService.getOffreById(id);
            if (original == null) {
                model.addAttribute("errorMessage", "Offre non trouvée");
                return "fragments/error-fragment :: error-fragment";
            }

            // Verify that this offre belongs to the recruiter
            if (original.getRecruteur() == null || !recruteurId.equals(original.getRecruteur().getIdRecruteur())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only duplicate your own job offers");
            }

            // Créer une copie de l'offre originale
            Offre copy = new Offre();
            copy.setTitle(original.getTitle() + " (Copie)");
            copy.setDescription(original.getDescription());
            copy.setLocation(original.getLocation());

            copy.setContractType(original.getContractType());
            copy.setWorkMode(original.getWorkMode());
            copy.setIndustry(original.getIndustry());
            copy.setExperienceLevel(original.getExperienceLevel());
            copy.setMinSalary(original.getMinSalary());
            copy.setMaxSalary(original.getMaxSalary());
            copy.setStatus("DRAFT"); // Toujours commencer comme brouillon
            copy.setRecruteur(original.getRecruteur());

            // Ajouter l'offre copiée au modèle
            model.addAttribute("offre", copy);
            model.addAttribute("activeTab", "post-job");
            model.addAttribute("isDuplicate", true);
            model.addAttribute("originalOffreId", id);

            return "recruterboard";
        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("Error duplicating job: ", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la duplication de l'offre");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    @GetMapping("/{id}")
    public Recruteur getRecruteurById(@PathVariable Long id, HttpServletRequest request) {
        // Verify user type is ADMIN or RECRUTEUR
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType == AuthService.UserType.ADMIN) {
            // Admin can access any recruiter
            return recruteurService.getRecruteurById(id);
        } else if (userType == AuthService.UserType.RECRUTEUR) {
            // Recruiter can only access their own profile
            Long authenticatedId = getUserIdAsLong(request);
            if (!authenticatedId.equals(id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own profile");
            }
            return recruteurService.getRecruteurById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    @GetMapping
    public List<Recruteur> getAllRecruteurs(HttpServletRequest request) {
        // Only ADMIN can list all recruiters
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can list all recruiters");
        }
        return recruteurService.getAllRecruteurs();
    }

    @PostMapping
    public Recruteur createRecruteur(@RequestBody Recruteur recruteur, HttpServletRequest request) {
        // Only ADMIN can create recruiters
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can create recruiters");
        }
        return recruteurService.saveRecruteur(recruteur);
    }

    @DeleteMapping("/{id}")
    public void deleteRecruteur(@PathVariable Long id, HttpServletRequest request) {
        // Only ADMIN can delete recruiters
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType != AuthService.UserType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can delete recruiters");
        }
        recruteurService.deleteRecruteur(id);
    }

    @GetMapping("/candidate-profile/{id}")
    public String viewCandidateProfile(@PathVariable Long id,
                                       @RequestParam(required = false) Long offreId,
                                       Model model,
                                       HttpServletRequest request) {
        try {
            // Verify user type is RECRUTEUR
            AuthService.UserType userType = getAuthenticatedUserType(request);
            if (userType != AuthService.UserType.RECRUTEUR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Get recruiter ID from authenticated user
            Long recruteurId = getUserIdAsLong(request);

            // Get the candidate
            Candidat candidat = candidatService.getCandidatById(id);
            if (candidat == null) {
                model.addAttribute("errorMessage", "Candidat non trouvé");
                return "fragments/error-fragment :: error-fragment";
            }

            // Add candidate to model
            model.addAttribute("candidat", candidat);

            // Calculate match score if an offer is specified
            if (offreId != null) {
                Offre offre = offreService.getOffreById(offreId);
                if (offre != null) {
                    // Verify that this offre belongs to the recruiter
                    if (offre.getRecruteur() == null || !recruteurId.equals(offre.getRecruteur().getIdRecruteur())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only match candidates with your own job offers");
                    }

                    // This is a simplified match calculation example
                    // In a real application, you would implement a more sophisticated algorithm
                    int matchScore = calculateMatchScore(candidat, offre);
                    model.addAttribute("matchScore", matchScore);
                    model.addAttribute("matchingJobTitle", offre.getTitle());
                }
            }

            // Get active job offers for comparison options - only for this recruiter
            List<Offre> activeOffres = offreService.findOffresByRecruteurIdRecruteur(recruteurId).stream()
                    .filter(o -> "ACTIVE".equals(o.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("offres", activeOffres);

            // Set the active tab to track navigation
            model.addAttribute("activeTab", "candidate-profile");

            return "recruterboard";
        } catch (ResponseStatusException rse) {
            throw rse; // Rethrow authorization exceptions
        } catch (Exception e) {
            logger.error("Error loading candidate profile: ", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement du profil du candidat");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    /**
     * Calculate a match score between a candidate and a job offer
     * This is a simplified implementation for demonstration purposes
     */
    private int calculateMatchScore(Candidat candidat, Offre offre) {
        // Base score
        int score = 50;

        // Check for matching skills (simplified)
        if (candidat.getSkills() != null && !candidat.getSkills().isEmpty()) {
            // In a real implementation, you would compare the skills with the job requirements
            // For demo, we'll just add points based on skills count
            score += Math.min(30, candidat.getSkills().size() * 5);
        }

        // Check for matching experience level (simplified)
        if (!candidat.getExperiences().isEmpty()) {
            String expLevel = offre.getExperienceLevel();
            int candidateYears = calculateTotalExperienceYears(candidat);

            if ("SENIOR".equals(expLevel) && candidateYears >= 5) {
                score += 10;
            } else if ("MID".equals(expLevel) && candidateYears >= 2 && candidateYears < 5) {
                score += 10;
            } else if ("JUNIOR".equals(expLevel) && candidateYears < 2) {
                score += 10;
            }
        }

        // Add some randomness for demonstration (remove in production)
        score += (int)(Math.random() * 10);

        // Ensure score is between 0-100
        return Math.min(100, Math.max(0, score));
    }

    /**
     * Calculate the total years of experience for a candidate
     */
    private int calculateTotalExperienceYears(Candidat candidat) {
        int totalYears = 0;

        for (Experience exp : candidat.getExperiences()) {
            if (exp.getStartDate() != null) {
                LocalDate endDate = exp.isCurrentJob() ?
                        LocalDate.now() :
                        (exp.getEndDate() != null ? exp.getEndDate() : LocalDate.now());

                long years = java.time.temporal.ChronoUnit.YEARS.between(
                        exp.getStartDate(), endDate);
                totalYears += years;
            }
        }

        return totalYears;
    }

    // Helper class for status update requests
    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}