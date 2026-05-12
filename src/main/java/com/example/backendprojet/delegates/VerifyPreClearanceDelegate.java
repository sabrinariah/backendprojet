package com.example.backendprojet.delegates;



import com.example.backendprojet.dto.PreClearanceFact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

/**
 * Delegate BPMN : ${verifyPreClearanceDelegate}
 *
 * Vérifie la conformité des documents de pré-dédouanement
 * (visas techniques, certificats, domiciliation, licence).
 * Règles Drools : pre-clearance-rules.drl
 *
 * Variables IN  : visaTechnique, certificatConformite, certificatOrigine,
 *                 licenceImport, banqueDomiciliataire
 * Variables OUT : preClearanceConforme (boolean), motifNonConformite (string)
 */
@Slf4j
@Component("verifyPreClearanceDelegate")
@RequiredArgsConstructor
public class VerifyPreClearanceDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) {
        log.info("[Drools] Vérification pré-dédouanement - dossier: {}",
                execution.getVariable("dossierId"));

        PreClearanceFact fact = new PreClearanceFact();
        fact.setVisaTechnique((Boolean) execution.getVariable("visaTechnique"));
        fact.setCertificatConformite((Boolean) execution.getVariable("certificatConformite"));
        fact.setCertificatOrigine((Boolean) execution.getVariable("certificatOrigine"));
        fact.setLicenceImport((String) execution.getVariable("licenceImport"));
        fact.setBanqueDomiciliataire((String) execution.getVariable("banqueDomiciliataire"));

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(fact);
            int rulesFired = session.fireAllRules();
            log.info("[Drools] {} règles déclenchées", rulesFired);
        } finally {
            session.dispose();
        }

        execution.setVariable("preClearanceConforme", fact.isConforme());
        execution.setVariable("motifNonConformite", fact.getMotifNonConformite());

        log.info("[Drools] Pré-dédouanement conforme: {} | Motif: {}",
                fact.isConforme(), fact.getMotifNonConformite());
    }
}