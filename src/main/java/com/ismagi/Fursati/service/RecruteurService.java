package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Recruteur;
import com.ismagi.Fursati.repository.RecruteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecruteurService {
    @Autowired
    private RecruteurRepository recruteurRepository;

    public List<Recruteur> getAllRecruteurs() {
        return recruteurRepository.findAll();
    }

    public Recruteur getRecruteurById(Long id) {
        return recruteurRepository.findById(id).orElse(null);
    }

    public Recruteur saveRecruteur(Recruteur recruteur) {
        return recruteurRepository.save(recruteur);
    }

    public void deleteRecruteur(Long id) {
        recruteurRepository.deleteById(id);
    }
}
