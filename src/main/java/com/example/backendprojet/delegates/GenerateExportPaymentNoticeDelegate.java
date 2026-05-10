package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.repository.DossierExportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component("generateExportPaymentNoticeDelegate")
@RequiredArgsConstructor
@Slf4j
public class GenerateExportPaymentNoticeDelegate implements JavaDelegate {

    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[EXPORT] GeneratePaymentNotice - dossier={}", dossierId);

        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND", "Dossier introuvable : " + dossierId));

        // Récupération du total calculé par CalculateExportDutiesDelegate
        BigDecimal totalAPayer = getBigDecimalVar(execution, "totalAPayer", BigDecimal.ZERO);

        // Génération référence avis de paiement
        String refAvis = "AEP-" + dossier.getReference() + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        dossier.setReferenceAvisPaiement(refAvis);
        dossier.setMontantTotalAPayer(totalAPayer);
        dossierRepo.save(dossier);

        // ✅ Variables disponibles pour Task_Paiement_Export (User Task)
        execution.setVariable("referenceAvisPaiement", refAvis);
        execution.setVariable("montantTotalAPayer",    totalAPayer);
        execution.setVariable("avisPaiementGenere",    true);

        // TODO : envoyer notification email/SMS à l'exportateur avec l'avis de paiement
        log.info("[EXPORT] Avis de paiement généré : ref={}, montant={} XAF", refAvis, totalAPayer);
    }

    private BigDecimal getBigDecimalVar(DelegateExecution exec, String key, BigDecimal defaultVal) {
        Object val = exec.getVariable(key);
        if (val == null) return defaultVal;
        try { return new BigDecimal(val.toString()); }
        catch (Exception e) { return defaultVal; }
    }
}