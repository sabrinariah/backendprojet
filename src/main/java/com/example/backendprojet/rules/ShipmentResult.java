package com.example.backendprojet.rules;


import lombok.Data;

@Data
public class ShipmentResult {

    // ✅ Utilisé par VerifyExportShipmentDelegate :
    //    Boolean.TRUE.equals(result.getConforme())
    //    → execution.setVariable("expeditionExportConforme", conforme)
    //    → BPMN Gateway_Expedition_Export : ${expeditionExportConforme == true}
    private Boolean conforme = false;

    // Motif en cas de non-conformité
    // → stocké dans dossier.motifNonConformiteExpedition
    private String motif;
    private boolean hasError = false;
}