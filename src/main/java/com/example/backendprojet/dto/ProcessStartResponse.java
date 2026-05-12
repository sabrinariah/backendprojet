package com.example.backendprojet.dto;

import com.example.backendprojet.entity.DossierImport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO retourné après le démarrage d'un processus d'import.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStartResponse {
    private DossierImport dossier;
    private String processInstanceId;
    private String message;
}
