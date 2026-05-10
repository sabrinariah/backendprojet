package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.entity.Exportateur;
import com.example.backendprojet.entity.Marchandise;
import com.example.backendprojet.repository.DossierExportRepository;
import com.example.backendprojet.rules.EligibilityResult;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("evaluateExportEligibilityDelegate")
public class EvaluateExportEligibilityDelegate implements JavaDelegate {

    @Autowired
    private DossierExportRepository dossierExportRepository;

    @Autowired
    private KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=== START EvaluateExportEligibilityDelegate ===");

        // 1. Récupérer le dossierId
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("📌 dossierId reçu = {}", dossierId);

        if (dossierId == null || dossierId.isBlank()) {
            throw new RuntimeException("Variable 'dossierId' manquante dans le processus !");
        }

        // 2. Charger le dossier
        DossierExport dossier = dossierExportRepository
                .findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new RuntimeException("Dossier introuvable : " + dossierId));

        // 3. Vérifier que les relations ne sont pas null
        Exportateur exportateur = dossier.getExportateur();
        Marchandise marchandise = dossier.getMarchandise();

        if (exportateur == null) {
            throw new RuntimeException("Exportateur null pour le dossier : " + dossierId);
        }
        if (marchandise == null) {
            throw new RuntimeException("Marchandise null pour le dossier : " + dossierId);
        }

        log.info("Exportateur: id={}, agree={}, enRegle={}",
                exportateur.getId(), exportateur.isAgree(), exportateur.isEnRegle());
        log.info("Marchandise: codeSH={}, valeurFob={}, poidsKg={}",
                marchandise.getCodeSH(), marchandise.getValeurFob(), marchandise.getPoidsKg());

        // 4. Créer EligibilityResult avec eligible = null (Boolean objet, pas boolean primitif)
        EligibilityResult result = new EligibilityResult();
        log.info("EligibilityResult initial: eligible={}", result.getEligible()); // doit afficher null

        // 5. Session Drools
        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(dossier);
            kieSession.insert(exportateur);
            kieSession.insert(marchandise);
            kieSession.insert(result);

            int rulesFired = kieSession.fireAllRules();
            log.info("🔥 Règles déclenchées : {}", rulesFired);

        } finally {
            kieSession.dispose();
        }

        log.info("=== END Delegate === eligible={} | code={} | reason={}",
                result.getEligible(), result.getCode(), result.getReason());

        // 6. Fallback si aucune règle déclenchée (ne doit pas arriver)
        if (result.getEligible() == null) {
            log.error("⚠️ Aucune règle Drools déclenchée ! Vérifier EligibilityResult.eligible = null (Boolean objet)");
            result.setEligible(false);
            result.setCode("ELIG_ERROR");
            result.setReason("Erreur évaluation Drools - aucune règle déclenchée");
        }

        // 7. Sauvegarder en base
        dossier.setExportEligible(result.getEligible());
        dossier.setMotifRejet(result.getReason());
        dossierExportRepository.save(dossier);

        // 8. ✅ Propager les variables vers la gateway BPMN
        execution.setVariable("exportEligible", result.getEligible());
        execution.setVariable("motifRejet", result.getReason() != null ? result.getReason() : "");

        log.info("✅ Variables BPMN propagées: exportEligible={}, motifRejet={}",
                result.getEligible(), result.getReason());
    }
}