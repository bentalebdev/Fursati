package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Document;
import com.ismagi.Fursati.repository.CandidatRepository;
import com.ismagi.Fursati.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class DocumentService {
    private static final Logger logger = Logger.getLogger(DocumentService.class.getName());

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    public List<Document> getAllDocumentsByCandidatId(Long candidatId) {
        return documentRepository.findByCandidatId(candidatId);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public Document saveDocument(Long candidatId, MultipartFile file, String documentType,
                                 String title, boolean isDefault) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // Get candidate
        Optional<Candidat> candidatOpt = candidatRepository.findById(candidatId);
        if (!candidatOpt.isPresent()) {
            throw new IllegalArgumentException("Candidat not found");
        }
        Candidat candidat = candidatOpt.get();

        // Create upload directory if it doesn't exist
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // Create candidate directory
        String candidatDir = uploadDir + File.separator + candidatId;
        File candidatDirFile = new File(candidatDir);
        if (!candidatDirFile.exists()) {
            candidatDirFile.mkdirs();
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file to disk
        Path filePath = Paths.get(candidatDir + File.separator + newFilename);
        Files.write(filePath, file.getBytes());

        // If this is a default document of the specified type, unset any existing defaults
        if (isDefault) {
            List<Document> existingDefaults = documentRepository.findByCandidatIdAndDocumentTypeAndIsDefault(
                    candidatId, documentType, true);

            for (Document doc : existingDefaults) {
                doc.setDefault(false);
                documentRepository.save(doc);
            }
        }

        // Save document metadata to database
        Document document = new Document();
        document.setCandidat(candidat);
        document.setFileName(originalFilename);
        document.setStoredFileName(newFilename);
        document.setFilePath(filePath.toString());
        document.setDocumentType(documentType);
        document.setTitle(title);
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setUploadDate(LocalDateTime.now());
        document.setDefault(isDefault);

        return documentRepository.save(document);
    }

    public void deleteDocument(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document != null) {
            // Delete file from disk
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete from database
            documentRepository.delete(document);
        }
    }

    public byte[] getDocumentBytes(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document != null) {
            Path filePath = Paths.get(document.getFilePath());
            return Files.readAllBytes(filePath);
        }
        return null;
    }

    public Document setAsDefault(Long documentId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document != null) {
            // Unset any existing defaults of the same type
            List<Document> existingDefaults = documentRepository.findByCandidatIdAndDocumentTypeAndIsDefault(
                    document.getCandidat().getId(), document.getDocumentType(), true);

            for (Document doc : existingDefaults) {
                doc.setDefault(false);
                documentRepository.save(doc);
            }

            // Set this document as default
            document.setDefault(true);
            return documentRepository.save(document);
        }
        return null;
    }

    public Document renameDocument(Long documentId, String newTitle) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document != null) {
            document.setTitle(newTitle);
            return documentRepository.save(document);
        }
        return null;
    }
}