package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.entity.Exportateur;
import com.example.backendprojet.entity.Marchandise;
import com.example.backendprojet.repository.DossierExportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("verifyExportPreClearanceDelegate")
@RequiredArgsConstructor
@Slf4j
public class VerifyExportPreClearanceDelegate implements JavaDelegate {

    private final KieContainer kieContainer;
    private final DossierExportRepository dossierRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(DelegateExecution execution) {

        log.info("========== VERIFY EXPORT PRE CLEARANCE ==========");

        try {

            // =========================================================
            // 1. dossierId
            // =========================================================
            Object rawId = execution.getVariable("dossierId");

            if (rawId == null) {
                fail(execution, "dossierId manquant");
                return;
            }

            UUID dossierId = UUID.fromString(String.valueOf(rawId));

            // =========================================================
            // 2. Charger dossier
            // =========================================================
            DossierExport dossier = dossierRepo.findById(dossierId).orElse(null);

            if (dossier == null) {
                fail(execution, "Dossier introuvable");
                return;
            }

            Exportateur exportateur = dossier.getExportateur();
            Marchandise marchandise = dossier.getMarchandise();

            // =========================================================
            // 3. Drools result (OBJET LOCAL UNIQUEMENT)
            // =========================================================
            com.example.backendprojet.rules.PreClearanceResult result =
                    new com.example.backendprojet.rules.PreClearanceResult();

            // =========================================================
            // 4. KIE SESSION
            // =========================================================
            KieSession session = kieContainer.newKieSession();

            try {
                session.insert(dossier);

                if (exportateur != null) session.insert(exportateur);
                if (marchandise != null) session.insert(marchandise);

                session.insert(result);

                int fired = session.fireAllRules();

                log.info("🔥 Règles exécutées = {}", fired);

            } finally {
                session.dispose();
            }

            // =========================================================
            // 5. fallback sécurité
            // =========================================================
            if (result.getConforme() == null) {
                result.setConforme(true);
                result.setMotif("Pré-dédouanement validé par défaut");
            }

            boolean conforme = Boolean.TRUE.equals(result.getConforme());

            // =========================================================
            // 6. SAUVEGARDE DB
            // =========================================================
            dossier.setPreClearanceConforme(conforme);
            dossier.setMotifNonConformitePreClearance(result.getMotif());

            dossierRepo.save(dossier);

            // =========================================================
            // 7. VARIABLES BPMN (UNIQUEMENT TYPES PRIMITIFS)
            // =========================================================
            execution.setVariable("preClearanceExportConforme", conforme);
            execution.setVariable("motifNonConformitePreClearance", result.getMotif());

            // ✅ BONUS : si tu veux garder le résultat complet
            execution.setVariable(
                    "preClearanceResultJson",
                    objectMapper.writeValueAsString(result)
            );

            log.info("✅ PRE CLEARANCE TERMINÉE : conforme={}", conforme);

        } catch (Exception e) {

            log.error("❌ Erreur VerifyExportPreClearanceDelegate", e);
            fail(execution, e.getMessage());
        }
    }

    private void fail(DelegateExecution execution, String message) {

        execution.setVariable("preClearanceExportConforme", false);
        execution.setVariable("errorMessage", message);

        log.error("❌ FAIL PRE CLEARANCE : {}", message);
    }
}