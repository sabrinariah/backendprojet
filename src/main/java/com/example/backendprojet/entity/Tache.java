package com.example.backendprojet.entity;

import jakarta.persistence.*;

@Entity
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String type;
    private String statut;
    private String description;

    private String assignee;

    // 🔥 AJOUT DU CHAMP ORDRE
    private Integer ordre;

    @ManyToOne
    @JoinColumn(name = "processus_id")
    private Processus processus;

    public Tache() {}

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    // 🔥 GETTER / SETTER ORDRE
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public Processus getProcessus() { return processus; }
    public void setProcessus(Processus processus) { this.processus = processus; }
}