package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query("SELECT c FROM Company c JOIN c.recruteurs r WHERE r.idRecruteur = :recruitId")
    Company getCompanyByRecruitId(@Param("recruitId") Long recruitId);

    // Find companies by verification status
    List<Company> findByIsVerified(Boolean isVerified);

    // Find companies by sector
    List<Company> findBySecteur(String sector);

    // Find companies by city
    List<Company> findByVille(String city);

    // Find companies by company size
    List<Company> findByTailleEntreprise(String companySize);

    // Search in multiple fields
    @Query("SELECT c FROM Company c WHERE " +
            "LOWER(c.nomEntreprise) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.numeroRC) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.siteWeb) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.emailContact) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Company> searchCompanies(@Param("searchTerm") String searchTerm);

    // Get distinct sectors
    @Query("SELECT DISTINCT c.secteur FROM Company c WHERE c.secteur IS NOT NULL ORDER BY c.secteur")
    List<String> findDistinctSectors();

    // Get distinct cities
    @Query("SELECT DISTINCT c.ville FROM Company c WHERE c.ville IS NOT NULL ORDER BY c.ville")
    List<String> findDistinctCities();
}