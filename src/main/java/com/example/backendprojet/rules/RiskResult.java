package com.example.backendprojet.rules;

import com.example.backendprojet.entity.CircuitType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskResult {
    private CircuitType circuit = null;
    private int score = 0;
    private String motif = null;
}