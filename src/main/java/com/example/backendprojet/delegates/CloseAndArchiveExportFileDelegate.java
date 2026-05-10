package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.repository.DossierExportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component("closeAndArchiveExportFileDelegate")
@RequiredArgsConstructor
@Slf4j
public class CloseAndArchiveExportFileDelegate implements JavaDelegate {

    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[EXPORT] CloseAndArchive - dossier={}", dossierId);

        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND", "Dossier introuvable : " + dossierId));

        dossier.setStatut(StatutDossier.EXPORTE);
        dossier.setDateCloture(LocalDateTime.now());
        dossierRepo.save(dossier);

        // Variable finale pour traçabilité
        execution.setVariable("dossierCloture", true);
        execution.setVariable("dateCloture", LocalDateTime.now().toString());

        // TODO : déclencher archivage GED, notification finale, mise à jour stats
        log.info("[EXPORT] Dossier {} clôturé et archivé avec succès", dossier.getReference());
    }
}