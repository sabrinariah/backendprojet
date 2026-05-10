package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Categorie;
import com.example.backendprojet.services.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
public class CategorieController {

    @Autowired
    private CategorieService service;

    @PostMapping
    public Categorie create(@RequestBody Categorie c) {
        return service.create(c);
    }

    @GetMapping
    public List<Categorie> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Categorie getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Categorie update(@PathVariable Long id, @RequestBody Categorie c) {
        return service.update(id, c);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}