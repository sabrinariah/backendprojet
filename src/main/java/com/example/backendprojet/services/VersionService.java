package com.example.backendprojet.services;

import com.example.backendprojet.entity.RegleMetier;
import com.example.backendprojet.entity.Version;
import com.example.backendprojet.repository.RegleMetierRepository;
import com.example.backendprojet.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionService {

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private RegleMetierRepository regleMetierRepository;

    public List<Version> getAll() {
        return versionRepository.findAll();
    }

    public List<Version> findByRegleId(Long regleId) {
        return versionRepository.findByRegleMetier_Id(regleId);
    }

    public Version getById(Long id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version non trouvée : " + id));
    }

    public Version create(Version v) {
        // Recharger la règle depuis la BDD si elle est fournie
        if (v.getRegleMetier() != null && v.getRegleMetier().getId() != null) {
            RegleMetier regle = regleMetierRepository.findById(v.getRegleMetier().getId())
                    .orElseThrow(() -> new RuntimeException("Règle introuvable"));
            v.setRegleMetier(regle);
        }
        return versionRepository.save(v);
    }

    public Version update(Long id, Version v) {
        Version existing = getById(id);
        existing.setNumero(v.getNumero());
        existing.setDescription(v.getDescription());
        return versionRepository.save(existing);
    }

    public void delete(Long id) {
        versionRepository.deleteById(id);
    }
}