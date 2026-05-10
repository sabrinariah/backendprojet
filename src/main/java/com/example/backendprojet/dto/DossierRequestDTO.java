package com.example.backendprojet.dto;


import com.example.backendprojet.entity.TypeMarchandise;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DossierRequestDTO {

    @NotNull
    private UUID exportateurId;

    @NotBlank
    private String designationMarchandise;

    @NotBlank
    @Pattern(regexp = "^\\d{2,10}(\\.\\d{2,4})?$", message = "Code SH invalide")
    private String codeSH;

    @NotNull
    private TypeMarchandise typeMarchandise;

    @NotNull @Positive
    private BigDecimal poidsKg;

    @NotNull @Positive
    private BigDecimal valeurFob;

    @NotBlank
    private String devise;

    private String origine;
    private String destination;

    private boolean dangereuse;
    private boolean perissable;
    private boolean necessiteCertificatPhytosanitaire;
}
