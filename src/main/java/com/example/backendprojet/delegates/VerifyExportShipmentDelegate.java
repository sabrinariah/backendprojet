package com.example.backendprojet.delegates;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.entity.Document;
import com.example.backendprojet.repository.DossierExportRepository;
import com.example.backendprojet.rules.ShipmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component("verifyExportShipmentDelegate")
@RequiredArgsConstructor
@Slf4j
public class VerifyExportShipmentDelegate implements JavaDelegate {

    private final KieContainer kieContainer;
    private final DossierExportRepository dossierRepo;

    @Override
    public void execute(DelegateExecution execution) {

        String dossierId = (String) execution.getVariable("dossierId");
        log.info("[Drools] Vérification expédition dossier={}", dossierId);

        // ============================
        // 🔥 CHARGEMENT DOSSIER
        // ============================
        DossierExport dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND", "Dossier introuvable : " + dossierId));

        // ============================
        // ✅ SYNCHRONISATION AUTOMATIQUE
        // Lire TOUTES les variables du processus Camunda
        // et mettre à jour le dossier en DB
        // ============================
        syncDossierFromProcessVariables(execution, dossier);

        // ============================
        // ✅ RAPPORT EMPOTAGE
        // Si numeroConteneur est renseigné → rapport empotage présent
        // ============================
        syncRapportEmpotage(execution, dossier);

        // Reset flags avant vérification
        dossier.setExpeditionConforme(null);
        dossier.setMotifNonConformiteExpedition(null);
        dossier.setDateDerniereModification(LocalDateTime.now());

        // Save + flush + reload
        dossierRepo.save(dossier);
        dossierRepo.flush();

        dossier = dossierRepo.findById(UUID.fromString(dossierId))
                .orElseThrow(() -> new BpmnError("DOSSIER_NOT_FOUND", "Dossier introuvable"));

        log.info("[Sync] numeroBESC='{}' certifOrigine='{}' certifPhyto='{}' nbDocs={}",
                dossier.getNumeroBESC(),
                dossier.getNumeroCertificatOrigine(),
                dossier.getNumeroCertificatPhytosanitaire(),
                dossier.getDocuments() != null ? dossier.getDocuments().size() : 0);

        // ============================
        // 🔥 DROOLS
        // ============================
        ShipmentResult result = new ShipmentResult();
        result.setConforme(null);
        result.setHasError(false);

        KieSession session = kieContainer.newKieSession();

        try {
            session.insert(dossier);

            if (dossier.getMarchandise() != null) {
                session.insert(dossier.getMarchandise());
                log.info("[Drools] Marchandise : phyto={} périssable={}",
                        dossier.getMarchandise().isNecessiteCertificatPhytosanitaire(),
                        dossier.getMarchandise().isPerissable());
            } else {
                log.warn("[Drools] Aucune marchandise liée");
            }

            if (dossier.getDocuments() != null && !dossier.getDocuments().isEmpty()) {
                dossier.getDocuments().forEach(doc -> {
                    session.insert(doc);
                    log.info("[Drools] Document : type={} nom={}", doc.getType(), doc.getNom());
                });
            } else {
                log.warn("[Drools] Aucun document lié");
            }

            session.insert(result);

            int rulesFired = session.fireAllRules();
            log.info("[Drools] {} règle(s) déclenchée(s)", rulesFired);

        } catch (Exception e) {
            log.error("[Drools] Erreur règles", e);
            throw new BpmnError("DROOLS_ERROR", e.getMessage());
        } finally {
            session.dispose();
        }

        // ============================
        // ✅ RÉSULTAT
        // ============================
        Boolean conforme = Boolean.TRUE.equals(result.getConforme());
        String motif = result.getMotif() != null ? result.getMotif() : "";

        log.info("[Drools] Résultat => conforme={} motif='{}'", conforme, motif);

        dossier.setExpeditionConforme(conforme);
        dossier.setMotifNonConformiteExpedition(conforme ? null : motif);
        dossierRepo.save(dossier);

        execution.setVariable("expeditionExportConforme", conforme);
        execution.setVariable("motifNonConformiteExpedition", motif);
    }

