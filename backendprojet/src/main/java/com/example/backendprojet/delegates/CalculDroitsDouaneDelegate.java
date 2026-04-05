package com.example.backendprojet.delegates;



import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("calculDroitsDouaneDelegate")
public class CalculDroitsDouaneDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        // ✅ Parser valeur sécurisé
        Double valeur = 0.0;
        Object valeurObj = execution.getVariable("valeur");
        if (valeurObj instanceof String) {
            try { valeur = Double.parseDouble((String) valeurObj); }
            catch (NumberFormatException e) { valeur = 0.0; }
        } else if (valeurObj instanceof Double)  { valeur = (Double) valeurObj; }
        else if (valeurObj instanceof Long)      { valeur = ((Long) valeurObj).doubleValue(); }
        else if (valeurObj instanceof Integer)   { valeur = ((Integer) valeurObj).doubleValue(); }

        String typeProduit = (String) execution.getVariable("typeProduit");

        // ── Taux selon type de produit ─────────────────────────────────
        double tauxDroits;
        if (typeProduit != null && typeProduit.toLowerCase().contains("alimentaire")) {
            tauxDroits = 0.05;   // 5% alimentaire
        } else if (typeProduit != null && typeProduit.toLowerCase().contains("électronique")) {
            tauxDroits = 0.10;   // 10% électronique
        } else if (typeProduit != null && typeProduit.toLowerCase().contains("luxe")) {
            tauxDroits = 0.30;   // 30% luxe
        } else {
            tauxDroits = 0.15;   // 15% par défaut
        }

        double droitsDouane  = valeur * tauxDroits;
        double tva           = valeur * 0.19;
        double fraisDouane   = 3000.0;
        double totalDroits   = droitsDouane + tva + fraisDouane;

        execution.setVariable("droitsDouane",  droitsDouane);
        execution.setVariable("tva",           tva);
        execution.setVariable("fraisDouane",   fraisDouane);
        execution.setVariable("totalDroits",   totalDroits);
        execution.setVariable("tauxDroits",    tauxDroits * 100 + "%");

        System.out.println("================================================");
        System.out.println("✅ CALCUL DROITS DE DOUANE");
        System.out.println("   Valeur marchandise : " + valeur + " DZD");
        System.out.println("   Taux droits        : " + (tauxDroits * 100) + "%");
        System.out.println("   Droits de douane   : " + droitsDouane + " DZD");
        System.out.println("   TVA (19%)          : " + tva + " DZD");
        System.out.println("   Frais douane       : " + fraisDouane + " DZD");
        System.out.println("   TOTAL À PAYER      : " + totalDroits + " DZD");
        System.out.println("================================================");
    }
}