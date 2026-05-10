package com.example.backendprojet.controller;



import com.example.backendprojet.dto.DossierRequestDTO;
import com.example.backendprojet.dto.DossierResponse;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.Mappers.DossierMapper;
import com.example.backendprojet.services.DossierExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dossiers")
@RequiredArgsConstructor
@Tag(name = "Dossiers Export", description = "Gestion des dossiers d'exportation")
@CrossOrigin(origins = "*")
public class DossierExportController {

    private final DossierExportService service;
    private final DossierMapper mapper;

    @PostMapping
    @Operation(summary = "Créer un nouveau dossier export et démarrer le processus")
    public ResponseEntity<DossierResponse> creer(
            @Valid @RequestBody DossierRequestDTO request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "anonyme";
        DossierExport dossier = service.creerEtDemarrer(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(dossier));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un dossier par son identifiant")
    public DossierResponse getById(@PathVariable UUID id) {
        return mapper.toDTO(service.getById(id));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Récupérer un dossier par sa référence")
    public DossierResponse getByReference(@PathVariable String reference) {
        return mapper.toDTO(service.getByReference(reference));
    }

    @GetMapping
    @Operation(summary = "Lister/rechercher les dossiers")
    public Page<DossierResponse> rechercher(
            @RequestParam(required = false) StatutDossier statut,
            Pageable pageable) {
        return service.rechercher(statut, pageable).map(mapper::toDTO);
    }

    @GetMapping("/me")
    @Operation(summary = "Mes dossiers")
    public Page<DossierResponse> mesDossiers(Principal principal, Pageable pageable) {
        String username = principal != null ? principal.getName() : "anonyme";
        return service.mesDossiers(username, pageable).map(mapper::toDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour un dossier")
    public DossierResponse mettreAJour(@PathVariable UUID id, @RequestBody DossierExport mise) {
        return mapper.toDTO(service.mettreAJour(id, mise));
    }
}