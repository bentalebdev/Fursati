package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query("SELECT c FROM Company c JOIN c.recruteurs r WHERE r.idRecruteur = :recruitId")
    Company getCompanyByRecruitId(@Param("recruitId") Long recruitId);

}