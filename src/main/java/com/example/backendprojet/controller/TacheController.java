package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Tache;
import com.example.backendprojet.services.TacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taches")
@CrossOrigin("*")
public class TacheController {

    @Autowired
    private TacheService service;

    @PostMapping("/processes/{id}")
    public Tache add(@PathVariable Long id, @RequestBody Tache t) {
        return service.addTache(id, t);
    }

    @GetMapping("/processes/{id}")
    public List<Tache> getByProcessus(@PathVariable Long id) {
        return service.getByProcessus(id);
    }

    // ✅ Nouvelle méthode pour supprimer une tâche
    @DeleteMapping("/{id}")
    public void deleteTache(@PathVariable Long id) {
        service.deleteTache(id);
    }
    @PutMapping("/{id}")
    public Tache updateTache(@PathVariable Long id, @RequestBody Tache tache) {
        return service.updateTache(id, tache);
    }
}