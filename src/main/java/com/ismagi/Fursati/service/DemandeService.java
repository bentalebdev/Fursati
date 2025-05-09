package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.repository.DemandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemandeService {
    @Autowired
    private DemandeRepository demandeRepository;

    public List<Demande> getAllDemandes() {
        return demandeRepository.findAll();
    }

    public List<Demande> getDemandesByRecruitId(Long recruitId) {
        return  demandeRepository.getDemandeByOffre_Recruteur_IdRecruteur(recruitId);
    }

    public Demande getDemandeById(Long id) {
        return demandeRepository.findById(id).orElse(null);
    }

    public Demande saveDemande(Demande demande) {
        return demandeRepository.save(demande);
    }

    public void deleteDemande(Long id) {
        demandeRepository.deleteById(id);
    }
    public List<Demande> getDemandesByCandidatId(Long candidatId) {
        return demandeRepository.findByCandidatId(candidatId);
    }

    public List<Demande> getDemandesByCandidatIdSortedByDate(Long candidatId) {
        return demandeRepository.findByCandidatIdOrderByDateDemandeDesc(candidatId);
    }

    public List<Demande> getDemandesByOffreId(Long offreId) {
        return demandeRepository.findByOffreId(offreId);
    }

    public Demande getDemandeByOffreAndCandidat(Long offreId, Long candidatId) {
        return demandeRepository.findByCandidatIdAndOffreId(candidatId, offreId);
    }

    public List<Demande> getDemandesByCandidatAndEtat(Long candidatId, String etat) {
        return demandeRepository.findByCandidatIdAndEtat(candidatId, etat);
    }

    public boolean hasAlreadyApplied(Long candidatId, Long offreId) {
        return demandeRepository.findByCandidatIdAndOffreId(candidatId, offreId) != null;
    }
}
