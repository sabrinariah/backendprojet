package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.CircuitType;
import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.repository.DossierExportRepository;
import com.example.backendprojet.rules.RiskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("evaluateExportRiskDelegate")
@RequiredArgsConstructor
@Slf4j
public class EvaluateExportRiskDelegate implements JavaDelegate {

    private final KieContainer kieContainer;
    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {

        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[Drools] Analyse risque dossier={}", dossierId);

        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND", "Dossier introuvable : " + dossierId));

        // ✅ RiskResult initialisé proprement
        RiskResult result = new RiskResult();

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(dossier);

            if (dossier.getMarchandise() != null) {
                session.insert(dossier.getMarchandise());
                log.info("[Drools] Marchandise insérée : type={} dangereuse={} valeurFob={}",
                        dossier.getMarchandise().getType(),
                        dossier.getMarchandise().isDangereuse(),
                        dossier.getMarchandise().getValeurFob());
            }

            if (dossier.getExportateur() != null) {
                session.insert(dossier.getExportateur());
                log.info("[Drools] Exportateur inséré : agree={} enRegle={}",
                        dossier.getExportateur().isAgree(),
                        dossier.getExportateur().isEnRegle());
            }

            session.insert(result);

            int rulesFired = session.fireAllRules();
            log.info("[Drools] {} règle(s) déclenchée(s)", rulesFired);

        } catch (Exception e) {
            log.error("[Drools] Erreur analyse risque", e);
            throw new BpmnError("DROOLS_ERROR", e.getMessage());
        } finally {
            session.dispose();
        }

        // ✅ Fallback VERT si aucune règle finale n'a matché
        CircuitType circuit = result.getCircuit() != null ? result.getCircuit() : CircuitType.VERT;
        String motif = result.getMotif() != null ? result.getMotif() : "Circuit vert par défaut";

        log.info("[Drools] Circuit={} score={} dossier={}", circuit.name(), result.getScore(), dossierId);

        // ✅ circuit.name() → "VERT" / "ORANGE" / "ROUGE" — aligné avec DB et BPMN
        dossier.setCircuitDouane(circuit.name());
        dossierRepo.save(dossier);

        execution.setVariable("decisionCircuit", circuit.name());
        execution.setVariable("scoreRisque",     result.getScore());
        execution.setVariable("motifCircuit",    motif);
        execution.setVariable("controleExportConforme", circuit == CircuitType.VERT);
    }

}