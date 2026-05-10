package com.example.backendprojet.rules;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ConformiteResult {
    private Boolean conforme;
    private String motifNonConformite;
    @Builder.Default
    private List<String> documentsManquants = new ArrayList<>();

    public void ajouterDocumentManquant(String doc) {
        if (documentsManquants == null) documentsManquants = new ArrayList<>();
        documentsManquants.add(doc);
        conforme = false;
    }
}
