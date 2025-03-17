package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Diplome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiplomeRepository extends JpaRepository<Diplome, Long> {
}
