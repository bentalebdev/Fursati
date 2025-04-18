package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Diplome;
import com.ismagi.Fursati.service.DiplomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diplomes")
public class DiplomeController {
    @Autowired
    private DiplomeService diplomeService;

    @GetMapping
    public List<Diplome> getAllDiplomes() {
        return diplomeService.getAllDiplomes();
    }

    @GetMapping("/{id}")
    public Diplome getDiplomeById(@PathVariable Long id) {
        return diplomeService.getDiplomeById(id);
    }

    @PostMapping
    public Diplome createDiplome(@RequestBody Diplome diplome) {
        return diplomeService.saveDiplome(diplome);
    }

    @DeleteMapping("/{id}")
    public void deleteDiplome(@PathVariable Long id) {
        diplomeService.deleteDiplome(id);
    }
}
