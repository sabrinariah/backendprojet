package com.example.backendprojet.services;

import com.example.backendprojet.entity.RegleMetier;
import com.example.backendprojet.entity.Version;
import com.example.backendprojet.repository.RegleMetierRepository;
import com.example.backendprojet.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RegleMetierService {

    @Autowired
    private RegleMetierRepository repo;

    @Autowired
    private VersionRepository versionRepository;

    // ================= GET ALL =================
    public List<RegleMetier> getAll() {
        return repo.findAll();
    }

    // ================= GET BY ID =================
    public RegleMetier getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Règle introuvable avec ID: " + id));
    }

    // ================= CREATE =================
    public RegleMetier create(RegleMetier r) {

        r.setActive(true);

        RegleMetier saved = repo.save(r);

        createVersion(saved, "v1", "Création initiale");

        return saved;
    }

    // ================= UPDATE =================
    public RegleMetier update(Long id, RegleMetier r) {

        RegleMetier existing = getById(id);

        existing.setCode(r.getCode());
        existing.setNom(r.getNom());
        existing.setAction(r.getAction());
        existing.setCategorie(r.getCategorie());

        RegleMetier updated = repo.save(existing);

        int nextVersion = versionRepository
                .findByRegleMetier_Id(id)
                .size() + 1;

        createVersion(updated, "v" + nextVersion, "Mise à jour règle");

        return updated;
    }

    // ================= DELETE (FIX IMPORTANT) =================
    public void delete(Long id) {

        RegleMetier regle = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Règle introuvable avec ID: " + id));

        // 🔥 supprimer les versions avant la règle (évite FK error)
        if (regle.getVersions() != null && !regle.getVersions().isEmpty()) {
            versionRepository.deleteAll(regle.getVersions());
        }

        repo.delete(regle);
    }

    // ================= TOGGLE =================
    public RegleMetier toggle(Long id) {
        RegleMetier r = getById(id);
        r.setActive(!r.isActive());
        return repo.save(r);
    }

    // ================= VERSION HELPER =================
    private void createVersion(RegleMetier r, String version, String desc) {

        Version v = new Version();
        v.setNumeroVersion(version);
        v.setDateCreation(new Date());
        v.setDescription(desc);
        v.setRegleMetier(r);

        versionRepository.save(v);
    }
}