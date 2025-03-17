package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Recruteur;
import com.ismagi.Fursati.service.RecruteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruteurs")
public class RecruteurController {
    @Autowired
    private RecruteurService recruteurService;

    @GetMapping
    public List<Recruteur> getAllRecruteurs() {
        return recruteurService.getAllRecruteurs();
    }

    @GetMapping("/{id}")
    public Recruteur getRecruteurById(@PathVariable Long id) {
        return recruteurService.getRecruteurById(id);
    }

    @PostMapping
    public Recruteur createRecruteur(@RequestBody Recruteur recruteur) {
        return recruteurService.saveRecruteur(recruteur);
    }

    @DeleteMapping("/{id}")
    public void deleteRecruteur(@PathVariable Long id) {
        recruteurService.deleteRecruteur(id);
    }
}
