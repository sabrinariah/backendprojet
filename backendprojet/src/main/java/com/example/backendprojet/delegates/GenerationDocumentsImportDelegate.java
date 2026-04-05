package com.example.backendprojet.delegates;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component("generationDocumentsImportDelegate")
public class GenerationDocumentsImportDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String importateur = (String) execution.getVariable("importateur");
        String paysOrigine = (String) execution.getVariable("paysOrigine");
        String typeProduit = (String) execution.getVariable("typeProduit");
        String codeSH      = (String) execution.getVariable("codeSH");

        String dateDoc = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long   ts      = System.currentTimeMillis();

        // ── Générer les références documents ──────────────────────────
        String declarationImport  = "DI-"  + dateDoc + "-" + ts;
        String bonDedouanement    = "BD-"  + dateDoc + "-" + ts;
        String certificatImport   = "CI-"  + dateDoc + "-" + ts;
        String licenceImport      = "LI-"  + dateDoc + "-" + ts;
        String attestationOrigine = "AO-"  + dateDoc + "-" + ts;

        execution.setVariable("declarationImport",  declarationImport);
        execution.setVariable("bonDedouanement",    bonDedouanement);
        execution.setVariable("certificatImport",   certificatImport);
        execution.setVariable("licenceImport",      licenceImport);
        execution.setVariable("attestationOrigine", attestationOrigine);
        execution.setVariable("dateGeneration",     dateDoc);
        execution.setVariable("statutDossier",      "DOCUMENTS_GENERES");

        System.out.println("================================================");
        System.out.println("✅ GÉNÉRATION DOCUMENTS IMPORT");
        System.out.println("   Importateur        : " + importateur);
        System.out.println("   Pays d'origine     : " + paysOrigine);
        System.out.println("   Type produit       : " + typeProduit);
        System.out.println("   Code SH            : " + codeSH);
        System.out.println("   Déclaration import : " + declarationImport);
        System.out.println("   Bon dédouanement   : " + bonDedouanement);
        System.out.println("   Certificat import  : " + certificatImport);
        System.out.println("   Licence import     : " + licenceImport);
        System.out.println("   Attestation origine: " + attestationOrigine);
        System.out.println("================================================");
    }
}