package com.example.backendprojet.delegates;



import com.example.backendprojet.dto.ImportEligibilityFact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

/**
 * Delegate BPMN : ${evaluateImportEligibilityDelegate}
 *
 * Service de vérification de l'éligibilité d'une importation.
 * Évalue les règles Drools définies dans : eligibility-rules.drl
 *
 * Variables IN  : dossierId, importateur, paysOrigine, typeProduit
 * Variables OUT : importEligible (boolean), motifRejet (string)
 */
@Slf4j
@Component("evaluateImportEligibilityDelegate")
@RequiredArgsConstructor
public class EvaluateImportEligibilityDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[Drools] Évaluation éligibilité import - dossier: {}", dossierId);

        // Construction du Fact à partir des variables du processus
        ImportEligibilityFact fact = new ImportEligibilityFact();
        fact.setDossierId(dossierId);
        fact.setImportateur((String) execution.getVariable("importateur"));
        fact.setPaysOrigine((String) execution.getVariable("paysOrigine"));
        fact.setTypeProduit((String) execution.getVariable("typeProduit"));

        // Exécution des règles Drools
        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(fact);
            int rulesFired = session.fireAllRules();
            log.info("[Drools] {} règles déclenchées", rulesFired);
        } finally {
            session.dispose();
        }

        // Stockage du résultat dans les variables du processus
        execution.setVariable("importEligible", fact.isEligible());
        execution.setVariable("motifRejet", fact.getMotifRejet());

        log.info("[Drools] Résultat - Éligible: {} | Motif: {}",
                fact.isEligible(), fact.getMotifRejet());
    }
}