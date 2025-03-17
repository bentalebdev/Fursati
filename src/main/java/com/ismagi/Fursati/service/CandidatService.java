package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.repository.CandidatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidatService {
    @Autowired
    private CandidatRepository candidatRepository;

    public List<Candidat> getAllCandidats() {
        return candidatRepository.findAll();
    }

    public Candidat getCandidatById(Long id) {
        return candidatRepository.findById(id).orElse(null);
    }

    public Candidat saveCandidat(Candidat candidat) {
        return candidatRepository.save(candidat);
    }

    public void deleteCandidat(Long id) {
        candidatRepository.deleteById(id);
    }
}
