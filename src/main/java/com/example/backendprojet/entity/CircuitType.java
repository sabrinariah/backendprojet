package com.example.backendprojet.entity;

public enum CircuitType {

    // ✅ CORRECTION : valeurs en français pour correspondre exactement
    // au BPMN : ${decisionCircuit == 'VERT'} / 'ORANGE' / 'ROUGE'
    VERT,     // ✅ au lieu de GREEN
    ORANGE,
    ROUGE      // Inspection physique (était RED)
}