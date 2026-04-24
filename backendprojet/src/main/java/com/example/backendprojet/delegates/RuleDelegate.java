package com.example.backendprojet.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RuleDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        System.out.println("🔥 RULE DELEGATE EXECUTÉ");

        // 🔹 Récupération sécurisée des variables
        Integer quantite = getInteger(execution, "quantite");
        Integer valeur = getInteger(execution, "valeur");
        Integer codeSH = getInteger(execution, "codeSH");
        String destinationFinale = getString(execution, "destinationFinale");

        System.out.println("👉 Variables : ");
        System.out.println("quantite = " + quantite);
        System.out.println("valeur = " + valeur);
        System.out.println("codeSH = " + codeSH);
        System.out.println("destinationFinale = " + destinationFinale);

        // 🔹 Exemple de règles métier
        String decision = "OK";

        if (valeur != null && valeur > 1000000) {
            decision = "ALERTE_VALUER_ELEVEE";
        }

        if (quantite != null && quantite > 100) {
            decision = "ALERTE_QUANTITE";
        }

        if ("france".equalsIgnoreCase(destinationFinale)) {
            decision = "CONTROLE_OBLIGATOIRE";
        }

        // 🔹 Stockage résultat dans Camunda
        execution.setVariable("decisionRegle", decision);

        System.out.println("✅ DECISION = " + decision);
    }

    // ===============================
    // 🔧 Helpers SAFE (IMPORTANT)
    // ===============================

    private Integer getInteger(DelegateExecution execution, String key) {
        Object value = execution.getVariable(key);
        if (value == null) return null;

        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);

        return null;
    }

    private String getString(DelegateExecution execution, String key) {
        Object value = execution.getVariable(key);
        return value != null ? value.toString() : null;
    }
}