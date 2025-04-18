package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Diplome;
import com.ismagi.Fursati.repository.DiplomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiplomeService {
    @Autowired
    private DiplomeRepository diplomeRepository;

    public List<Diplome> getAllDiplomes() {
        return diplomeRepository.findAll();
    }

    public Diplome getDiplomeById(Long id) {
        return diplomeRepository.findById(id).orElse(null);
    }

    public Diplome saveDiplome(Diplome diplome) {
        return diplomeRepository.save(diplome);
    }

    public void deleteDiplome(Long id) {
        diplomeRepository.deleteById(id);
    }
}
