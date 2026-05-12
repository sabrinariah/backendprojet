package com.example.backendprojet.dto;

import com.example.backendprojet.entity.CircuitDouane;
import lombok.Data;

/**
 * Fact Drools utilisé par RiskAnalysisDelegate.
 * Règles : risk-analysis-rules.drl
 */
@Data
public class RiskAnalysisFact {

    // Inputs
    private String paysOrigine;
    private String typeProduit;
    private Long valeurCAF;
    private String codeSH;

    // Outputs
    private CircuitDouane circuit = CircuitDouane.VERT;
    private String motifChoix;
}