package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.service.CandidatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidats")
public class CandidatController {
    @Autowired
    private CandidatService candidatService;

    @GetMapping
    public List<Candidat> getAllCandidats() {
        return candidatService.getAllCandidats();
    }

    @GetMapping("/{id}")
    public Candidat getCandidatById(@PathVariable Long id) {
        return candidatService.getCandidatById(id);
    }

    @PostMapping
    public Candidat createCandidat(@RequestBody Candidat candidat) {
        return candidatService.saveCandidat(candidat);
    }

    @DeleteMapping("/{id}")
    public void deleteCandidat(@PathVariable Long id) {
        candidatService.deleteCandidat(id);
    }
}
