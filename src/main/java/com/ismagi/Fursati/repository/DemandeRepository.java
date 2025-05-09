package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByCandidatId(Long candidatId);

    // Find applications by offer ID
    List<Demande> findByOffreId(Long offreId);

    // Find applications by candidate ID and offer ID
    Demande findByCandidatIdAndOffreId(Long candidatId, Long offreId);

    // Find applications by candidate ID and status
    List<Demande> findByCandidatIdAndEtat(Long candidatId, String etat);

    // Find applications by candidate ID ordered by date descending (newest first)
    List<Demande> findByCandidatIdOrderByDateDemandeDesc(Long candidatId);

    // Get applications by recruiter ID using a custom query
    @Query("SELECT d FROM Demande d WHERE d.offre.recruteur.idRecruteur = :recruitId")
    List<Demande> getDemandeByOffre_Recruteur_IdRecruteur(@Param("recruitId") Long recruitId);
}