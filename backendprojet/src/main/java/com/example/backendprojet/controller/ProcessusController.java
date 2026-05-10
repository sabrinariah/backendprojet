package com.example.backendprojet.controller;



import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.entity.Tache;
import com.example.backendprojet.services.ProcessusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/processus")
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessusController {

    private final ProcessusService processusService;

    public ProcessusController(ProcessusService processusService) {
        this.processusService = processusService;
    }

    @GetMapping
    public List<Processus> getAll() {
        return processusService.getAllProcessus();
    }

    @GetMapping("/{id}")
    public Processus getById(@PathVariable Long id) {
        return processusService.getProcessusById(id);
    }

    @PostMapping
    public Processus create(@RequestBody Processus processus) {
        return processusService.createProcessus(processus);
    }

    @PutMapping("/{id}")
    public Processus update(@PathVariable Long id, @RequestBody Processus processus) {
        return processusService.updateProcessus(id, processus);
    }

    @PatchMapping("/{id}/toggle")
    public Processus toggle(@PathVariable Long id) {
        return processusService.toggleProcessus(id);
    }

    @PostMapping("/{id}/taches")
    public Tache addTache(@PathVariable Long id, @RequestBody Tache tache) {
        return processusService.addTacheToProcessus(id, tache);
    }

    @GetMapping("/{id}/taches")
    public List<Tache> getTaches(@PathVariable Long id) {
        return processusService.getTachesByProcessus(id);
    }

    @PutMapping("/taches/{id}")
    public Tache updateTache(@PathVariable Long id, @RequestBody Tache tache) {
        return processusService.updateTache(id, tache);
    }
    @GetMapping("/test")
    public String test() {
        return "API Processus OK";
    }
    // Supprimer un processus
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcessus(@PathVariable Long id) {
        processusService.deleteProcessus(id);
        return ResponseEntity.noContent().build();
    }

    // Supprimer une tâche
    @DeleteMapping("/taches/{id}")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        processusService.deleteTache(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{processusId}/taches/{tacheId}")
    public Tache updateTache(
            @PathVariable Long processusId,
            @PathVariable Long tacheId,
            @RequestBody Tache tache
    ) {
        return processusService.updateTache(processusId, tacheId, tache);
    }
}