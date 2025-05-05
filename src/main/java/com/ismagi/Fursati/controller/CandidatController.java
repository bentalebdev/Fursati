package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.dto.*;
import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.CandidatProfileService;
import com.ismagi.Fursati.service.CandidatService;
import com.ismagi.Fursati.service.DemandeService;
import com.ismagi.Fursati.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    // Add these methods to your existing CandidatController class

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