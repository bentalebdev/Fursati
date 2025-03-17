package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Tracabilite;
import com.ismagi.Fursati.service.TracabiliteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracabilites")
public class TracabiliteController {
    @Autowired
    private TracabiliteService tracabiliteService;

    @GetMapping
    public List<Tracabilite> getAllTracabilites() {
        return tracabiliteService.getAllTracabilites();
    }

    @GetMapping("/{id}")
    public Tracabilite getTracabiliteById(@PathVariable Long id) {
        return tracabiliteService.getTracabiliteById(id);
    }

    @PostMapping
    public Tracabilite createTracabilite(@RequestBody Tracabilite tracabilite) {
        return tracabiliteService.saveTracabilite(tracabilite);
    }

    @DeleteMapping("/{id}")
    public void deleteTracabilite(@PathVariable Long id) {
        tracabiliteService.deleteTracabilite(id);
    }
}
