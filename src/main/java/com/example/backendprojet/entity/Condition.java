package com.example.backendprojet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String champ;
    private String operateur;
    private String valeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regle_id")
    @JsonIgnore
    private RegleMetier regleMetier;

    public Condition() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChamp() { return champ; }
    public void setChamp(String champ) { this.champ = champ; }

    public String getOperateur() { return operateur; }
    public void setOperateur(String operateur) { this.operateur = operateur; }

    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }

    public RegleMetier getRegleMetier() { return regleMetier; }
    public void setRegleMetier(RegleMetier regleMetier) { this.regleMetier = regleMetier; }
}