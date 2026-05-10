package com.example.backendprojet.entity;


import com.example.backendprojet.entity.TypeMarchandise;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "marchandises")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Marchandise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String designation;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String codeSH;       // Système harmonisé (ex: 0901.21.10)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMarchandise type;

    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal poidsKg;

    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valeurFob;   // Free On Board

    @Column(length = 3)
    private String devise;          // XAF, EUR, USD

    private String origine;         // Pays/région d'origine
    private String destination;     // Pays de destination

    private boolean dangereuse;
    private boolean perissable;
    private boolean necessiteCertificatPhytosanitaire;
}