package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Admin;
import com.ismagi.Fursati.entity.Company;
import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


}