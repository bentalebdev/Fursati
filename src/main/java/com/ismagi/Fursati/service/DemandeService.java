package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.repository.DemandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DemandeService {
    @Autowired
    private DemandeRepository demandeRepository;

    public List<Demande> getAllDemandes() {
        return demandeRepository.findAll();
    }

    public List<Demande> getDemandesByRecruitId(Long recruitId) {
        return demandeRepository.getDemandeByOffre_Recruteur_IdRecruteur(recruitId);
    }

    public Demande getDemandeById(Long id) {
        return demandeRepository.findById(id).orElse(null);
    }

    public Demande saveDemande(Demande demande) {
        return demandeRepository.save(demande);
    }

    /**
     * Updates an existing demande (job application)
     * @param demande the demande object with updated values
     * @return the updated demande
     * @throws RuntimeException if the demande does not exist
     */
    public Demande updateDemande(Demande demande) {
        if (demande.getIdDemande() == null) {
            throw new RuntimeException("Cannot update a demande without an ID");
        }

        Optional<Demande> existingDemande = demandeRepository.findById(demande.getIdDemande());
        if (!existingDemande.isPresent()) {
            throw new RuntimeException("Cannot update demande with ID " + demande.getIdDemande() + " as it does not exist");
        }

        return demandeRepository.save(demande);
    }

    /**
     * Updates the status of a job application
     * @param id the ID of the demande to update
     * @param newStatus the new status to set
     * @return the updated demande
     * @throws RuntimeException if the demande does not exist
     */
    public Demande updateDemandeStatus(Long id, String newStatus) {
        Demande demande = getDemandeById(id);
        if (demande == null) {
            throw new RuntimeException("Demande with ID " + id + " not found");
        }

        demande.setEtat(newStatus);
        return demandeRepository.save(demande);
    }

    public void deleteDemande(Long id) {
        demandeRepository.deleteById(id);
    }

    /**
     * Find applications by candidate ID and sort by date
     * @param candidatId ID of the candidate
     * @return List of applications, sorted by date (newest first)
     */
    public List<Demande> getDemandesByCandidatIdSortedByDate(Long candidatId) {
        return demandeRepository.findByCandidatIdOrderByDateDemandeDesc(candidatId);
    }
}