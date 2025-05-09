package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.repository.OffreRepository;
import com.ismagi.Fursati.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    private final OffreRepository offreRepository;

    @Autowired
    public HomeController(OffreRepository offreRepository) {
        this.offreRepository = offreRepository;
    }

    @GetMapping("/")
    public String homePage(HttpSession session, Model model) {
        // Vérifier si l'utilisateur est déjà authentifié
        Boolean isAuthenticated = (Boolean) session.getAttribute("authenticated");

        if (isAuthenticated != null && isAuthenticated) {
            // Récupérer le type d'utilisateur depuis la session
            AuthService.UserType userType = (AuthService.UserType) session.getAttribute("userType");

            // Rediriger vers le dashboard correspondant
            if (userType != null) {
                switch (userType) {
                    case ADMIN:
                        return "redirect:/admin";
                    case CANDIDAT:
                        return "redirect:/candidats"; // Path corrigé (était /candidat)
                    case RECRUTEUR:
                        return "redirect:/recruteurs";
                }
            }
        }

        // Si non authentifié ou type inconnu, afficher la page d'accueil
        model.addAttribute("activeTab", "home");
        return "home";
    }

    @GetMapping("/jobs")
    public String jobs(Model model) {
        List<Offre> offres = offreRepository.findAll();
        model.addAttribute("offres", offres);
        model.addAttribute("activeTab", "jobs");
        return "home";
    }

    @GetMapping("/companies")
    public String companies(Model model) {
        model.addAttribute("activeTab", "companies");
        return "home";
    }

    @GetMapping("/candidates")
    public String candidates(Model model) {
        model.addAttribute("activeTab", "candidates");
        return "home";
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        model.addAttribute("activeTab", "blog");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("activeTab", "about");
        return "home";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("activeTab", "signup");
        return "home";
    }
}