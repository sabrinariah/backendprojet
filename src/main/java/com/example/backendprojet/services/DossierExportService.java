package com.example.backendprojet.services;


import com.example.backendprojet.dto.DossierRequestDTO;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.entity.Exportateur;
import com.example.backendprojet.entity.Marchandise;
import com.example.backendprojet.exception.ResourceNotFoundException;
import com.example.backendprojet.repository.DossierExportRepository;
import com.example.backendprojet.repository.ExportateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierExportService {

    private static final String PROCESS_KEY = "Process_Export";

    private final DossierExportRepository dossierRepo;
    private final ExportateurRepository exportateurRepo;
    private final RuntimeService runtimeService;

    @Transactional
    public DossierExport creerEtDemarrer(DossierRequestDTO request, String creePar) {
        log.info("Création dossier export par {}", creePar);

        Exportateur exportateur = exportateurRepo.findById(request.getExportateurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exportateur non trouvé: " + request.getExportateurId()));

        Marchandise marchandise = Marchandise.builder()
                .designation(request.getDesignationMarchandise())
                .codeSH(request.getCodeSH())
                .type(request.getTypeMarchandise())
                .poidsKg(request.getPoidsKg())
                .valeurFob(request.getValeurFob())
                .devise(request.getDevise())
                .origine(request.getOrigine())
                .destination(request.getDestination())
                .dangereuse(request.isDangereuse())
                .perissable(request.isPerissable())
                .necessiteCertificatPhytosanitaire(request.isNecessiteCertificatPhytosanitaire())
                .build();

        DossierExport dossier = DossierExport.builder()
                .reference(genererReference())
                .statut(StatutDossier.EN_COURS)
                .exportateur(exportateur)
                .marchandise(marchandise)
                .creePar(creePar)
                .dateCreation(LocalDateTime.now())
                .build();

        dossier = dossierRepo.save(dossier);

        Map<String, Object> variables = new HashMap<>();
        variables.put("dossierId", dossier.getId().toString());
        variables.put("reference", dossier.getReference());
        variables.put("exportateurId", exportateur.getId().toString());
        variables.put("creePar", creePar);

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                PROCESS_KEY, dossier.getReference(), variables);

        dossier.setProcessInstanceId(instance.getId());
        dossier.setBusinessKey(instance.getBusinessKey());

        log.info("Dossier {} créé et processus {} démarré",
                dossier.getReference(), instance.getId());

        return dossierRepo.save(dossier);
    }

    public DossierExport getById(UUID id) {
        return dossierRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouvé: " + id));
    }

    public DossierExport getByReference(String reference) {
        return dossierRepo.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouvé: " + reference));
    }

    public Page<DossierExport> rechercher(StatutDossier statut, Pageable pageable) {
        if (statut != null) {
            return dossierRepo.findByStatut(statut, pageable);
        }
        return dossierRepo.findAll(pageable);
    }

    public Page<DossierExport> mesDossiers(String username, Pageable pageable) {
        return dossierRepo.findByCreePar(username, pageable);
    }

    @Transactional
    public DossierExport mettreAJour(UUID id, DossierExport mise) {
        DossierExport existant = getById(id);
        if (mise.getNumeroDeclarationDouane() != null)
            existant.setNumeroDeclarationDouane(mise.getNumeroDeclarationDouane());
        if (mise.getBanqueDomiciliation() != null)
            existant.setBanqueDomiciliation(mise.getBanqueDomiciliation());
        if (mise.getNumeroBESC() != null)
            existant.setNumeroBESC(mise.getNumeroBESC());
        if (mise.getNumeroCertificatOrigine() != null)
            existant.setNumeroCertificatOrigine(mise.getNumeroCertificatOrigine());
        if (mise.getNumeroCertificatPhytosanitaire() != null)
            existant.setNumeroCertificatPhytosanitaire(mise.getNumeroCertificatPhytosanitaire());
        if (mise.getNumeroBonAEmbarquer() != null)
            existant.setNumeroBonAEmbarquer(mise.getNumeroBonAEmbarquer());
        return dossierRepo.save(existant);
    }

    private String genererReference() {
        String ref;
        do {
            int annee = LocalDateTime.now().getYear();
            int rand = ThreadLocalRandom.current().nextInt(100000, 999999);
            ref = String.format("EXP-%d-%06d", annee, rand);
        } while (dossierRepo.existsByReference(ref));
        return ref;
    }
}