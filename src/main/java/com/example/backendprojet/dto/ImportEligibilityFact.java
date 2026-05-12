package com.example.backendprojet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fact Drools utilisé par EvaluateImportEligibilityDelegate.
 * Règles : eligibility-rules.drl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportEligibilityFact {

    // Inputs
    private String dossierId;
    private String importateur;
    private String paysOrigine;
    private String typeProduit;

    // Outputs (modifiés par les règles)
    private boolean eligible = true;
    private String motifRejet;
}