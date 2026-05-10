package com.example.backendprojet.controller;


import com.example.backendprojet.entity.Exportateur;
import com.example.backendprojet.repository.ExportateurRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exportateurs")
@RequiredArgsConstructor
@Tag(name = "Exportateurs")
@CrossOrigin(origins = "*")
public class ExportateurController {

    private final ExportateurRepository repo;

    @GetMapping
    public List<Exportateur> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Exportateur getById(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public ResponseEntity<Exportateur> creer(@Valid @RequestBody Exportateur exportateur) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(exportateur));
    }
}