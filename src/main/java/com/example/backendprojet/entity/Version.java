package com.example.backendprojet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "versions")
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regle_id")
    @JsonIgnore
    private RegleMetier regleMetier;

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RegleMetier getRegleMetier() { return regleMetier; }
    public void setRegleMetier(RegleMetier regleMetier) { this.regleMetier = regleMetier; }
}