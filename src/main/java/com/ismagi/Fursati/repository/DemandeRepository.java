package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    @Query("SELECT d from Demande d where d.offre.recruteur.idRecruteur=:id")
   List<Demande> getDemandeByOffreRecruteur_IdRecruteur(Long id);
}
