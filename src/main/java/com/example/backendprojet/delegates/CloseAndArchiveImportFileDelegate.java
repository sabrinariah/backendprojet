package com.example.backendprojet.delegates;



import com.example.backendprojet.entity.DossierImport;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.repository.DossierImportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Delegate BPMN : ${closeAndArchiveImportFileDelegate}
 *
 * Clôture définitive et archivage du dossier d'import.
 * - Passe le statut du dossier à CLOTURE
 * - Enregistre la date de clôture
 * - Génère une référence d'archivage
 *
 * Variables IN  : dossierId
 * Variables OUT : dateCloture, referenceArchivage, dossierClos (boolean)
 */
@Slf4j
@Component("closeAndArchiveImportFileDelegate")
@RequiredArgsConstructor
public class CloseAndArchiveImportFileDelegate implements JavaDelegate {

    private final DossierImportRepository repository;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("Clôture et archivage du dossier: {}", dossierId);

        LocalDateTime now = LocalDateTime.now();
        String dateCloture = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String referenceArchivage = "ARCH-IMP-" + now.getYear() + "-"
                + dossierId.replaceAll("[^A-Z0-9]", "");

        // Mise à jour en BDD : passage au statut CLOTURE
        DossierImport dossier = repository.findByDossierId(dossierId).orElse(null);
        boolean closSucces = false;

        if (dossier != null) {
            dossier.setStatut(StatutDossier.CLOTURE);
            repository.save(dossier);
            closSucces = true;
            log.info("Dossier {} clos avec succès | Réf archivage: {}",
                    dossierId, referenceArchivage);
        } else {
            log.error("Dossier {} introuvable - clôture impossible", dossierId);
        }

        execution.setVariable("dateCloture", dateCloture);
        execution.setVariable("referenceArchivage", referenceArchivage);
        execution.setVariable("dossierClos", closSucces);

        log.info("✅ Processus import terminé - Dossier: {} | Statut: CLOTURE", dossierId);
    }
}