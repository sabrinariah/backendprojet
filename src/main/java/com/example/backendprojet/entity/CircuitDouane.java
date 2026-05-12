package com.example.backendprojet.entity;

/**
 * Circuit douanier déterminé par l'analyse de risque (Drools).
 */
public enum CircuitDouane {
    VERT,    // Mainlevée immédiate (risque faible)
    ORANGE,  // Contrôle documentaire (risque moyen)
    ROUGE    // Inspection physique (risque élevé)
}
