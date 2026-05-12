package com.example.backendprojet.delegates;


import com.example.backendprojet.dto.ReleaseConditionsFact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

/**
 * Delegate BPMN : ${verifyReleaseConditionsDelegate}
 *
 * Vérifie que toutes les conditions sont réunies pour émettre
 * le Bon À Enlever (BAE) :
 *   - Paiement confirmé
 *   - Marchandise conforme
 *   - Contrôle conforme (si circuit ROUGE)
 * Règles Drools : bae-conditions-rules.drl
 *
 * Variables IN  : paiementConfirme, marchandiseConforme, controleConforme,
 *                 decisionCircuit
 * Variables OUT : conditionsBaeOK (boolean), motifBlocage (string)
 */
@Slf4j
@Component("verifyReleaseConditionsDelegate")
@RequiredArgsConstructor
public class VerifyReleaseConditionsDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) {
        log.info("[Drools] Vérification conditions BAE - dossier: {}",
                execution.getVariable("dossierId"));

        ReleaseConditionsFact fact = new ReleaseConditionsFact();
        fact.setPaiementConfirme((Boolean) execution.getVariable("paiementConfirme"));
        fact.setMarchandiseConforme((Boolean) execution.getVariable("marchandiseConforme"));
        fact.setControleConforme((Boolean) execution.getVariable("controleConforme"));
        fact.setDecisionCircuit((String) execution.getVariable("decisionCircuit"));

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(fact);
            int rulesFired = session.fireAllRules();
            log.info("[Drools] {} règles déclenchées", rulesFired);
        } finally {
            session.dispose();
        }

        execution.setVariable("conditionsBaeOK", fact.isConditionsOK());
        execution.setVariable("motifBlocage", fact.getMotifBlocage());

        log.info("[Drools] Conditions BAE OK: {} | Motif blocage: {}",
                fact.isConditionsOK(), fact.getMotifBlocage());
    }
}
