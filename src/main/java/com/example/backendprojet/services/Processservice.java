package com.example.backendprojet.services;



import com.example.backendprojet.dto.ProcessusDTO;
import com.example.backendprojet.dto.TacheDTO;
import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.repository.ProcessusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class Processservice {

    private final ProcessusRepository processusRepository;

    public List<ProcessusDTO> findAll() {
        return processusRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public ProcessusDTO findById(Long id) {
        Processus p = processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus introuvable : " + id));
        return toDTO(p);
    }

    public ProcessusDTO create(ProcessusDTO dto) {
        Processus p = toEntity(dto);
        p.setId(null);
        return toDTO(processusRepository.save(p));
    }

    public ProcessusDTO update(Long id, ProcessusDTO dto) {
        Processus p = processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus introuvable : " + id));
        p.setNom(dto.getNom());
        p.setTypeProcessus(dto.getTypeProcessus());
        p.setDateDebut(dto.getDateDebut());
        p.setDateFin(dto.getDateFin());
        if (dto.getActive() != null) p.setActif(dto.getActive());
        return toDTO(processusRepository.save(p));
    }

    public void delete(Long id) {
        processusRepository.deleteById(id);
    }

    public ProcessusDTO toggleActive(Long id) {
        Processus p = processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus introuvable : " + id));
        p.setActif(!Boolean.TRUE.equals(p.getActif()));
        return toDTO(processusRepository.save(p));
    }

    // ---- Mappers ----
    private ProcessusDTO toDTO(Processus p) {
        return ProcessusDTO.builder()
                .id(p.getId())
                .nom(p.getNom())
                .typeProcessus(p.getTypeProcessus())
                .dateDebut(p.getDateDebut())
                .dateFin(p.getDateFin())
                .active(p.getActif())
                .taches(p.getTaches() == null ? List.of() :
                        p.getTaches().stream().map(t -> TacheDTO.builder()
                                .id(t.getId())
                                .nom(t.getNom())
                                .description(t.getDescription())
                                .assignee(t.getAssignee())
                                .processusId(p.getId())
                                .build()).collect(Collectors.toList()))
                .build();
    }

    private Processus toEntity(ProcessusDTO dto) {
        return Processus.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .typeProcessus(dto.getTypeProcessus())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .actif(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

}