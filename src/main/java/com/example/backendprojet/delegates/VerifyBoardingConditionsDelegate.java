package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.repository.DossierExportRepository;
import com.example.backendprojet.rules.BoardingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("verifyBoardingConditionsDelegate")
@RequiredArgsConstructor
@Slf4j
public class VerifyBoardingConditionsDelegate implements JavaDelegate {

    private final KieContainer kieContainer;
    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[Drools] Vérification conditions d'embarquement dossier={}", dossierId);

        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND",
                        "Dossier introuvable : " + dossierId));

        // ✅ Log diagnostic pour voir l'état des données en base
        log.info("[Drools] État dossier — paiementConfirme={} numeroBonAEmbarquer='{}' numeroBESC='{}'",
                dossier.getPaiementConfirme(),
                dossier.getNumeroBonAEmbarquer(),
                dossier.getNumeroBESC());

        // ✅ BoardingResult initialisé à null (pas false) pour que R-BOARD-999 fonctionne
        BoardingResult result = new BoardingResult();

        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(dossier);
            if (dossier.getMarchandise() != null) {
                session.insert(dossier.getMarchandise());
            }
            session.insert(result);
            int nbRegles = session.fireAllRules();
            log.info("[Drools] {} règle(s) déclenchée(s) — conditionsOK={} motif='{}'",
                    nbRegles, result.getConditionsOK(), result.getMotif());
        } catch (Exception e) {
            log.error("[Drools] Erreur vérification embarquement", e);
            throw new BpmnError("DROOLS_ERROR", e.getMessage());
        } finally {
            session.dispose();
        }

        boolean conditionsOK = Boolean.TRUE.equals(result.getConditionsOK());

        dossier.setConditionsEmbarquementOK(conditionsOK);
        dossier.setMotifNonConformiteEmbarquement(result.getMotif() != null ? result.getMotif() : "");
        dossierRepo.save(dossier);

        // ✅ Variable alignée sur Gateway_Boarding_Conditions : ${conditionsEmbarquementOK == true}
        execution.setVariable("conditionsEmbarquementOK", conditionsOK);
        execution.setVariable("motifNonConformiteEmbarquement",
                result.getMotif() != null ? result.getMotif() : "");

        log.info("[Drools] Conditions embarquement OK={} pour dossier={}", conditionsOK, dossierId);
    }
}