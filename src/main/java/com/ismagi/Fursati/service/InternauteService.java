package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Internaute;
import com.ismagi.Fursati.repository.InternauteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternauteService {
    @Autowired
    private InternauteRepository internauteRepository;

    public List<Internaute> getAllInternautes() {
        return internauteRepository.findAll();
    }

    public Internaute getInternauteById(Long id) {
        return internauteRepository.findById(id).orElse(null);
    }

    public Internaute saveInternaute(Internaute internaute) {
        return internauteRepository.save(internaute);
    }

    public void deleteInternaute(Long id) {
        internauteRepository.deleteById(id);
    }
}
