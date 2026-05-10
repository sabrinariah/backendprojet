package com.example.backendprojet.dto;

import lombok.Data;

@Data
class ConditionDto {
    private Long id;
    private String champ;
    private String operateur;
    private String type;
    private String valeur;
}