package com.example.backendprojet.entity;



import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class RegleMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;        // Nom de la règle
    private String code;       // Code unique
    private String action;     // Action liée à la règle
    private Boolean actif = true; // Indique si la règle est active

    // 🔹 Relation Many-to-Many inverse avec Processus
    @ManyToMany(mappedBy = "regles")
    private Set<Processus> processus = new HashSet<>();

    public RegleMetier() {}

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Set<Processus> getProcessus() {
        return processus;
    }

    public void setProcessus(Set<Processus> processus) {
        this.processus = processus;
    }

    // 🔹 Méthodes utilitaires pour synchroniser la relation
    public void addProcessus(Processus p) {
        processus.add(p);
        p.getRegles().add(this);
    }

    public void removeProcessus(Processus p) {
        processus.remove(p);
        p.getRegles().remove(this);
    }
}