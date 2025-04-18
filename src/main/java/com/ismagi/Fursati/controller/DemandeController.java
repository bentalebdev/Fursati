package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Demande;
import com.ismagi.Fursati.service.DemandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demandes")
public class DemandeController {
    @Autowired
    private DemandeService demandeService;

    @GetMapping
    public List<Demande> getAllDemandes() {
        return demandeService.getAllDemandes();
    }

    @GetMapping("/{id}")
    public Demande getDemandeById(@PathVariable Long id) {
        return demandeService.getDemandeById(id);
    }

    @PostMapping
    public Demande createDemande(@RequestBody Demande demande) {
        return demandeService.saveDemande(demande);
    }

    @DeleteMapping("/{id}")
    public void deleteDemande(@PathVariable Long id) {
        demandeService.deleteDemande(id);
    }
}
