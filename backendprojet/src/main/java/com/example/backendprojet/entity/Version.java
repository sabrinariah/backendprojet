package com.example.backendprojet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroVersion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    private String description;

    // ================= RELATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regle_metier_id", nullable = false)
    @JsonIgnore // 🔥 évite boucle JSON
    private RegleMetier regleMetier;

    public Version() {}

    public Version(String numeroVersion, Date dateCreation, String description, RegleMetier regleMetier) {
        this.numeroVersion = numeroVersion;
        this.dateCreation = dateCreation;
        this.description = description;
        this.regleMetier = regleMetier;
    }

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroVersion() { return numeroVersion; }
    public void setNumeroVersion(String numeroVersion) { this.numeroVersion = numeroVersion; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RegleMetier getRegleMetier() { return regleMetier; }
    public void setRegleMetier(RegleMetier regleMetier) { this.regleMetier = regleMetier; }
}