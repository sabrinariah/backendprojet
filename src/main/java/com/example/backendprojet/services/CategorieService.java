package com.example.backendprojet.services;

import com.example.backendprojet.entity.Categorie;
import com.example.backendprojet.repository.CategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieService {

    @Autowired
    private CategorieRepository repo;

    // ================= GET ALL =================
    public List<Categorie> getAll() {
        return repo.findAll();
    }

    // ================= GET BY ID =================
    public Categorie getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
    }

    // ================= CREATE =================
    public Categorie create(Categorie c) {
        return repo.save(c);
    }

    // ================= UPDATE =================
    public Categorie update(Long id, Categorie c) {

        Categorie existing = getById(id);

        existing.setNom(c.getNom());
        existing.setType(c.getType());
        existing.setDescription(c.getDescription());

        return repo.save(existing);
    }

    // ================= DELETE =================
    public void delete(Long id) {
        repo.deleteById(id);
    }
}