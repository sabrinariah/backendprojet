package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Version;
import com.example.backendprojet.services.VersionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@CrossOrigin("*")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    // GET: toutes les versions
    @GetMapping
    public List<Version> getAllVersions() {
        return versionService.getAllVersions();
    }

    // GET: versions d'une règle
    @GetMapping("/regle/{id}")
    public List<Version> getVersionsByRegle(@PathVariable Long id) {
        return versionService.getVersionsByRegleId(id);
    }

    // GET: dernière version d'une règle
    @GetMapping("/regle/{id}/last")
    public Version getLastVersion(@PathVariable Long id) {
        return versionService.getLastVersionByRegleId(id);
    }
}