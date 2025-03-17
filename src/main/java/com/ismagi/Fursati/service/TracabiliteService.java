package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Tracabilite;
import com.ismagi.Fursati.repository.TracabiliteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TracabiliteService {
    @Autowired
    private TracabiliteRepository tracabiliteRepository;

    public List<Tracabilite> getAllTracabilites() {
        return tracabiliteRepository.findAll();
    }

    public Tracabilite getTracabiliteById(Long id) {
        return tracabiliteRepository.findById(id).orElse(null);
    }

    public Tracabilite saveTracabilite(Tracabilite tracabilite) {
        return tracabiliteRepository.save(tracabilite);
    }

    public void deleteTracabilite(Long id) {
        tracabiliteRepository.deleteById(id);
    }
}
