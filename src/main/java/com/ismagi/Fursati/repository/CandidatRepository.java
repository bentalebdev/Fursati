package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Candidat  getCandidatById(Long id);
    public <Optional> Candidat findCandidatByEmail(String email);

}