package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.repository.DossierExportRepository;
import com.example.backendprojet.rules.DutiesResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("calculateExportDutiesDelegate")
@RequiredArgsConstructor
@Slf4j
public class CalculateExportDutiesDelegate implements JavaDelegate {

    private final KieContainer kieContainer;
    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[Drools] Calcul des taxes dossier={}", dossierId);

        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND", "Dossier introuvable : " + dossierId));

        DutiesResult result = new DutiesResult();
        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(dossier);
            if (dossier.getMarchandise() != null) session.insert(dossier.getMarchandise());
            session.insert(result);
            session.fireAllRules();
        } catch (Exception e) {
            log.error("[Drools] Erreur calcul taxes", e);
            throw new BpmnError("DROOLS_ERROR", e.getMessage());
        } finally {
            session.dispose();
        }

        // Mise à jour du dossier
        dossier.setMontantTaxes(result.getDroitsExport());
        dossier.setMontantRedevances(result.getRedevances().add(result.getTaxesParafiscales()));
        dossierRepo.save(dossier);

        // ✅ Variables alignées sur le BPMN (utilisées par GeneratePaymentNotice en aval)
        execution.setVariable("droitsExport",       result.getDroitsExport());
        execution.setVariable("redevances",          result.getRedevances());
        execution.setVariable("taxesParafiscales",   result.getTaxesParafiscales());
        execution.setVariable("totalAPayer",         result.getTotal());

        log.info("[Drools] Total à payer dossier={} : {} XAF (droits={}, redevances={}, parafiscales={})",
                dossierId, result.getTotal(), result.getDroitsExport(),
                result.getRedevances(), result.getTaxesParafiscales());
    }
}