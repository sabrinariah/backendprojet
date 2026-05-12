package com.example.backendprojet.services;

import com.example.backendprojet.entity.Categorie;
import com.example.backendprojet.entity.Condition;
import com.example.backendprojet.entity.RegleMetier;
import com.example.backendprojet.repository.CategorieRepository;
import com.example.backendprojet.repository.RegleMetierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegleMetierService {

    @Autowired
    private RegleMetierRepository repository;

    @Autowired
    private CategorieRepository categorieRepository;

    public List<RegleMetier> getAll() {
        return repository.findAll();
    }

    public RegleMetier getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Règle non trouvée : " + id));
    }

    public RegleMetier create(RegleMetier r) {
        // version par défaut
        if (r.getVersion() == null) {
            r.setVersion(1);
        }

        // recharger la catégorie depuis la BDD (sinon erreur de detached entity)
        if (r.getCategorie() != null && r.getCategorie().getId() != null) {
            Categorie cat = categorieRepository.findById(r.getCategorie().getId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            r.setCategorie(cat);
        }

        // lier chaque condition à la règle
        if (r.getConditions() != null) {
            for (Condition c : r.getConditions()) {
                c.setRegleMetier(r);
            }
        }

        return repository.save(r);
    }

    public RegleMetier update(Long id, RegleMetier r) {
        RegleMetier existing = getById(id);

        existing.setCode(r.getCode());
        existing.setNom(r.getNom());
        existing.setAction(r.getAction());
        existing.setActive(r.isActive());

        // ✅ incrémenter la version à chaque modification
        Integer currentVersion = existing.getVersion() == null ? 1 : existing.getVersion();
        existing.setVersion(currentVersion + 1);

        // catégorie
        if (r.getCategorie() != null && r.getCategorie().getId() != null) {
            Categorie cat = categorieRepository.findById(r.getCategorie().getId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            existing.setCategorie(cat);
        }

        // conditions : on remplace complètement la liste
        existing.getConditions().clear();
        if (r.getConditions() != null) {
            for (Condition c : r.getConditions()) {
                c.setId(null); // forcer la création
                c.setRegleMetier(existing);
                existing.getConditions().add(c);
            }
        }

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public RegleMetier toggle(Long id) {
        RegleMetier r = getById(id);
        r.setActive(!r.isActive());
        return repository.save(r);
    }
}