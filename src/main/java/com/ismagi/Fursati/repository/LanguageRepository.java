package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByCandidatId(Long candidatId);

}
