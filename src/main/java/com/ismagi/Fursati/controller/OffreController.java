package com.ismagi.Fursati.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.entity.Recruteur;
import com.ismagi.Fursati.service.OffreService;
import com.ismagi.Fursati.service.RecruteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/offres")
public class OffreController {

    @Autowired
    private OffreService offreService;
    @Autowired
    private RecruteurService recruteurService;

    @PostMapping("/create")
    public String createOffre(@ModelAttribute Offre offre,
                              @RequestParam(required = false) String responsibilitiesStr,
                              @RequestParam(required = false) String qualificationsStr,
                              RedirectAttributes redirectAttributes) {

        System.out.println("DEBUG: Starting createOffre method");
        System.out.println("DEBUG: Received job title: " + offre.getTitle());
        System.out.println("DEBUG: Responsibilities string: " + responsibilitiesStr);
        System.out.println("DEBUG: Qualifications string: " + qualificationsStr);

        try {
            // Get recruiter
            System.out.println("DEBUG: Getting recruiter with ID 1");
            Recruteur recruteur = recruteurService.getRecruteurById(offre.getRecruteur().getIdRecruteur());
            if (recruteur == null) {
                System.out.println("DEBUG: CRITICAL ERROR - Recruiter with ID 1 not found!");
                throw new RuntimeException("Recruiter with ID 1 not found");
            }
            System.out.println("DEBUG: Retrieved recruiter: " + recruteur.getIdRecruteur());
            offre.setRecruteur(recruteur);

            // Process responsibilities - REPLACING Arrays.asList with ArrayList
            if (responsibilitiesStr != null && !responsibilitiesStr.isEmpty()) {
                System.out.println("DEBUG: Processing responsibilities");
                // Create a modifiable ArrayList instead of fixed-size List
                List<String> responsibilities = new java.util.ArrayList<>();
                String[] respArray = responsibilitiesStr.split(",");
                System.out.println("DEBUG: Split responsibilities into " + respArray.length + " items");
                for (String resp : respArray) {
                    if (!resp.trim().isEmpty()) {
                        responsibilities.add(resp.trim());
                    }
                }
                System.out.println("DEBUG: Final responsibilities count: " + responsibilities.size());
                offre.setResponsibilities(responsibilities);
            }

            // Process qualifications - REPLACING Arrays.asList with ArrayList
            if (qualificationsStr != null && !qualificationsStr.isEmpty()) {
                System.out.println("DEBUG: Processing qualifications");
                // Create a modifiable ArrayList instead of fixed-size List
                List<String> qualifications = new java.util.ArrayList<>();
                String[] qualArray = qualificationsStr.split(",");
                System.out.println("DEBUG: Split qualifications into " + qualArray.length + " items");
                for (String qual : qualArray) {
                    if (!qual.trim().isEmpty()) {
                        qualifications.add(qual.trim());
                    }
                }
                System.out.println("DEBUG: Final qualifications count: " + qualifications.size());
                offre.setQualifications(qualifications);
            }

            // Set default dates
            System.out.println("DEBUG: Setting dates");
            if (offre.getPostedAt() == null) {
                offre.setPostedAt(LocalDateTime.now());
            }
            if (offre.getExpiresAt() == null) {
                offre.setExpiresAt(LocalDateTime.now().plusDays(30));
            }

            // Save the offer
            System.out.println("DEBUG: About to save offer to database");
            Offre savedOffre = offreService.saveOffre(offre);
            System.out.println("DEBUG: Successfully saved offer with ID: " + savedOffre.getId());

            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", "L'offre a été publiée avec succès.");
            return "redirect:/recruteurs/post-job"; // Redirect back to the post-job page

        } catch (Exception e) {
            System.err.println("DEBUG: EXCEPTION TYPE: " + e.getClass().getName());
            System.err.println("DEBUG: EXCEPTION MESSAGE: " + e.getMessage());
            System.err.println("DEBUG: EXCEPTION STACKTRACE:");
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Une erreur s'est produite lors de l'enregistrement de l'offre: " + e.getMessage());
            return "redirect:/recruteurs/post-job";
        }
    }

    @GetMapping("/recruiter/{id}")
    public String getRecruiterOffres(@PathVariable Long id, Model model) {
        List<Offre> offres = offreService.findOffresByRecruteurIdRecruteur(id);
        model.addAttribute("offres", offres);
        return "fragments/recruiter/my-jobs :: my-jobs";
    }

    /**
     * Endpoint pour mettre à jour le statut d'une offre
     */
    @PutMapping("/api/offres/{id}/status")
    @ResponseBody
    public String updateOffreStatus(@PathVariable Long id, @RequestBody Offre request) {
        try {
            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                return "{\"success\": false, \"error\": \"Offre non trouvée\"}";
            }

            offre.setStatus(request.getStatus());

            // Pour les offres activées, mettre à jour les dates
            if ("ACTIVE".equals(request.getStatus())) {
                offre.setPostedAt(java.time.LocalDateTime.now());
                offre.setExpiresAt(java.time.LocalDateTime.now().plusDays(30));
            }

            offreService.saveOffre(offre);
            return "{\"success\": true}";
        } catch (Exception e) {
            return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
        }
    }
    
}