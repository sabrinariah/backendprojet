package com.example.backendprojet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "exportateurs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Exportateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String numeroContribuable;

    @NotBlank
    @Column(nullable = false)
    private String raisonSociale;

    @Column(nullable = false)
    private boolean agree;       // Agréé par les autorités

    @Column(nullable = false)
    private boolean enRegle;     // À jour de ses obligations fiscales

    private String adresse;
    private String email;
    private String telephone;
    private String numeroAgrement;
}