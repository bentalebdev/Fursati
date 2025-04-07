package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.CandidatService;
import com.ismagi.Fursati.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Controller
@RequestMapping("/candidats")
public class CandidatController {
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
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activeTab", "dashboard");
        return "candidateboard";
    }

    @GetMapping("/jobs")
    public String jobs(Model model) {
        List<Offre> offres = offreService.getAllOffres();
        model.addAttribute("offres", offres);
        model.addAttribute("activeTab", "jobs");
        return "candidateboard";
    }
    // Modification de la méthode dans CandidatController.java
    @GetMapping("/jobs/details/{id}")
    public String jobDetails(@PathVariable Long id, Model model) {
        Offre offre = offreService.getOffreById(id);
        model.addAttribute("offre", offre);
        model.addAttribute("activeTab", "jobsdetails");
        return "candidateboard";
    }

    @GetMapping("/applications")
    public String applications(Model model) {
        model.addAttribute("activeTab", "applications");
        return "candidateboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("activeTab", "profile");
        return "candidateboard";
    }

    @GetMapping("/documents")
    public String documents(Model model) {
        model.addAttribute("activeTab", "documents");
        return "candidateboard";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activeTab", "settings");
        return "candidateboard";
    }
}