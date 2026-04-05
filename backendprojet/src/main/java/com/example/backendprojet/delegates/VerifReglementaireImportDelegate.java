package com.example.backendprojet.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("verifReglementaireImportDelegate")
public class VerifReglementaireImportDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        // ── Récupération des variables ─────────────────────────────────
        String paysOrigine = (String) execution.getVariable("paysOrigine");
        String typeProduit = (String) execution.getVariable("typeProduit");
        String importateur = (String) execution.getVariable("importateur");

        // ✅ Parser valeur de façon sécurisée (stockée comme String dans BPMN)
        Double valeur = 0.0;
        Object valeurObj = execution.getVariable("valeur");
        if (valeurObj != null) {
            if (valeurObj instanceof String) {
                try {
                    valeur = Double.parseDouble((String) valeurObj);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Valeur non parseable : " + valeurObj);
                    valeur = 0.0;
                }
            } else if (valeurObj instanceof Double) {
                valeur = (Double) valeurObj;
            } else if (valeurObj instanceof Long) {
                valeur = ((Long) valeurObj).doubleValue();
            } else if (valeurObj instanceof Integer) {
                valeur = ((Integer) valeurObj).doubleValue();
            }
        }

        // ── Initialisation ─────────────────────────────────────────────
        boolean autorise   = true;
        String  motifRefus = "";

        // ── Règle 1 : Pays sous embargo ────────────────────────────────
        if (paysOrigine != null) {
            String pays = paysOrigine.trim().toLowerCase();
            if (pays.equals("corée du nord")
                    || pays.equals("iran")
                    || pays.equals("syrie")
                    || pays.equals("cuba")) {
                autorise   = false;
                motifRefus = "Pays d'origine sous embargo international : " + paysOrigine;
            }
        }

        // ── Règle 2 : Valeur dépasse le seuil import ───────────────────
        if (autorise && valeur > 50_000_000) {
            autorise   = false;
            motifRefus = "Valeur dépasse le seuil autorisé à l'import (50M DZD) : " + valeur + " DZD";
        }

        // ── Règle 3 : Produit interdit à l'importation ─────────────────
        if (autorise && typeProduit != null) {
            String produit = typeProduit.trim().toLowerCase();
            if (produit.contains("armes")
                    || produit.contains("munitions")
                    || produit.contains("explosifs")
                    || produit.contains("militaire")
                    || produit.contains("stupéfiants")
                    || produit.contains("drogue")) {
                autorise   = false;
                motifRefus = "Produit interdit à l'importation : " + typeProduit;
            }
        }

        // ── Règle 4 : Importateur non renseigné ────────────────────────
        if (autorise && (importateur == null || importateur.trim().isEmpty())) {
            autorise   = false;
            motifRefus = "Importateur non renseigné";
        }

        // ── Règle 5 : Pays d'origine non renseigné ─────────────────────
        if (autorise && (paysOrigine == null || paysOrigine.trim().isEmpty())) {
            autorise   = false;
            motifRefus = "Pays d'origine non renseigné";
        }

        // ── Calcul du niveau de risque ─────────────────────────────────
        String niveauRisque;
        if (!autorise) {
            niveauRisque = "BLOQUÉ";
        } else if (valeur > 10_000_000) {
            niveauRisque = "ÉLEVÉ";
        } else if (valeur > 1_000_000) {
            niveauRisque = "MOYEN";
        } else {
            niveauRisque = "FAIBLE";
        }

        // ── Stocker les résultats dans les variables du processus ───────
        execution.setVariable("importAutorise",          autorise);
        execution.setVariable("motifRefusImport",        motifRefus);
        execution.setVariable("resultatReglementaire",   autorise ? "CONFORME" : "NON_CONFORME");
        execution.setVariable("niveauRisqueImport",      niveauRisque);
        execution.setVariable("valeurParsee",            valeur);

        // ── Log console ────────────────────────────────────────────────
        System.out.println("================================================");
        System.out.println("✅ VÉRIFICATION RÉGLEMENTAIRE IMPORT");
        System.out.println("   Importateur  : " + importateur);
        System.out.println("   Pays origine : " + paysOrigine);
        System.out.println("   Type produit : " + typeProduit);
        System.out.println("   Valeur       : " + valeur + " DZD");
        System.out.println("   Résultat     : " + (autorise ? "✅ AUTORISÉ" : "❌ REFUSÉ"));
        System.out.println("   Niveau risque: " + niveauRisque);
        if (!autorise) {
            System.out.println("   Motif refus  : " + motifRefus);
        }
        System.out.println("================================================");
    }
}