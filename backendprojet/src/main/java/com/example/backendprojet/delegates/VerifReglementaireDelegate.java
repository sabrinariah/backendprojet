package com.example.backendprojet.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("verifReglementaireDelegate")
public class VerifReglementaireDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        String paysDestination = (String) execution.getVariable("paysDestination");
        String typeProduit     = (String) execution.getVariable("typeProduit");

        Double valeur = 0.0;

        Object valeurObj = execution.getVariable("valeur");
        if (valeurObj != null) {
            valeur = Double.parseDouble(valeurObj.toString());
        }

        boolean conforme = true;
        String motifRefus = null;

        if (valeur > 10_000_000) {
            conforme = false;
            motifRefus = "Valeur dépasse le seuil autorisé (10M DZD)";
        }

        if (paysDestination != null && paysDestination.equalsIgnoreCase("Corée du Nord")) {
            conforme = false;
            motifRefus = "Pays sous embargo international";
        }

        if (typeProduit != null && typeProduit.toLowerCase().contains("militaire")) {
            conforme = false;
            motifRefus = "Produit soumis à autorisation spéciale";
        }

        execution.setVariable("exportAutorise", conforme);
        execution.setVariable("motifRefus", motifRefus);
        execution.setVariable("resultatReglementaire", conforme ? "CONFORME" : "REFUSÉ");

        System.out.println("✅ Vérification : " + (conforme ? "CONFORME" : "REFUSÉ"));
    }}