    // ============================
    // ✅ SYNCHRONISATION AUTOMATIQUE
    // Lit les variables Camunda des tâches précédentes
    // et met à jour le dossier sans dépendre du formulaire correction
    // ============================
    private void syncDossierFromProcessVariables(DelegateExecution execution, DossierExport dossier) {

        // ✅ numeroBESC — saisi dans Task_BESC_Booking
        String numeroBESC = getStringVar(execution, "numeroBESC");
        if (numeroBESC != null && !numeroBESC.isBlank()) {
            dossier.setNumeroBESC(numeroBESC);
            log.info("[Sync] numeroBESC='{}'", numeroBESC);
        }

        // ✅ numeroCertificatOrigine — saisi dans Task_Certificat_Origine
        String certifOrigine = getStringVar(execution, "numeroCertificatOrigine");
        if (certifOrigine != null && !certifOrigine.isBlank()) {
            dossier.setNumeroCertificatOrigine(certifOrigine);
            log.info("[Sync] numeroCertificatOrigine='{}'", certifOrigine);
        }

        // ✅ numeroCertificatPhytosanitaire — saisi dans Task_Certificat_Phytosanitaire
        String certifPhyto = getStringVar(execution, "numeroCertificatPhyto");
        if (certifPhyto != null && !certifPhyto.isBlank()) {
            dossier.setNumeroCertificatPhytosanitaire(certifPhyto);
            log.info("[Sync] numeroCertificatPhytosanitaire='{}'", certifPhyto);
        }

        // ✅ numeroDeclarationDouane — saisi dans Task_Soumission_Douane_Export
        String numDeclaration = getStringVar(execution, "numeroDeclaration");
        if (numDeclaration != null && !numDeclaration.isBlank()) {
            dossier.setNumeroDeclarationDouane(numDeclaration);
            log.info("[Sync] numeroDeclarationDouane='{}'", numDeclaration);
        }
    }

    // ============================
    // ✅ RAPPORT EMPOTAGE AUTOMATIQUE
    // Si numeroConteneur saisi dans Task_Empotage_Rapport → document ajouté
    private void syncRapportEmpotage(DelegateExecution execution, DossierExport dossier) {

        String numeroConteneur = getStringVar(execution, "numeroConteneur");
        log.info("[Sync] numeroConteneur='{}'", numeroConteneur);

        if (numeroConteneur != null && !numeroConteneur.isBlank()) {
            boolean dejaPresent = dossier.getDocuments() != null &&
                    dossier.getDocuments().stream()
                            .anyMatch(d -> "RAPPORT_EMPOTAGE".equals(d.getType()));

            if (!dejaPresent) {
                Document rapport = Document.builder()
                        .nom("rapport_empotage_" + numeroConteneur + ".pdf")   // ✅ NOT NULL
                        .type("RAPPORT_EMPOTAGE")                               // ✅ NOT NULL
                        .cheminFichier("virtual/rapport_empotage_"             // ✅ NOT NULL
                                + numeroConteneur + ".pdf")
                        .contentType("application/pdf")
                        .taille(0L)
                        .dateUpload(LocalDateTime.now())                        // ✅ NOT NULL
                        .uploadePar("sync_automatique")
                        .dossier(dossier)
                        .build();

                dossier.getDocuments().add(rapport);
                log.info("[Sync] Rapport empotage ajouté pour conteneur={}", numeroConteneur);
            } else {
                log.info("[Sync] Rapport empotage déjà présent");
            }
        } else {
            log.warn("[Sync] numeroConteneur absent — rapport empotage non ajouté");
        }
    }
    // ============================
    // Helpers
    // ============================
    private String getStringVar(DelegateExecution execution, String name) {
        Object val = execution.getVariable(name);
        return val != null ? val.toString().trim() : null;
    }
}