package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.repository.OffreRepository;
import com.ismagi.Fursati.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final OffreRepository offreRepository;

    @Autowired
    public HomeController(OffreRepository offreRepository) {
        this.offreRepository = offreRepository;
    }

    @GetMapping("/")
    public String homePage(HttpSession session, Model model) {
        // Check if user is already authenticated
        Boolean isAuthenticated = (Boolean) session.getAttribute("authenticated");

        if (isAuthenticated != null && isAuthenticated) {
            // Get user type from session
            AuthService.UserType userType = (AuthService.UserType) session.getAttribute("userType");

            // Log user information
            Object userId = session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");
            logger.info("Authenticated user: {} (ID: {}, Type: {}) accessing home page",
                    userName, userId, userType);

            // Redirect to the corresponding dashboard
            if (userType != null) {
                switch (userType) {
                    case ADMIN:
                        return "redirect:/admin/dashboard";
                    case CANDIDAT:
                        return "redirect:/candidats/dashboard";
                    case RECRUTEUR:
                        return "redirect:/recruteurs/dashboard";
                }
            }
        }

        // If not authenticated or unknown type, display home page
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