package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.dto.*;
import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.CandidatProfileService;
import com.ismagi.Fursati.service.CandidatService;
import com.ismagi.Fursati.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // API endpoints
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

    // Pages web
    @GetMapping({"", "/"})
    public String redirectToDashboard() {
        return "redirect:/candidats/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        logger.info("Loading dashboard page...");
        model.addAttribute("activeTab", "dashboard");

        // Get current candidate (hardcoded to ID 1 for now)
        Long candidatId = 1L;
        Candidat candidat = candidatService.getCandidatById(candidatId);

        if (candidat != null) {
            // We'll need to add some methods to the services to get this data
            // For now, we'll create mock data that would be provided by these methods

            // Count applications (we would need a DemandeService for this)
            int applicationCount = 12; // This would be demandeService.countByCandidatId(candidatId)

            // Count interviews (Demandes with status "INTERVIEW")
            int interviewCount = 3; // This would be demandeService.countByCandidatIdAndEtat(candidatId, "INTERVIEW")

            // Count saved offers (we need a method or entity for saved offers)
            int savedOffersCount = 8; // This would come from a savedOffersService

            // Profile views (we need a method or entity for profile views)
            int profileViewsCount = 42; // This would come from a profileViewsService

            // Recent activities (last 3 applications, interviews, saved jobs)
            List<Map<String, Object>> recentActivities = new ArrayList<>();
            // This would come from a method like demandeService.getRecentActivitiesByCandidatId(candidatId, 3)

            // Mock data for recent activities
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("type", "application");
            activity1.put("title", "Développeur Full Stack");
            activity1.put("company", "TechMagic SARL");
            activity1.put("daysAgo", 2);
            recentActivities.add(activity1);

            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("type", "interview");
            activity2.put("title", "Responsable Marketing");
            activity2.put("company", "MarketPro");
            activity2.put("daysAgo", 3);
            recentActivities.add(activity2);

            Map<String, Object> activity3 = new HashMap<>();
            activity3.put("type", "saved");
            activity3.put("title", "Chef de projet Digital");
            activity3.put("company", "DigiTech");
            activity3.put("daysAgo", 5);
            recentActivities.add(activity3);

            // Upcoming interviews
            List<Map<String, Object>> upcomingInterviews = new ArrayList<>();
            // This would come from a method like demandeService.getUpcomingInterviewsByCandidatId(candidatId)

            // Mock data for upcoming interviews
            Map<String, Object> interview1 = new HashMap<>();
            interview1.put("company", "MarketPro");
            interview1.put("title", "Responsable Marketing");
            interview1.put("date", "22 Mars");
            upcomingInterviews.add(interview1);

            Map<String, Object> interview2 = new HashMap<>();
            interview2.put("company", "DigiTech");
            interview2.put("title", "Développeur Front-end");
            interview2.put("date", "25 Mars");
            upcomingInterviews.add(interview2);

            Map<String, Object> interview3 = new HashMap<>();
            interview3.put("company", "Tech Solutions");
            interview3.put("title", "Chef de Projet");
            interview3.put("date", "28 Mars");
            upcomingInterviews.add(interview3);

            // Add all data to the model
            model.addAttribute("applicationCount", applicationCount);
            model.addAttribute("interviewCount", interviewCount);
            model.addAttribute("savedOffersCount", savedOffersCount);
            model.addAttribute("profileViewsCount", profileViewsCount);
            model.addAttribute("recentActivities", recentActivities);
            model.addAttribute("upcomingInterviews", upcomingInterviews);
        }

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

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

            model.addAttribute("offre", offre);
            model.addAttribute("similarOffers", similarOffers);
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

    @GetMapping("/applications")
    public String applications(Model model) {
        logger.info("Loading applications page...");

        model.addAttribute("activeTab", "applications");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

    // === PROFILE ===

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
    public String updateBasicInfo(@ModelAttribute BasicInfoDTO basicInfoDTO) {
        candidatProfileService.updateBasicInfo(1L, basicInfoDTO);
        return "redirect:/candidats/profile";
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

    @PostMapping("/profile/skills-languages")
    public String updateSkillsAndLanguages(@ModelAttribute SkillsLanguagesDTO dto) {
        candidatProfileService.updateSkillsAndLanguages(1L, dto);
        return "redirect:/candidats/profile";
    }

    @GetMapping("/documents")
    public String documents(Model model) {
        logger.info("Loading documents page...");

        model.addAttribute("activeTab", "documents");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
    }

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
}