package com.example.backendprojet.dto;

import lombok.Data;

/**
 * Fact Drools utilisé par VerifyPreClearanceDelegate.
 * Règles : pre-clearance-rules.drl
 */
@Data
public class PreClearanceFact {

    // Inputs
    private Boolean visaTechnique;
    private Boolean certificatConformite;
    private Boolean certificatOrigine;
    private String licenceImport;
    private String banqueDomiciliataire;

    // Outputs
    private boolean conforme = true;
    private String motifNonConformite;
}
