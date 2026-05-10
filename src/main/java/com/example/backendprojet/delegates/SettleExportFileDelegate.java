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

@Component("settleExportFileDelegate")
@RequiredArgsConstructor
@Slf4j
public class SettleExportFileDelegate implements JavaDelegate {

    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[EXPORT] SettleExportFile (apurement) - dossier={}", dossierId);

        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND", "Dossier introuvable : " + dossierId));

        // Apurement : vérification que toutes les obligations douanières sont soldées
        boolean apurementOK = verifierApurement(dossier);

        dossier.setStatut(apurementOK ? StatutDossier.APURE : StatutDossier.INCIDENT_APUREMENT);
        dossier.setDateApurement(LocalDateTime.now());
        dossierRepo.save(dossier);

        execution.setVariable("apurementOK",    apurementOK);
        execution.setVariable("dateApurement",  LocalDateTime.now().toString());

        // TODO : intégration SYDONIA / CAMCIS pour apurement automatique
        log.info("[EXPORT] Apurement {} pour dossier {}", apurementOK ? "OK" : "KO", dossier.getReference());
    }

    private boolean verifierApurement(DossierExport dossier) {
        // Conditions d'apurement :
        // 1. Paiement confirmé
        // 2. Constat d'embarquement existant
        // 3. Statut cohérent
        return Boolean.TRUE.equals(dossier.getPaiementConfirme())
                && Boolean.TRUE.equals(dossier.getConditionsEmbarquementOK());
    }
}