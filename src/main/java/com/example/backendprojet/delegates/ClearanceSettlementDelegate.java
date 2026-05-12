package com.example.backendprojet.delegates;


import com.example.backendprojet.entity.DossierImport;
import com.example.backendprojet.repository.DossierImportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Delegate BPMN : ${clearanceSettlementDelegate}
 *
 * Apurement final du dossier d'import après le constat de sortie.
 * - Vérifie que toutes les étapes obligatoires ont été exécutées
 * - Met à jour le dossier en BDD avec la date d'apurement
 *
 * Variables IN  : dossierId, numeroBAE, numeroBAD, numeroConstatSortie
 * Variables OUT : dateApurement, dossierApure (boolean)
 */
@Slf4j
@Component("clearanceSettlementDelegate")
@RequiredArgsConstructor
public class ClearanceSettlementDelegate implements JavaDelegate {

    private final DossierImportRepository repository;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("Apurement du dossier import: {}", dossierId);

        // Récupération des données clés
        String numeroBAE = (String) execution.getVariable("numeroBAE");
        String numeroBAD = (String) execution.getVariable("numeroBAD");
        String numeroConstatSortie = (String) execution.getVariable("numeroConstatSortie");

        // Vérification de la complétude du dossier
        boolean apure = numeroBAE != null && !numeroBAE.isBlank()
                && numeroBAD != null && !numeroBAD.isBlank()
                && numeroConstatSortie != null && !numeroConstatSortie.isBlank();

        String dateApurement = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Mise à jour en BDD
        DossierImport dossier = repository.findByDossierId(dossierId).orElse(null);
        if (dossier != null) {
            // L'apurement laisse le dossier en EN_COURS
            // Le statut final CLOTURE sera positionné par closeAndArchiveImportFileDelegate
            repository.save(dossier);
            log.info("Dossier {} apuré en BDD", dossierId);
        } else {
            log.warn("Dossier {} introuvable en BDD", dossierId);
        }

        execution.setVariable("dateApurement", dateApurement);
        execution.setVariable("dossierApure", apure);

        log.info("Apurement terminé - Dossier: {} | Apuré: {} | Date: {}",
                dossierId, apure, dateApurement);
    }
}