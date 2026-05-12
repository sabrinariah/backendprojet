package com.example.backendprojet.delegates;


import com.example.backendprojet.dto.TaxCalculationFact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

/**
 * Delegate BPMN : ${calculateDutiesAndTaxesDelegate}
 *
 * Calcule les droits de douane, la TVA et les autres taxes.
 * Applique les taux préférentiels UEMOA si applicables.
 * Règles Drools : tax-calculation-rules.drl
 *
 * Variables IN  : valeurCAF, codeSH, paysOrigine
 * Variables OUT : droitsDouane, tva, autresTaxes, totalTaxes
 */
@Slf4j
@Component("calculateDutiesAndTaxesDelegate")
@RequiredArgsConstructor
public class CalculateDutiesAndTaxesDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) {
        log.info("[Drools] Calcul droits et taxes - dossier: {}",
                execution.getVariable("dossierId"));

        TaxCalculationFact fact = new TaxCalculationFact();
        fact.setValeurCAF((Long) execution.getVariable("valeurCAF"));
        fact.setCodeSH((String) execution.getVariable("codeSH"));
        fact.setPaysOrigine((String) execution.getVariable("paysOrigine"));

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(fact);
            int rulesFired = session.fireAllRules();
            log.info("[Drools] {} règles déclenchées", rulesFired);
        } finally {
            session.dispose();
        }

        execution.setVariable("droitsDouane", fact.getDroitsDouane());
        execution.setVariable("tva", fact.getTva());
        execution.setVariable("autresTaxes", fact.getAutresTaxes());
        execution.setVariable("totalTaxes", fact.getTotalTaxes());

        log.info("[Drools] Taxes calculées - Droits: {} | TVA: {} | Autres: {} | TOTAL: {}",
                fact.getDroitsDouane(), fact.getTva(),
                fact.getAutresTaxes(), fact.getTotalTaxes());
    }
}