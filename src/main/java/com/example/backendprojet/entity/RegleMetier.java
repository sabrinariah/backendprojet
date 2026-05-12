package com.example.backendprojet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regles_metier")
public class RegleMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String nom;
    private String action;
    private boolean active;

    @Column(name = "version_num")
    private Integer version = 1;

    // ================= CATEGORIE =================
    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    // ================= CONDITIONS =================
    @OneToMany(
            mappedBy = "regleMetier",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Condition> conditions = new ArrayList<>();

    // ================= VERSIONS (historique) =================
    @OneToMany(
            mappedBy = "regleMetier",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Version> versions = new ArrayList<>();

    public RegleMetier() {}

    public RegleMetier(String code, String nom, String action, boolean active, Categorie categorie) {
        this.code = code;
        this.nom = nom;
        this.action = action;
        this.active = active;
        this.categorie = categorie;
    }

    // ================= GETTERS / SETTERS =================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public List<Condition> getConditions() { return conditions; }
    public void setConditions(List<Condition> conditions) {
        this.conditions.clear();
        if (conditions != null) {
            for (Condition c : conditions) {
                c.setRegleMetier(this);
                this.conditions.add(c);
            }
        }
    }

    public List<Version> getVersions() { return versions; }
    public void setVersions(List<Version> versions) { this.versions = versions; }
}