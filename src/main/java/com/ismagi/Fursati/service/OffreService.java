package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.repository.OffreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OffreService {
    @Autowired
    private OffreRepository offreRepository;

    public List<Offre> getAllOffres() {
        return offreRepository.findAll();
    }

    public Offre getOffreById(Long id) {
        return offreRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Offre non trouvée avec l'id: " + id));
    }
    public Offre saveOffre(Offre offre) {
        return offreRepository.save(offre);
    }

    public void deleteOffre(Long id) {
        offreRepository.deleteById(id);
    }
}
