package com.example.backendprojet.services;

import com.example.backendprojet.entity.Version;
import com.example.backendprojet.repository.VersionRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class VersionService {

    private final VersionRepository versionRepository;

    public VersionService(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    // ✅ 1. toutes les versions
    public List<Version> getAllVersions() {
        return versionRepository.findAll();
    }

    // ✅ 2. versions par règle métier
    public List<Version> getVersionsByRegleId(Long regleId) {
        return versionRepository.findByRegleMetier_Id(regleId);
    }

    // ✅ 3. dernière version d'une règle métier
    public Version getLastVersionByRegleId(Long regleId) {
        return versionRepository.findByRegleMetier_Id(regleId)
                .stream()
                .max(Comparator.comparing(Version::getDateCreation))
                .orElse(null);
    }
    public List<Version> findByRegleId(Long regleId) {
        return versionRepository.findByRegleMetier_Id(regleId);
    }
}