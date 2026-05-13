package com.example.backendprojet.controller;


import  com.example.backendprojet.dto.TacheDTO;
import com.example.backendprojet.services.TacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TacheController {

    private final TacheService service;

    @GetMapping
    public List<TacheDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/processus/{processusId}")
    public List<TacheDTO> getByProcessus(@PathVariable Long processusId) {
        return service.findByProcessus(processusId);
    }

    @GetMapping("/{id}")
    public TacheDTO getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public TacheDTO create(@RequestBody TacheDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public TacheDTO update(@PathVariable Long id, @RequestBody TacheDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}