package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Recruteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruteurRepository extends JpaRepository<Recruteur, Long> {
    public  <Optinal> Recruteur findRecruteurByEmail(String email);
}
