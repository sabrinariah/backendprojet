package com.example.backendprojet.rules;

import lombok.Data;

@Data
public class BoardingResult {

    // ✅ CORRECTION : null par défaut pour que R-BOARD-999 puisse se déclencher
    // La règle R-BOARD-999 teste : BoardingResult(conditionsOK == null)
    // Si on met false ici, R-BOARD-999 ne se déclenche JAMAIS
    private Boolean conditionsOK = null;

    private String motif;
}