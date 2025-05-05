package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.service.CandidatService;
import com.ismagi.Fursati.service.DemandeService;
import com.ismagi.Fursati.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // View a specific application
    @GetMapping("/{id}")
    public String viewDemande(@PathVariable Long id, Model model) {
        logger.info("Viewing application with ID: " + id);

        Demande demande = demandeService.getDemandeById(id);
        if (demande == null) {
            model.addAttribute("errorMessage", "Candidature non trouvée");
            return "error";
        }

        model.addAttribute("demande", demande);
        return "demande-details";
    }

    // Create a new application form
    @GetMapping("/new")
    public String newDemandeForm(Model model) {
        model.addAttribute("demande", new Demande());
        model.addAttribute("candidats", candidatService.getAllCandidats());
        model.addAttribute("offres", offreService.getAllOffres());
        return "demande-form";
    }

    // Submit new application
    @PostMapping
    public String createDemande(@ModelAttribute Demande demande) {
        demande.setDateDemande(new Date());
        demande.setEtat("PENDING"); // Set initial status
        demandeService.saveDemande(demande);
        return "redirect:/recruteurs/applications";
    }

    // API endpoint to update application status
    @PutMapping("/{id}/status")
    @ResponseBody
    public Map<String, Object> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Demande demande = demandeService.getDemandeById(id);
            if (demande == null) {
                response.put("success", false);
                response.put("error", "Candidature non trouvée");
                return response;
            }

            // Update status
            demande.setEtat(request.getStatus());
            demandeService.saveDemande(demande);

            // Send email if requested
            if (request.isSendEmail() && demande.getCandidat() != null && demande.getCandidat().getEmail() != null) {
                // Email sending logic would go here
                logger.info("Sending email to: " + demande.getCandidat().getEmail() + " with status: " + request.getStatus());
            }

            response.put("success", true);
            return response;
        } catch (Exception e) {
            logger.severe("Error updating status: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    // API endpoint for bulk status update
    @PutMapping("/bulk-status")
    @ResponseBody
    public Map<String, Object> bulkUpdateStatus(@RequestBody BulkStatusUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            int updateCount = 0;

            for (Long id : request.getIds()) {
                Demande demande = demandeService.getDemandeById(id);
                if (demande != null) {
                    demande.setEtat(request.getStatus());
                    demandeService.saveDemande(demande);
                    updateCount++;

                    // Send email if requested
                    if (request.isSendEmail() && demande.getCandidat() != null && demande.getCandidat().getEmail() != null) {
                        // Email sending logic would go here
                        logger.info("Sending email to: " + demande.getCandidat().getEmail() + " with status: " + request.getStatus());
                    }
                }
            }

            response.put("success", true);
            response.put("updatedCount", updateCount);
            return response;
        } catch (Exception e) {
            logger.severe("Error updating bulk status: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    // API endpoint to re-examine a rejected application
    @PutMapping("/{id}/reexamine")
    @ResponseBody
    public Map<String, Object> reexamineDemande(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Demande demande = demandeService.getDemandeById(id);
            if (demande == null) {
                response.put("success", false);
                response.put("error", "Candidature non trouvée");
                return response;
            }

            // Change status from REJECTED to REVIEWED
            if ("REJECTED".equals(demande.getEtat())) {
                demande.setEtat("REVIEWED");
                demandeService.saveDemande(demande);

                // Notify candidate if needed
                if (demande.getCandidat() != null && demande.getCandidat().getEmail() != null) {
                    // Email notification logic would go here
                    logger.info("Notifying: " + demande.getCandidat().getEmail() + " about re-examination");
                }

                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("error", "Seules les candidatures refusées peuvent être réexaminées");
            }

            return response;
        } catch (Exception e) {
            logger.severe("Error re-examining application: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    // API endpoint to schedule/update an interview
    @PostMapping("/{id}/interview")
    @ResponseBody
    public Map<String, Object> scheduleInterview(
            @PathVariable Long id,
            @RequestBody InterviewRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Demande demande = demandeService.getDemandeById(id);
            if (demande == null) {
                response.put("success", false);
                response.put("error", "Candidature non trouvée");
                return response;
            }

            // Update status to INTERVIEW
            demande.setEtat("INTERVIEW");
            demandeService.saveDemande(demande);

            // In a real application, you would save interview details to an Interview entity
            // Here we're just logging it for demonstration
            logger.info("Interview scheduled for application ID: " + id +
                    ", Date: " + request.getDate() +
                    ", Time: " + request.getTime() +
                    ", Type: " + request.getType());

            // Notify candidate if needed
            if (demande.getCandidat() != null && demande.getCandidat().getEmail() != null) {
                // Email notification logic would go here
                logger.info("Notifying: " + demande.getCandidat().getEmail() + " about interview");
            }

            response.put("success", true);
            return response;
        } catch (Exception e) {
            logger.severe("Error scheduling interview: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    // API endpoint to update an interview
    @PutMapping("/{id}/interview")
    @ResponseBody
    public Map<String, Object> updateInterview(
            @PathVariable Long id,
            @RequestBody InterviewRequest request) {

        // Implementation would be similar to the scheduleInterview method
        // For simplicity, we'll just call that method
        return scheduleInterview(id, request);
    }

    // API endpoint to get interview details
    @GetMapping("/{id}/interview")
    @ResponseBody
    public Map<String, Object> getInterviewDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Demande demande = demandeService.getDemandeById(id);
            if (demande == null || !"INTERVIEW".equals(demande.getEtat())) {
                response.put("success", false);
                response.put("error", "Entretien non trouvé");
                return response;
            }

            // In a real application, you would fetch interview details from an Interview entity
            // For now, we're returning mock data
            response.put("success", true);
            response.put("date", "2025-05-15");
            response.put("time", "14:30");
            response.put("type", "VIDEO");
            response.put("location", "https://meet.example.com/interview");
            response.put("notes", "Préparez des questions sur l'expérience du candidat");

            return response;
        } catch (Exception e) {
            logger.severe("Error getting interview details: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    // Helper classes for request objects
    public static class StatusUpdateRequest {
        private String status;
        private String reason;
        private String comment;
        private boolean sendEmail = true;

        // Getters and setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public boolean isSendEmail() {
            return sendEmail;
        }

        public void setSendEmail(boolean sendEmail) {
            this.sendEmail = sendEmail;
        }
    }

    public static class BulkStatusUpdateRequest {
        private List<Long> ids;
        private String status;
        private String reason;
        private String comment;
        private boolean sendEmail = true;

        // Getters and setters
        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public boolean isSendEmail() {
            return sendEmail;
        }

        public void setSendEmail(boolean sendEmail) {
            this.sendEmail = sendEmail;
        }
    }

    public static class InterviewRequest {
        private String date;
        private String time;
        private String type;
        private String location;
        private String notes;

        // Getters and setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}