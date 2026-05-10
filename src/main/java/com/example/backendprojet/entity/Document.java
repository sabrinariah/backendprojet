package com.example.backendprojet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String type;     // FACTURE, PACKING_LIST, CERTIFICAT_ORIGINE, BESC, etc.

    @Column(nullable = false)
    private String cheminFichier;

    private String contentType;
    private Long taille;

    @Column(nullable = false)
    private LocalDateTime dateUpload;

    private String uploadePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id")
    private DossierExport dossier;
}
