package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.entity.Recruteur;
import com.ismagi.Fursati.service.RecruteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/recruteurs")
public class RecruteurController {
    private static final Logger logger = Logger.getLogger(CandidatController.class.getName());

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



        return "recruterboard";
    }
    @GetMapping("/my-jobs")
    public String myjobs(Model model) {
        logger.info("Loading post-job page...");
        model.addAttribute("activeTab", "my-jobs");



        return "recruterboard";
    }
    @GetMapping("/candidates")
    public String candidates(Model model) {
        logger.info("Loading candidates page...");
        model.addAttribute("activeTab", "candidates");



        return "recruterboard";
    }
    @GetMapping("/applications")
    public String applications(Model model) {
        logger.info("Loading applications page...");
        model.addAttribute("activeTab", "applications");



        return "recruterboard";
    }
    @GetMapping("/company")
    public String company(Model model) {
        logger.info("Loading company page...");
        model.addAttribute("activeTab", "company");



        return "recruterboard";
    }

    @GetMapping
    public List<Recruteur> getAllRecruteurs() {
        return recruteurService.getAllRecruteurs();
    }

    @GetMapping("/{id}")
    public Recruteur getRecruteurById(@PathVariable Long id) {
        return recruteurService.getRecruteurById(id);
    }

    @PostMapping
    public Recruteur createRecruteur(@RequestBody Recruteur recruteur) {
        return recruteurService.saveRecruteur(recruteur);
    }

    @DeleteMapping("/{id}")
    public void deleteRecruteur(@PathVariable Long id) {
        recruteurService.deleteRecruteur(id);
    }
}
