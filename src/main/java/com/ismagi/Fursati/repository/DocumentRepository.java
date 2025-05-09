package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCandidatId(Long candidatId);

    List<Document> findByCandidatIdAndDocumentType(Long candidatId, String documentType);

    List<Document> findByCandidatIdAndDocumentTypeAndIsDefault(Long candidatId, String documentType, boolean isDefault);
}