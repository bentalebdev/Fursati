package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.entity.Recruteur;
import com.ismagi.Fursati.service.OffreService;
import com.ismagi.Fursati.service.RecruteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Controller
@RequestMapping("/recruteurs")
public class RecruteurController {
    private static final Logger logger = Logger.getLogger(CandidatController.class.getName());

    @Autowired
    private OffreService offreService;

    @Autowired
    private RecruteurService recruteurService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        logger.info("Loading dashboard page...");
        model.addAttribute("activeTab", "dashboard");
        return "recruterboard";
    }

    @GetMapping("/post-job")
    public String postjob(Model model) {
        logger.info("Loading post-job page...");
        model.addAttribute("activeTab", "post-job");
        model.addAttribute("offre", new Offre());
        return "recruterboard";
    }

    @GetMapping("/my-jobs")
    public String getMyOffres(
            Model model,
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
            @RequestParam(defaultValue = "10") int size) {

        try {
            Long recruteurId = 1L; // In production, get from authenticated user

            // Add search parameters to model for form re-population
            model.addAttribute("currentKeyword", keyword);
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSort", sortBy);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            // Get all offers from service
            List<Offre> offres = offreService.findOffresByRecruteurIdRecruteur(1l);

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
        } catch (Exception e) {
            logger.severe("Error loading my-jobs: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement des offres d'emploi");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    @GetMapping("/candidates")
    public String candidates(Model model) {
        logger.info("Loading candidates page...");
        model.addAttribute("activeTab", "candidates");
        return "recruterboard";
    }

    @GetMapping("/applications")
    public String applications(Model model, @RequestParam(required = false) Long offreId) {
        logger.info("Loading applications page...");
        model.addAttribute("activeTab", "applications");
        if (offreId != null) {
            model.addAttribute("filteredOffreId", offreId);
        }
        return "recruterboard";
    }

    @GetMapping("/company")
    public String company(Model model) {
        logger.info("Loading company page...");
        model.addAttribute("activeTab", "company");
        return "recruterboard";
    }

    // API endpoints for job actions
    @DeleteMapping("/api/offres/{id}")
    @ResponseBody
    public String deleteOffreApi(@PathVariable Long id) {
        try {
            offreService.deleteOffre(id);
            return "{'success': true}";
        } catch (Exception e) {
            logger.severe("Error deleting offer: " + e.getMessage());
            return "{'success': false, 'error': '" + e.getMessage() + "'}";
        }
    }

    @PutMapping("/api/offres/{id}/status")
    @ResponseBody
    public String updateOffreStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        try {
            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                return "{'success': false, 'error': 'Offre non trouvée'}";
            }

            offre.setStatus(request.getStatus());
            offreService.saveOffre(offre);
            return "{'success': true}";
        } catch (Exception e) {
            logger.severe("Error updating offer status: " + e.getMessage());
            return "{'success': false, 'error': '" + e.getMessage() + "'}";
        }
    }

    @PostMapping("/api/offres/{id}/renew")
    @ResponseBody
    public String renewOffre(@PathVariable Long id) {
        try {
            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                return "{'success': false, 'error': 'Offre non trouvée'}";
            }

            // Logique de renouvellement: réinitialiser le statut et mettre à jour les dates
            offre.setStatus("ACTIVE");
            offre.setPostedAt(java.time.LocalDateTime.now());
            offre.setExpiresAt(java.time.LocalDateTime.now().plusDays(30)); // 30 jours par défaut
            offreService.saveOffre(offre);

            return "{'success': true}";
        } catch (Exception e) {
            logger.severe("Error renewing offer: " + e.getMessage());
            return "{'success': false, 'error': '" + e.getMessage() + "'}";
        }
    }

    @GetMapping("/duplicate-job/{id}")
    public String duplicateJob(@PathVariable Long id, Model model) {
        try {
            Offre original = offreService.getOffreById(id);
            if (original == null) {
                model.addAttribute("errorMessage", "Offre non trouvée");
                return "fragments/error-fragment :: error-fragment";
            }

            // Créer une copie de l'offre originale
            Offre copy = new Offre();
            copy.setTitle(original.getTitle() + " (Copie)");
            copy.setDescription(original.getDescription());
            copy.setLocation(original.getLocation());
            copy.setCompanyName(original.getCompanyName());
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
        } catch (Exception e) {
            logger.severe("Error duplicating job: " + e.getMessage());
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la duplication de l'offre");
            return "fragments/error-fragment :: error-fragment";
        }
    }

    @GetMapping("/{id}")
    public Recruteur getRecruteurById(@PathVariable Long id) {
        return recruteurService.getRecruteurById(id);
    }

    @GetMapping
    public List<Recruteur> getAllRecruteurs() {
        return recruteurService.getAllRecruteurs();
    }

    @PostMapping
    public Recruteur createRecruteur(@RequestBody Recruteur recruteur) {
        return recruteurService.saveRecruteur(recruteur);
    }

    @DeleteMapping("/{id}")
    public void deleteRecruteur(@PathVariable Long id) {
        recruteurService.deleteRecruteur(id);
    }

    // Classe auxiliaire pour les requêtes de mise à jour de statut
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