package com.example.backendprojet.delegates;


import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Delegate BPMN : ${generatePaymentNoticeDelegate}
 *
 * Génère un avis de paiement après calcul des taxes.
 * Crée une référence unique et la date d'émission.
 *
 * Variables IN  : dossierId, totalTaxes
 * Variables OUT : referenceAvisPaiement, montantAPayer, dateEmissionAvis
 */
@Slf4j
@Component("generatePaymentNoticeDelegate")
public class GeneratePaymentNoticeDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");
        Long totalTaxes = (Long) execution.getVariable("totalTaxes");

        // Génération d'une référence unique
        String avisRef = "AVIS-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();

        String dateEmission = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        execution.setVariable("referenceAvisPaiement", avisRef);
        execution.setVariable("montantAPayer", totalTaxes);
        execution.setVariable("dateEmissionAvis", dateEmission);

        log.info("Avis de paiement généré - Dossier: {} | Réf: {} | Montant: {} FCFA",
                dossierId, avisRef, totalTaxes);
    }
}