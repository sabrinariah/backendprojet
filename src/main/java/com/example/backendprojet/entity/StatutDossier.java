package com.example.backendprojet.entity;

public enum StatutDossier {

    // ── Phase initiale ────────────────────────────────────────────────────────
    BROUILLON,
    SOUMIS,
    EN_COURS,                       // ✅ AJOUTÉ : statut générique "en cours de traitement"

    // ── Phase 1 : Pré-dédouanement ────────────────────────────────────────────
    EN_VERIFICATION_ELIGIBILITE,
    ELIGIBLE,
    REJETE,

    PROCEDURES_PREALABLES,
    DECLARATION_DE_F1,
    DOMICILIATION_BANCAIRE,
    PREPARATION_DOCUMENTS,

    EN_VERIFICATION_PRE_CLEARANCE,
    PRE_CLEARANCE_CONFORME,
    PRE_CLEARANCE_NON_CONFORME,

    // ── Phase 2 : Préparation / Expédition ────────────────────────────────────
    INSPECTION_EN_COURS,
    CERTIFICAT_PHYTOSANITAIRE,
    EMPOTAGE,
    CERTIFICAT_ORIGINE,
    BESC_BOOKING,

    EN_VERIFICATION_EXPEDITION,
    EXPEDITION_CONFORME,
    EXPEDITION_NON_CONFORME,

    // ── Phase 3 : Dédouanement ────────────────────────────────────────────────
    SOUMIS_DOUANE,

    EN_ANALYSE_RISQUE,
    CIRCUIT_VERT,
    CIRCUIT_ORANGE,
    CIRCUIT_ROUGE,

    CONTROLE_DOCUMENTAIRE,
    INSPECTION_PHYSIQUE,
    CONTROLE_CONFORME,
    CONTROLE_NON_CONFORME,

    CALCUL_TAXES,
    AVIS_PAIEMENT_GENERE,
    EN_ATTENTE_PAIEMENT,
    PAIEMENT_CONFIRME,
    PAIEMENT_NON_CONFIRME,

    // ── Phase 4 : Embarquement / Clôture ──────────────────────────────────────
    CONDITIONS_EMBARQUEMENT_OK,
    CONDITIONS_EMBARQUEMENT_KO,
    BON_A_EMBARQUER,
    AUTORISATION_EMBARQUEMENT,
    MISE_A_BORD,
    CONSTAT_EMBARQUEMENT,

    APURE,
    INCIDENT_APUREMENT,
    EXPORTE,

    // ── Statuts transverses ───────────────────────────────────────────────────
    SUSPENDU,
    ANNULE,
    CLOTURE
}