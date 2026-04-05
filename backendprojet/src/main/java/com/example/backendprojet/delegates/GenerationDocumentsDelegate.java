package com.example.backendprojet.delegates;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("generationDocumentsDelegate")
public class GenerationDocumentsDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String exportateur = (String) execution.getVariable("exportateur");
        String paysDestination = (String) execution.getVariable("paysDestination");

        // Ici : générer PDF, appeler un service externe, etc.
        execution.setVariable("certificatOrigine", "CERT-" + System.currentTimeMillis());
        execution.setVariable("factureExport", "FACT-" + System.currentTimeMillis());
        execution.setVariable("documentDouanier", "DOUA-" + System.currentTimeMillis());

        System.out.println("✅ Documents générés pour " + exportateur + " → " + paysDestination);
    }
}