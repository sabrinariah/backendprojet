package com.example.backendprojet.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Processus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String typeProcessus;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean actif = true;
    private String bpmnProcessId; // ✅ AJOUT

    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }
    // Relation OneToMany avec Tache
    @OneToMany(mappedBy = "processus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tache> taches = new ArrayList<>();

    // 🔹 Relation Many-to-Many avec RegleMetier
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "processus_regle", // table de jointure
            joinColumns = @JoinColumn(name = "processus_id"),
            inverseJoinColumns = @JoinColumn(name = "regle_id")
    )
    private Set<RegleMetier> regles = new HashSet<>();

    public Processus() {}

    // ----------- Getters & Setters -----------

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTypeProcessus() {
        return typeProcessus;
    }

    public void setTypeProcessus(String typeProcessus) {
        this.typeProcessus = typeProcessus;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public List<Tache> getTaches() {
        return taches;
    }

    public void setTaches(List<Tache> taches) {
        this.taches = taches;
    }

    public Set<RegleMetier> getRegles() {
        return regles;
    }

    public void setRegles(Set<RegleMetier> regles) {
        this.regles = regles;
    }

    // 🔹 Méthodes utilitaires pour gérer la relation Many-to-Many
    public void addRegle(RegleMetier regle) {
        regles.add(regle);
        regle.getProcessus().add(this);
    }

    public void removeRegle(RegleMetier regle) {
        regles.remove(regle);
        regle.getProcessus().remove(this);
    }
}