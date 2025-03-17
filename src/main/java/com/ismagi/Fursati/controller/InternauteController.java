package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Internaute;
import com.ismagi.Fursati.service.InternauteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internautes")
public class InternauteController {
    @Autowired
    private InternauteService internauteService;

    @GetMapping
    public List<Internaute> getAllInternautes() {
        return internauteService.getAllInternautes();
    }

    @GetMapping("/{id}")
    public Internaute getInternauteById(@PathVariable Long id) {
        return internauteService.getInternauteById(id);
    }

    @PostMapping
    public Internaute createInternaute(@RequestBody Internaute internaute) {
        return internauteService.saveInternaute(internaute);
    }

    @DeleteMapping("/{id}")
    public void deleteInternaute(@PathVariable Long id) {
        internauteService.deleteInternaute(id);
    }
}
