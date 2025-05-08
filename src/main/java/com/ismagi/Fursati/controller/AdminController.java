package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.*;
import com.ismagi.Fursati.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private OffreService offreService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CandidatService candidatService;
    @Autowired
    private DemandeService demandeService;


    @GetMapping
    public String showAdmin(Model model) {
        model.addAttribute("activeTab", "dashboard");
        return "adminboard";
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("activeTab", "dashboard");
        // Add dashboard data...
        return "adminboard";
    }

    @GetMapping("/candidats")
    public String showAdminUsers(Model model) {
        model.addAttribute("candidats",candidatService.getAllCandidats());
        model.addAttribute("activeTab", "candidats");
        //
        return "adminboard";
    }

    @GetMapping("/companies")
    public String showAdminCompanies(Model model) {
        List<Company> companyList=companyService.getCompanies();
        model.addAttribute("companies", companyList);
        model.addAttribute("activeTab", "companies");
        return "adminboard";
    }
    @GetMapping("/jobs")
    public String showAdminJobs(Model model) {
       List< Offre> offres =  offreService.getAllOffres();
        model.addAttribute("jobs", offres);
        model.addAttribute("activeTab", "jobs");

        return "adminboard";
    }
    @GetMapping("/applications")
    public String showAdminApplications(Model model) {
        List<Demande> demandes = demandeService.getAllDemandes();

        // Set the applications list
        model.addAttribute("applications", demandes);

        // Calculate and add counts for each status based on English values in database
        long pendingCount = demandes.stream().filter(d -> "PENDING".equals(d.getEtat())).count();
        long interviewCount = demandes.stream().filter(d -> "INTERVIEW".equals(d.getEtat())).count();
        long shortlistedCount = demandes.stream().filter(d -> "SHORTLISTED".equals(d.getEtat())).count();
        long rejectedCount = demandes.stream().filter(d -> "REJECTED".equals(d.getEtat())).count();
        long reviewedCount = demandes.stream().filter(d -> "REVIEWED".equals(d.getEtat())).count();

        // Add the counts to the model
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", interviewCount + reviewedCount); // Combine interview and reviewed for "In Progress"
        model.addAttribute("selectedCount", shortlistedCount); // SHORTLISTED is mapped to "Sélectionnés"
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("reviewedCount", reviewedCount);
        model.addAttribute("interviewCount", interviewCount);

        // Add total count for convenience
        model.addAttribute("totalCount", demandes.size());

        // Extract unique companies for the filter (in a safe way)
        Set<Company> uniqueCompanies = new HashSet<>();
        for (Demande demande : demandes) {
            if (demande.getOffre() != null &&
                    demande.getOffre().getRecruteur() != null &&
                    demande.getOffre().getRecruteur().getCompany() != null) {
                uniqueCompanies.add(demande.getOffre().getRecruteur().getCompany());
            }
        }
        model.addAttribute("uniqueCompanies", uniqueCompanies);

        // Set the active tab
        model.addAttribute("activeTab", "applications");

        return "adminboard";
    }

    @GetMapping("/applications/{id}/update-status")
    public String updateApplicationStatus(@PathVariable Long id, @RequestParam String status,
                                          RedirectAttributes redirectAttributes) {
        try {
            demandeService.updateDemandeStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Statut de la candidature mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/admin/applications";
    }
    @Autowired
    private RecruteurService recruteurService;  // S'assurer que c'est déjà injecté dans votre contrôleur

    /**
     * Endpoint pour récupérer la liste des recruteurs
     * @return Liste des recruteurs au format JSON
     */
    @GetMapping("/recruteurs/list")
    @ResponseBody
    public List<Map<String, Object>> getRecruteursList() {
        List<Recruteur> recruteurs = recruteurService.getAllRecruteurs();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Recruteur recruteur : recruteurs) {
            Map<String, Object> recruteurMap = new HashMap<>();
            recruteurMap.put("idRecruteur", recruteur.getIdRecruteur());

            String nomEntreprise = "Sans entreprise";
            if (recruteur.getCompany() != null && recruteur.getCompany().getNomEntreprise() != null) {
                nomEntreprise = recruteur.getCompany().getNomEntreprise();
            }

            recruteurMap.put("nomEntreprise", nomEntreprise);
            result.add(recruteurMap);
        }

        return result;
    }
    // À ajouter dans AdminController.java

    // Endpoint pour récupérer les données d'une offre
    @GetMapping("/jobs/{id}/data")
    @ResponseBody
    public Offre getJobData(@PathVariable Long id) {
        return offreService.getOffreById(id);
    }

    // Endpoint pour mettre à jour une offre
    @PutMapping("/jobs/{id}")
    @ResponseBody
    public Map<String, Object> updateJob(@PathVariable Long id, @RequestBody Offre updatedOffre) {
        Map<String, Object> response = new HashMap<>();
        try {
            Offre existingOffre = offreService.getOffreById(id);
            if (existingOffre == null) {
                response.put("success", false);
                response.put("error", "Offre non trouvée");
                return response;
            }

            // Mise à jour des champs simples
            existingOffre.setTitle(updatedOffre.getTitle());
            existingOffre.setDescription(updatedOffre.getDescription());
            existingOffre.setIndustry(updatedOffre.getIndustry());
            existingOffre.setLocation(updatedOffre.getLocation());
            existingOffre.setContractType(updatedOffre.getContractType());
            existingOffre.setWorkMode(updatedOffre.getWorkMode());
            existingOffre.setExperienceLevel(updatedOffre.getExperienceLevel());
            existingOffre.setStatus(updatedOffre.getStatus());
            existingOffre.setMinSalary(updatedOffre.getMinSalary());
            existingOffre.setMaxSalary(updatedOffre.getMaxSalary());

            // Mise à jour des responsabilités et qualifications
            if (updatedOffre.getResponsibilities() != null) {
                existingOffre.setResponsibilities(updatedOffre.getResponsibilities());
            }

            if (updatedOffre.getQualifications() != null) {
                existingOffre.setQualifications(updatedOffre.getQualifications());
            }

            // Mise à jour du recruteur si spécifié
            if (updatedOffre.getRecruteur() != null && updatedOffre.getRecruteur().getIdRecruteur() != null) {
                Recruteur recruteur = recruteurService.getRecruteurById(updatedOffre.getRecruteur().getIdRecruteur());
                if (recruteur != null) {
                    existingOffre.setRecruteur(recruteur);
                }
            }

            // Enregistrer les modifications
            offreService.saveOffre(existingOffre);

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    // Endpoint pour supprimer une offre
    @DeleteMapping("/jobs/{id}")
    @ResponseBody
    public Map<String, Object> deleteJob(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            offreService.deleteOffre(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Endpoint pour supprimer plusieurs offres
    @PostMapping("/jobs/batch-delete")
    @ResponseBody
    public Map<String, Object> batchDeleteJobs(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> ids = request.get("ids");
            if (ids != null && !ids.isEmpty()) {
                for (Long id : ids) {
                    offreService.deleteOffre(id);
                }
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("error", "Aucun ID fourni");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Endpoint pour changer le statut d'une offre
    @PostMapping("/jobs/{id}/status")
    @ResponseBody
    public Map<String, Object> changeJobStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                response.put("success", false);
                response.put("error", "Statut non fourni");
                return response;
            }

            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                response.put("success", false);
                response.put("error", "Offre non trouvée");
                return response;
            }

            offre.setStatus(newStatus);

            // Pour les offres activées, mettre à jour les dates
            if ("ACTIVE".equals(newStatus)) {
                offre.setPostedAt(LocalDateTime.now());
                offre.setExpiresAt(LocalDateTime.now().plusDays(30));
            }

            offreService.saveOffre(offre);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Endpoint pour exporter les offres en CSV
    @GetMapping("/jobs/export")
    public void exportJobs(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"offres.csv\"");

        List<Offre> offres = offreService.getAllOffres();

        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                .withHeader("ID", "Titre", "Entreprise", "Industrie", "Lieu", "Type", "Statut", "Publiée le"))) {

            for (Offre offre : offres) {
                csvPrinter.printRecord(
                        offre.getId(),
                        offre.getTitle(),
                        offre.getCompanyName(),
                        offre.getIndustry(),
                        offre.getLocation(),
                        offre.getContractType(),
                        offre.getStatus(),
                        offre.getPostedAt() != null ? offre.getPostedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"
                );
            }
        }
    }




}