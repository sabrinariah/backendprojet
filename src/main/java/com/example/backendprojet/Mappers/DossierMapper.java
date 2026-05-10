package com.example.backendprojet.Mappers;

import com.example.backendprojet.dto.DossierResponse;
import com.example.backendprojet.entity.CircuitType;
import com.example.backendprojet.entity.DossierExport;
import org.springframework.stereotype.Component;

@Component
public class DossierMapper {

    public DossierResponse toDTO(DossierExport entity) {
        if (entity == null) return null;

        return DossierResponse.builder()
                .id(entity.getId())
                .reference(entity.getReference())
                .statut(entity.getStatut())
                .processInstanceId(entity.getProcessInstanceId())
                .exportateurRaisonSociale(entity.getExportateur() != null
                        ? entity.getExportateur().getRaisonSociale() : null)
                .exportateurNumero(entity.getExportateur() != null
                        ? entity.getExportateur().getNumeroContribuable() : null)
                .designationMarchandise(entity.getMarchandise() != null
                        ? entity.getMarchandise().getDesignation() : null)
                .codeSH(entity.getMarchandise() != null
                        ? entity.getMarchandise().getCodeSH() : null)
                .valeurFob(entity.getMarchandise() != null
                        ? entity.getMarchandise().getValeurFob() : null)
                .poidsKg(entity.getMarchandise() != null
                        ? entity.getMarchandise().getPoidsKg() : null)
                .exportEligible(entity.getExportEligible())
                .motifRejet(entity.getMotifRejet())
                // ✅ CORRECTION : circuitDouane est String dans l'entité → assignation directe
                .circuitDouane(
                        entity.getCircuitDouane() != null
                                ? CircuitType.valueOf(entity.getCircuitDouane())
                                : null
                )                .montantTaxes(entity.getMontantTaxes())
                .montantRedevances(entity.getMontantRedevances())
                // ✅ CORRECTION : getPaiementConfirme() (Boolean objet) au lieu de isPaiementConfirme()
                //    isPaiementConfirme() attend un boolean primitif → NPE si champ null
                .paiementConfirme(entity.getPaiementConfirme())
                .numeroDeclarationDouane(entity.getNumeroDeclarationDouane())
                .numeroBESC(entity.getNumeroBESC())
                .numeroCertificatOrigine(entity.getNumeroCertificatOrigine())
                .numeroBonAEmbarquer(entity.getNumeroBonAEmbarquer())
                .dateCreation(entity.getDateCreation())
                .dateExportation(entity.getDateExportation())
                .dateCloture(entity.getDateCloture())
                .build();
    }
}