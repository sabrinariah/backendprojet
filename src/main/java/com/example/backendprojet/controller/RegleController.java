package com.example.backendprojet.controller;

import com.example.backendprojet.entity.*;
import com.example.backendprojet.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regles")
@CrossOrigin(origins = "*")
public class RegleController {

    @Autowired
    private RegleMetierService service;

    @Autowired
    private ConditionService conditionService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private CategorieService categorieService;

    // ================= REGLES =================

    @GetMapping
    public List<RegleMetier> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RegleMetier getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public RegleMetier create(@RequestBody RegleMetier r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    public RegleMetier update(@PathVariable Long id, @RequestBody RegleMetier r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ✅ URL corrigée pour correspondre au frontend
    @PutMapping("/{id}/toggle")
    public RegleMetier toggle(@PathVariable Long id) {
        return service.toggle(id);
    }

    // ================= CONDITIONS =================

    @GetMapping("/{id}/conditions")
    public List<Condition> getConditions(@PathVariable Long id) {
        return conditionService.findByRegleId(id);
    }

    // ================= VERSIONS =================

    @GetMapping("/{id}/versions")
    public List<Version> getVersions(@PathVariable Long id) {
        return versionService.findByRegleId(id);
    }

    // ================= CATEGORIES =================

    @GetMapping("/categories")
    public List<Categorie> getCategories() {
        return categorieService.getAll();
    }
}