package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.repository.DossierExportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("confirmExportPaymentDelegate")
@RequiredArgsConstructor
@Slf4j
public class ConfirmExportPaymentDelegate implements JavaDelegate {

    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {
        String dossierId = (String) execution.getVariable("dossierId");

        // ✅ Lecture de toutes les variables possibles soumises par Task_Paiement_Export
        String  referencePaiement  = (String)  execution.getVariable("referencePaiement");
        String  numeroQuittance    = (String)  execution.getVariable("numeroQuittance");
        Boolean paiementFormulaire = (Boolean) execution.getVariable("paiementExportConfirme");

        log.info("[EXPORT] ConfirmPayment - dossier={}, referencePaiement={}, quittance={}, formConfirme={}",
                dossierId, referencePaiement, numeroQuittance, paiementFormulaire);

        if (dossierId == null || dossierId.isBlank()) {
            throw new BpmnError("DOSSIER_ID_MANQUANT", "La variable dossierId est absente du contexte BPMN");
        }

        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND",
                        "Dossier introuvable : " + dossierId));

        // ✅ Résolution de la référence de paiement :
        //    priorité 1 : referencePaiement (champ principal du formulaire)
        //    priorité 2 : numeroQuittance   (champ alternatif)
        //    priorité 3 : referenceAvisPaiement déjà en base (fallback)
        String refFinale = referencePaiement;
        if (refFinale == null || refFinale.isBlank()) {
            refFinale = numeroQuittance;
        }
        if (refFinale == null || refFinale.isBlank()) {
            refFinale = dossier.getReferenceAvisPaiement();
            log.warn("[EXPORT] Aucune référence dans les variables BPMN, " +
                    "fallback sur dossier.referenceAvisPaiement={}", refFinale);
        }

        // ✅ Paiement confirmé si :
        //    - une référence/quittance non vide est présente, OU
        //    - l'opérateur a coché manuellement "paiementExportConfirme = true" dans le formulaire
        boolean paiementConfirme =
                (refFinale != null && !refFinale.isBlank())
                        || Boolean.TRUE.equals(paiementFormulaire);

        // ✅ Persister sur le dossier
        dossier.setPaiementConfirme(paiementConfirme);
        if (refFinale != null && !refFinale.isBlank()) {
            dossier.setReferenceAvisPaiement(refFinale);
        }
        dossierRepo.save(dossier);

        // ✅ Propagation vers la gateway Gateway_Payment_Export
        //    BPMN : ${paiementExportConfirme == true}  → Flow_022_Yes
        //           default                            → Flow_022_No
        execution.setVariable("paiementExportConfirme", paiementConfirme);

        log.info("[EXPORT] Paiement {} pour dossier {} (ref={})",
                paiementConfirme ? "CONFIRMÉ ✅" : "NON CONFIRMÉ ❌",
                dossier.getReference(), refFinale);

        // ✅ NE PAS lancer BpmnError ici : la gateway gère le routage vers
        //    Task_Regulariser_Paiement_Export si paiementConfirme == false.
        //    Un BpmnError provoquerait un incident Camunda au lieu d'un retry propre.
    }
}