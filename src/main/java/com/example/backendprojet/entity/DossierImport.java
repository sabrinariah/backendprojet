package com.example.backendprojet.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité principale représentant un dossier d'importation.
 * Centralise toutes les données collectées au fil des 4 phases du processus.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dossier_import")
public class DossierImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dossierId;

    private String nomDossier;
    private String importateur;
    private String paysOrigine;
    private String typeProduit;
    private LocalDate dateDepot;

    // Référence Camunda
    private String processInstanceId;

    // ===== PHASE 1 : PRE-DEDOUANEMENT =====
    private Boolean importEligible;
    private String motifRejet;

    // Procédures préalables
    private String licenceImport;
    private String autoriteCompetente;
    private String referenceAutorisation;
    @Column(length = 1000)
    private String observationsPrealables;

    // Déclaration importation DI
    private String numeroDI;
    private String codeSH;
    private Long quantite;
    private Long valeurCAF;
    private String deviseFacture;
    private String origineMarchandise;

    // Domiciliation bancaire / assurance
    private String banqueDomiciliataire;
    private String numeroCompte;
    private String referenceAssurance;
    private Long montantAssure;
    private String devise;

    // Visas / Certificats
    private Boolean visaTechnique;
    private Boolean certificatConformite;
    private Boolean certificatOrigine;
    private String autresDocuments;
    private Boolean preClearanceConforme;

    // ===== PHASE 2 : PRISE EN CHARGE =====
    private String numeroManifeste;
    private String navire;
    private LocalDate dateArrivee;
    private String portArrivee;
    private String numeroConnaissement;

    // Déchargement / entrepôt
    private String numeroEntrepot;
    private LocalDate dateDechargement;
    private String emplacement;
    private String numeroConteneur;

    // Reconnaissance marchandise
    private String agentReconnaissance;
    private LocalDate dateReconnaissance;
    private Long poidsConstate;
    private Boolean anomaliesDetectees;
    @Column(length = 1000)
    private String observationsRecon;
    private Boolean marchandiseConforme;

    // Anomalie
    private String typeAnomalie;
    @Column(length = 1000)
    private String actionCorrective;
    @Column(length = 1000)
    private String rapportAnomalie;
    private Boolean anomalieResolue;

    // ===== PHASE 3 : DEDOUANEMENT =====
    // DAU
    private String numeroDeclaration;
    private String bureauDouane;
    private LocalDate dateDepotDouane;
    private String declarant;

    @Enumerated(EnumType.STRING)
    private CircuitDouane decisionCircuit;

    // Contrôle documentaire (ORANGE)
    private String agentDouaneDoc;
    private LocalDate dateControleDoc;
    @Column(length = 1000)
    private String observationsControle;

    // Inspection physique (ROUGE)
    private String agentDouanePhys;
    private LocalDate dateInspection;
    private String resultScanner;
    private Boolean anomaliesInspection;
    @Column(length = 1000)
    private String rapportInspection;
    private Boolean controleConforme;

    // Calcul taxes
    private Long droitsDouane;
    private Long tva;
    private Long autresTaxes;
    private Long totalTaxes;

    // Avis de paiement
    private String referenceAvisPaiement;
    private Long montantAPayer;

    // Paiement électronique
    private String referencePaiement;
    private String numeroQuittance;
    private Long montantPaiement;
    private String methodePaiement;
    private String banquePaiement;
    private LocalDate datePaiement;
    private Boolean paiementConfirme;

    // ===== PHASE 4 : ENLEVEMENT =====
    private Boolean conditionsBaeOK;

    // BAE
    private String numeroBAE;
    private String agentDouaneBAE;
    private LocalDate dateEmissionBAE;
    @Column(length = 1000)
    private String observationsBAE;

    // BAD
    private String numeroBAD;
    private String armateur;
    private LocalDate dateBAD;
    private String quaiLivraison;

    // Constat de sortie
    private String numeroConstatSortie;
    private LocalDate dateSortie;
    private String agentControleSortie;
    private Boolean conformiteSortie;
    @Column(length = 1000)
    private String observationsSortie;

    // ===== METADONNEES =====
    @Enumerated(EnumType.STRING)
    private StatutDossier statut = StatutDossier.EN_COURS;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateCloture;
    private String referenceArchivage;
    private String creePar;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutDossier.EN_COURS;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}