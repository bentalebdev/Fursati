package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offres")
public class OffreController {
    @Autowired
    private OffreService offreService;

    @GetMapping
    public List<Offre> getAllOffres() {
        return offreService.getAllOffres();
    }

    @GetMapping("/{id}")
    public Offre getOffreById(@PathVariable Long id) {
        return offreService.getOffreById(id);
    }

    @PostMapping
    public Offre createOffre(@RequestBody Offre offre) {
        return offreService.saveOffre(offre);
    }

    @DeleteMapping("/{id}")
    public void deleteOffre(@PathVariable Long id) {
        offreService.deleteOffre(id);
    }
}
