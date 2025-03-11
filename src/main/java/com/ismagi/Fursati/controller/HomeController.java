package com.ismagi.Fursati.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@RequestParam(name = "tab", required = false, defaultValue = "home") String activeTab,
                       Model model) {
        // Ajouter l'onglet actif au modèle
        model.addAttribute("activeTab", activeTab);

        return "home";
    }


    @GetMapping("/jobs")
    public String jobs(Model model) {
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
    public String signup(Model model){
        model.addAttribute("activeTab","signup");
        return "home";
    }
}