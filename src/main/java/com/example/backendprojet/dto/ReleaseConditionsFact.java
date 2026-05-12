package com.example.backendprojet.dto;



import lombok.Data;

/**
 * Fact Drools utilisé par VerifyReleaseConditionsDelegate.
 * Règles : bae-conditions-rules.drl
 */
@Data
public class ReleaseConditionsFact {

    // Inputs
    private Boolean paiementConfirme;
    private Boolean marchandiseConforme;
    private Boolean controleConforme;
    private String decisionCircuit;

    // Outputs
    private boolean conditionsOK = true;
    private String motifBlocage;
}