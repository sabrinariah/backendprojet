package com.example.backendprojet.dto;


import com.example.backendprojet.entity.CircuitType;
import com.example.backendprojet.entity.StatutDossier;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DossierResponse {
    private UUID id;
    private String reference;
    private StatutDossier statut;
    private String processInstanceId;

    private String exportateurRaisonSociale;
    private String exportateurNumero;

    private String designationMarchandise;
    private String codeSH;
    private BigDecimal valeurFob;
    private BigDecimal poidsKg;

    private Boolean exportEligible;
    private String motifRejet;
    private CircuitType circuitDouane;
    private BigDecimal montantTaxes;
    private BigDecimal montantRedevances;
    private boolean paiementConfirme;

    private String numeroDeclarationDouane;
    private String numeroBESC;
    private String numeroCertificatOrigine;
    private String numeroBonAEmbarquer;

    private LocalDateTime dateCreation;
    private LocalDateTime dateExportation;
    private LocalDateTime dateCloture;
}