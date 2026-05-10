package com.example.backendprojet.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exemple : "Produits alimentaires", "Transport", etc.
    private String nom;

    // 🔥 IMPORTANT POUR TON BUG IMPORT / EXPORT
    private String type; // IMPORT ou EXPORT

    private String description;

    // ── Constructeurs ─────────────────────────────────────────────
    public Categorie() {}

    public Categorie(String nom, String type, String description) {
        this.nom = nom;
        this.type = type;
        this.description = description;
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}