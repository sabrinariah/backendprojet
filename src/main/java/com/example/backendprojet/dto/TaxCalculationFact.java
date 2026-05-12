package com.example.backendprojet.dto;



import lombok.Data;

/**
 * Fact Drools utilisé par CalculateDutiesAndTaxesDelegate.
 * Règles : tax-calculation-rules.drl
 */
@Data
public class TaxCalculationFact {

    // Inputs
    private Long valeurCAF;
    private String codeSH;
    private String paysOrigine;

    // Outputs
    private Long droitsDouane = 0L;
    private Long tva = 0L;
    private Long autresTaxes = 0L;
    private Long totalTaxes = 0L;

    /**
     * Méthode utilitaire appelée par les règles Drools pour
     * calculer le total des taxes.
     */
    public void calculerTotal() {
        this.totalTaxes = (droitsDouane != null ? droitsDouane : 0L)
                + (tva != null ? tva : 0L)
                + (autresTaxes != null ? autresTaxes : 0L);
    }
}