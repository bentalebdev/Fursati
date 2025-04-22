package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Offre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    // Keep existing method that works
    List<Offre> findOffresByRecruteurIdRecruteur(Long idRecruteur);

    // Method with explicit JPQL query to avoid property path issues
    @Query("SELECT o FROM Offre o WHERE o.recruteur.idRecruteur = :recruteurId")
    Page<Offre> findByRecruteurId(@Param("recruteurId") Long recruteurId, Pageable pageable);

    // Method with explicit JPQL query for counting
    @Query("SELECT COUNT(o) FROM Offre o WHERE o.recruteur.idRecruteur = :recruteurId AND o.status = :status")
    long countByRecruteurIdAndStatus(@Param("recruteurId") Long recruteurId, @Param("status") String status);

    // Method with explicit JPQL query for status filtering
    @Query("SELECT o FROM Offre o WHERE o.status = :status")
    Page<Offre> findByStatus(@Param("status") String status, Pageable pageable);

    // Method with explicit JPQL query for recruiter ID and status
    @Query("SELECT o FROM Offre o WHERE o.recruteur.idRecruteur = :recruteurId AND o.status = :status")
    Page<Offre> findByRecruteurIdAndStatus(@Param("recruteurId") Long recruteurId, @Param("status") String status, Pageable pageable);

    // Method with explicit JPQL query for title and recruiter ID
    @Query("SELECT o FROM Offre o WHERE LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND o.recruteur.idRecruteur = :recruteurId")
    Page<Offre> findByTitleContainingIgnoreCaseAndRecruteurId(@Param("keyword") String keyword, @Param("recruteurId") Long recruteurId, Pageable pageable);
}