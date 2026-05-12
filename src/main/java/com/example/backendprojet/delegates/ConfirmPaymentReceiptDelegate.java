package com.example.backendprojet.delegates;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Delegate BPMN : ${confirmPaymentReceiptDelegate}
 *
 * Vérifie la concordance entre le montant payé et le montant attendu.
 * Confirme la validité de la quittance.
 *
 * Variables IN  : montantPaiement, totalTaxes, numeroQuittance, referencePaiement
 * Variables OUT : paiementConfirme (boolean), motifNonConfirmation (string)
 */
@Slf4j
@Component("confirmPaymentReceiptDelegate")
public class ConfirmPaymentReceiptDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Long montantPaye = (Long) execution.getVariable("montantPaiement");
        Long montantAttendu = (Long) execution.getVariable("totalTaxes");
        String numeroQuittance = (String) execution.getVariable("numeroQuittance");
        String referencePaiement = (String) execution.getVariable("referencePaiement");

        boolean confirme = true;
        String motif = null;

        // Vérification 1 : numéro de quittance fourni
        if (numeroQuittance == null || numeroQuittance.isBlank()) {
            confirme = false;
            motif = "Numéro de quittance manquant";
        }

        // Vérification 2 : référence paiement fournie
        else if (referencePaiement == null || referencePaiement.isBlank()) {
            confirme = false;
            motif = "Référence de paiement manquante";
        }

        // Vérification 3 : montant payé
        else if (montantPaye == null) {
            confirme = false;
            motif = "Montant de paiement non renseigné";
        }

        // Vérification 4 : montant suffisant
        else if (montantAttendu != null && montantPaye < montantAttendu) {
            confirme = false;
            motif = String.format("Montant insuffisant : payé %d, attendu %d",
                    montantPaye, montantAttendu);
        }

        execution.setVariable("paiementConfirme", confirme);
        execution.setVariable("motifNonConfirmation", motif);

        log.info("Confirmation paiement - Confirmé: {} | Quittance: {} | Motif: {}",
                confirme, numeroQuittance, motif);
    }
}
