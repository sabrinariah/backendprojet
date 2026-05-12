package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Version;
import com.example.backendprojet.services.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@CrossOrigin(origins = "*")
public class VersionController {

    @Autowired
    private VersionService versionService;

    // ================= GET ALL =================
    @GetMapping
    public List<Version> getAll() {
        return versionService.getAll();
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<Version> getById(@PathVariable Long id) {
        try {
            Version v = versionService.getById(id);
            return ResponseEntity.ok(v);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================= GET BY REGLE =================
    @GetMapping("/regle/{regleId}")
    public List<Version> getByRegle(@PathVariable Long regleId) {
        return versionService.findByRegleId(regleId);
    }

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<Version> create(@RequestBody Version v) {
        try {
            Version created = versionService.create(v);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<Version> update(@PathVariable Long id, @RequestBody Version v) {
        try {
            Version updated = versionService.update(id, v);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            versionService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}