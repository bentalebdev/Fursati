package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.CandidatService;
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

    @GetMapping("/profile")
    public String profile(Model model) {
        logger.info("Loading profile page...");

        model.addAttribute("activeTab", "profile");

        // Provide a default Offre object to avoid null pointer exceptions in the view
        if (!model.containsAttribute("offre")) {
            model.addAttribute("offre", new Offre());
        }

        return "candidateboard";
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