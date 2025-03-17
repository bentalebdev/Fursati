package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.service.CandidatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/candidats")
public class CandidatController {
    @Autowired
    private CandidatService candidatService;

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
        model.addAttribute("activeTab", "jobs");
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