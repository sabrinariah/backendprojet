package com.example.backendprojet.dto;



import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessusDTO {
    private Long id;
    private String nom;
    private String typeProcessus;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean active;
    private List<TacheDTO> taches;

    public Boolean getActive() { return active; }
    public void setActive(Boolean actif) { this.active = actif; }
}