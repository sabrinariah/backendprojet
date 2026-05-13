package com.example.backendprojet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TacheDTO {
    private Long id;
    private String nom;
    private String type;        // ✅ ajouté
    private String statut;      // ✅ ajouté
    private String description;
    private String assignee;
    private Integer ordre;      // ✅ ajouté
    private Long processusId;
}