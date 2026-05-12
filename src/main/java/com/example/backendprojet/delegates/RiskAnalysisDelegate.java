package com.example.backendprojet.delegates;


import com.example.backendprojet.dto.RiskAnalysisFact;
import com.example.backendprojet.entity.CircuitDouane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

/**
 * Delegate BPMN : ${riskAnalysisDelegate}
 *
 * Analyse le risque douanier et détermine le circuit :
 *   - VERT   : Mainlevée immédiate (risque faible)
 *   - ORANGE : Contrôle documentaire (risque moyen)
 *   - ROUGE  : Inspection physique (risque élevé)
 * Règles Drools : risk-analysis-rules.drl
 *
 * Variables IN  : paysOrigine, typeProduit, valeurCAF, codeSH
 * Variables OUT : decisionCircuit (VERT|ORANGE|ROUGE)
 */
@Slf4j
@Component("riskAnalysisDelegate")
@RequiredArgsConstructor
public class RiskAnalysisDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) {
        String numeroDeclaration = (String) execution.getVariable("numeroDeclaration");
        log.info("[Drools] Analyse de risque - DAU: {}", numeroDeclaration);

        RiskAnalysisFact fact = new RiskAnalysisFact();
        fact.setPaysOrigine((String) execution.getVariable("paysOrigine"));
        fact.setTypeProduit((String) execution.getVariable("typeProduit"));
        fact.setValeurCAF((Long) execution.getVariable("valeurCAF"));
        fact.setCodeSH((String) execution.getVariable("codeSH"));

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(fact);
            int rulesFired = session.fireAllRules();
            log.info("[Drools] {} règles déclenchées", rulesFired);
        } finally {
            session.dispose();
        }

        // Par défaut : VERT si rien n'a été décidé
        CircuitDouane circuit = fact.getCircuit() != null
                ? fact.getCircuit()
                : CircuitDouane.VERT;

        execution.setVariable("decisionCircuit", circuit.name());
        execution.setVariable("motifChoixCircuit", fact.getMotifChoix());

        log.info("[Drools] Circuit décidé: {} | Motif: {}",
                circuit, fact.getMotifChoix());
    }
}
