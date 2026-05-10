package com.example.backendprojet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dossiers_export")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DossierExport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDossier statut;

    // ── Lien Camunda ─────────────────────────────────────────────────────────
    private String processInstanceId;
    private String businessKey;

    // ── Acteurs ───────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exportateur_id", nullable = false)
    private Exportateur exportateur;

    private String transitaireId;
    private String creePar;
    private String banqueDomiciliation;

    // ── Marchandise ───────────────────────────────────────────────────────────
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "marchandise_id")
    private Marchandise marchandise;

    // ── Documents ─────────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    // ── Phase 1 : Éligibilité ─────────────────────────────────────────────────
    private Boolean exportEligible;
    private String  motifRejet;

    // ── Phase 1 : Pré-dédouanement ────────────────────────────────────────────
    private Boolean preClearanceConforme;
    private String  motifNonConformitePreClearance;

    // ── Phase 2 : Expédition ──────────────────────────────────────────────────
    private Boolean expeditionConforme;
    private String  motifNonConformiteExpedition;

    // ── Phase 3 : Circuit douanier ────────────────────────────────────────────
    // ✅ CORRECTION : champ renommé "circuitDouane" (alias de "circuitDouanier")
    //    → Lombok génère getCircuitDouane() / setCircuitDouane()
    //    → compatibilité avec tout code qui appelait getCircuitDouane()
    @Column(name = "circuit_douane")
    private String circuitDouane;

    // ── Phase 3 : Contrôle ────────────────────────────────────────────────────
    private Boolean controleConforme;

    // ── Phase 3 : Taxes ───────────────────────────────────────────────────────
    @Column(precision = 15, scale = 2)
    private BigDecimal montantTaxes;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantRedevances;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantTotalAPayer;

    private Boolean paiementConfirme;
    private String  referenceAvisPaiement;

    // ── Phase 4 : Embarquement ────────────────────────────────────────────────
    private Boolean conditionsEmbarquementOK;
    private String  motifNonConformiteEmbarquement;
    private LocalDateTime dateApurement;

    // ── Numéros officiels ─────────────────────────────────────────────────────
    private String numeroDeclarationDouane;
    private String numeroBESC;
    private String numeroCertificatOrigine;
    private String numeroCertificatPhytosanitaire;
    private String numeroBonAEmbarquer;

    // ── Dates ─────────────────────────────────────────────────────────────────
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereModification;
    private LocalDateTime dateExportation;
    private LocalDateTime dateCloture;

    // ── Hooks JPA ─────────────────────────────────────────────────────────────
    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut      == null) statut        = StatutDossier.BROUILLON;
    }

    @PreUpdate
    public void preUpdate() {
        dateDerniereModification = LocalDateTime.now();
    }
}