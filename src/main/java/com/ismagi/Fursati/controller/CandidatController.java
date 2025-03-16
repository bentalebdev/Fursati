package com.ismagi.Fursati.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/candidate")
public class CandidatController {

    @GetMapping("/dashboard")
    public String candidat(Model model) {
        // Indiquer que l'onglet "candidates" est actif dans la navbar
        model.addAttribute("activeTab", "candidates");

        // Rediriger vers la page candidateboard
        return "candidateboard";
    }
}