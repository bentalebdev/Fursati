package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.repository.OffreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class OffreService {
    private static final Logger logger = Logger.getLogger(OffreService.class.getName());

    @Autowired
    private OffreRepository offreRepository;

    public List<Offre> getAllOffres() {
        try {
            return offreRepository.findAll();
        } catch (Exception e) {
            logger.severe("Error retrieving all offers: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Offre getOffreById(Long id) {
        try {
            return offreRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.severe("Error retrieving offer with id " + id + ": " + e.getMessage());
            return null;
        }
    }

    public Offre saveOffre(Offre offre) {
        try {
            return offreRepository.save(offre);
        } catch (Exception e) {
            logger.severe("Error saving offer: " + e.getMessage());
            throw e; // Relancer l'exception après journalisation
        }
    }

    public void deleteOffre(Long id) {
        try {
            offreRepository.deleteById(id);
        } catch (Exception e) {
            logger.severe("Error deleting offer with id " + id + ": " + e.getMessage());
            throw e; // Relancer l'exception après journalisation
        }
    }

    /**
     * Finds similar job offers based on the industry, contract type, or location
     * @param offre The source offer to find similar offers for
     * @return A list of similar offers, excluding the source offer itself
     */
    public List<Offre> getSimilarOffres(Offre offre) {
        if (offre == null || offre.getId() == null) {
            return new ArrayList<>();
        }

        try {
            // Get all offers
            List<Offre> allOffers = offreRepository.findAll();

            // Filter offers to find similar ones based on industry, contract type, or location
            return allOffers.stream()
                    .filter(o -> o.getId() != null && !o.getId().equals(offre.getId())) // Exclude the current offer
                    .filter(o -> isSimilar(offre, o))
                    .limit(4) // Limit to 4 similar offers
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warning("Error finding similar offers: " + e.getMessage());
            return new ArrayList<>(); // Return empty list in case of errors
        }
    }

    /**
     * Determines if two offers are similar based on their attributes
     * @param source The source offer
     * @param target The potential similar offer
     * @return true if the offers are similar, false otherwise
     */
    private boolean isSimilar(Offre source, Offre target) {
        // Sécuriser contre les valeurs nulles
        if (source == null || target == null) return false;

        try {
            // Consider offers similar if they share industry, contract type, or location
            boolean sameIndustry = source.getIndustry() != null &&
                    target.getIndustry() != null &&
                    source.getIndustry().equals(target.getIndustry());

            boolean sameContractType = source.getContractType() != null &&
                    target.getContractType() != null &&
                    source.getContractType().equals(target.getContractType());

            boolean sameLocation = source.getLocation() != null &&
                    target.getLocation() != null &&
                    source.getLocation().equals(target.getLocation());

            boolean sameExperienceLevel = source.getExperienceLevel() != null &&
                    target.getExperienceLevel() != null &&
                    source.getExperienceLevel().equals(target.getExperienceLevel());

            // Consider similar if at least one attribute matches
            int matchCount = 0;
            if (sameIndustry) matchCount++;
            if (sameContractType) matchCount++;
            if (sameLocation) matchCount++;
            if (sameExperienceLevel) matchCount++;

            return matchCount >= 1; // Retourner true s'il y a au moins une correspondance
        } catch (Exception e) {
            logger.warning("Error comparing offers for similarity: " + e.getMessage());
            return false;
        }
    }
}