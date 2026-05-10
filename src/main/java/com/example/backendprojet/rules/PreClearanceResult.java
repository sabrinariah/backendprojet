package com.example.backendprojet.rules;



import lombok.Data;

@Data
public class PreClearanceResult {

    // ✅ Utilisé par VerifyExportPreClearanceDelegate :
    //    Boolean.TRUE.equals(result.getConforme())
    //    → execution.setVariable("preClearanceExportConforme", conforme)
    //    → BPMN Gateway_PreClearance_Export : ${preClearanceExportConforme == true}
    private Boolean conforme ;

    // Motif en cas de non-conformité
    // → stocké dans dossier.motifNonConformitePreClearance
    private String motif;
}