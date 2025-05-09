package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.CandidatService;
import com.ismagi.Fursati.service.DemandeService;
import com.ismagi.Fursati.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/demandes")
public class DemandeController {
    private static final Logger logger = Logger.getLogger(DemandeController.class.getName());

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private CandidatService candidatService;

    @Autowired
    private OffreService offreService;

    @PostMapping("/postuler")
    public String postuler(@RequestParam Long offreId, RedirectAttributes redirectAttributes) {
        logger.info("Traitement de la demande pour l'offre ID: " + offreId);

        try {
            // Récupérer l'offre
            Offre offre = offreService.getOffreById(offreId);
            if (offre == null) {
                logger.warning("Offre non trouvée avec l'ID: " + offreId);
                redirectAttributes.addFlashAttribute("errorMessage", "Offre non trouvée");
                return "redirect:/candidats/jobs";
            }

            // Pour l'instant, on utilise un ID de candidat fixe (à remplacer par l'authentification)
            Long candidatId = 1L; // À remplacer par le candidat connecté
            Candidat candidat = candidatService.getCandidatById(candidatId);

            if (candidat == null) {
                logger.warning("Candidat non trouvé avec l'ID: " + candidatId);
                redirectAttributes.addFlashAttribute("errorMessage", "Candidat non trouvé");
                return "redirect:/candidats/jobs";
            }

            // Vérifier si une demande existe déjà
            List<Demande> demandes = demandeService.getAllDemandes();
            boolean demandeExists = demandes.stream()
                    .anyMatch(d -> d.getCandidat().getId().equals(candidatId) &&
                            d.getOffre().getId().equals(offreId));

            if (demandeExists) {
                logger.info("Une demande existe déjà pour ce candidat et cette offre");
                redirectAttributes.addFlashAttribute("infoMessage", "Vous avez déjà postulé à cette offre");
                return "redirect:/candidats/jobs/details/" + offreId;
            }

            // Créer et sauvegarder la demande
            Demande demande = new Demande();
            demande.setCandidat(candidat);
            demande.setOffre(offre);
            demande.setDateDemande(new Date());
            demande.setEtat("PENDING"); // État initial standardisé

            demandeService.saveDemande(demande);

            logger.info("Demande créée avec succès pour l'offre ID: " + offreId);
            redirectAttributes.addFlashAttribute("successMessage", "Votre candidature a été envoyée avec succès");

            return "redirect:/candidats/jobs/details/" + offreId;

        } catch (Exception e) {
            logger.severe("Erreur lors du traitement de la demande: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur s'est produite: " + e.getMessage());
            return "redirect:/candidats/jobs";
        }
    }

    @GetMapping("/mes-candidatures")
    public String mesCandidatures(Model model) {
        try {
            // Get the authenticated candidate
            Long candidatId = getCurrentCandidatId();

            // Get all applications for this candidate, ordered by date (newest first)
            List<Demande> candidatures = demandeService.getDemandesByCandidatIdSortedByDate(candidatId);

            model.addAttribute("candidatures", candidatures);
            return "candidat/mes-candidatures";

        } catch (Exception e) {
            logger.severe("Erreur lors de la récupération des candidatures: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Une erreur s'est produite lors du chargement de vos candidatures");
            return "error";
        }
    }


    // Helper method to get the current authenticated candidate's ID
    // This is a simplified example - implement actual authentication in a real app
    private Long getCurrentCandidatId() {
        // In a real application, you would use Spring Security to get the authenticated user
        // Something like:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // return ((CandidatUserDetails) auth.getPrincipal()).getCandidatId();

        // For now, we'll return a dummy ID (replace this with actual authentication)
        return 1L;
    }
    @PutMapping("/{id}/status")
    @ResponseBody
    public Map<String, Object> updateDemandeStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String status = (String) request.get("status");
            if (status == null) {
                response.put("success", false);
                response.put("error", "Status is required");
                return response;
            }

            Demande demande = demandeService.getDemandeById(id);
            if (demande == null) {
                response.put("success", false);
                response.put("error", "Demand not found");
                return response;
            }

            // Update the status
            demande.setEtat(status);
            demandeService.saveDemande(demande);

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PutMapping("/bulk-status")
    @ResponseBody
    public Map<String, Object> bulkUpdateStatus(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String status = (String) request.get("status");
            List<Long> ids = (List<Long>) request.get("ids");

            if (status == null || ids == null || ids.isEmpty()) {
                response.put("success", false);
                response.put("error", "Status and ids are required");
                return response;
            }

            int updatedCount = 0;
            for (Long id : ids) {
                Demande demande = demandeService.getDemandeById(id);
                if (demande != null) {
                    demande.setEtat(status);
                    demandeService.saveDemande(demande);
                    updatedCount++;
                }
            }

            response.put("success", true);
            response.put("updatedCount", updatedCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}