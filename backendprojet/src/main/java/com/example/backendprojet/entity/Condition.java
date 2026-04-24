package com.example.backendprojet.entity;



import jakarta.persistence.*;

@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Champ évalué : ex "paysOrigine", "typeProduit"
    private String champ;

    // Opérateur : toujours "==" (égale)
    private String operateur = "==";

    // Valeur à comparer : ex "FRANCE", "ALIMENTAIRE"
    private String valeur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regle_id")
    private RegleMetier regle;

    // ── Constructeurs ─────────────────────────────────────────────────────
    public Condition() {}

    public Condition(String champ, String valeur, RegleMetier regle) {
        this.champ     = champ;
        this.operateur = "==";
        this.valeur    = valeur;
        this.regle     = regle;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public Long getId()                       { return id; }
    public void setId(Long id)                { this.id = id; }

    public String getChamp()                  { return champ; }
    public void setChamp(String champ)        { this.champ = champ; }

    public String getOperateur()              { return operateur; }
    public void setOperateur(String op)       { this.operateur = op; }

    public String getValeur()                 { return valeur; }
    public void setValeur(String valeur)      { this.valeur = valeur; }

    public RegleMetier getRegle()             { return regle; }
    public void setRegle(RegleMetier regle)   { this.regle = regle; }
}