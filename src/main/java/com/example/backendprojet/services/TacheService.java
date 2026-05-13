package com.example.backendprojet.services;

import com.example.backendprojet.dto.TacheDTO;
import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.entity.Tache;
import com.example.backendprojet.repository.ProcessusRepository;
import com.example.backendprojet.repository.TacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TacheService {

    private final TacheRepository tacheRepo;
    private final ProcessusRepository processusRepo;
    public List<TacheDTO> findAll() {
        return tacheRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TacheDTO> findByProcessus(Long processusId) {
        return tacheRepo.findByProcessusId(processusId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TacheDTO findById(Long id) {
        return tacheRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable avec l'id : " + id));
    }

    @Transactional
    public TacheDTO create(TacheDTO dto) {
        // Guard: reject missing or zero processusId before hitting the DB
        if (dto.getProcessusId() == null || dto.getProcessusId() == 0) {
            throw new IllegalArgumentException(
                    "processusId est requis pour créer une tâche (reçu : " + dto.getProcessusId() + ")"
            );
        }

        Processus processus = processusRepo.findById(dto.getProcessusId())
                .orElseThrow(() -> new RuntimeException(
                        "Processus introuvable avec l'id : " + dto.getProcessusId()
                ));

        Tache tache = toEntity(dto, processus);
        return toDTO(tacheRepo.save(tache));
    }

    @Transactional
    public TacheDTO update(Long id, TacheDTO dto) {
        Tache tache = tacheRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable avec l'id : " + id));

        tache.setNom(dto.getNom());
        tache.setType(dto.getType());
        tache.setStatut(dto.getStatut());
        tache.setDescription(dto.getDescription());
        tache.setAssignee(dto.getAssignee());
        tache.setOrdre(dto.getOrdre());

        // Update processus only if a new one is specified
        if (dto.getProcessusId() != null && dto.getProcessusId() != 0) {
            Processus processus = processusRepo.findById(dto.getProcessusId())
                    .orElseThrow(() -> new RuntimeException(
                            "Processus introuvable avec l'id : " + dto.getProcessusId()
                    ));
            tache.setProcessus(processus);
        }

        return toDTO(tacheRepo.save(tache));
    }

    @Transactional
    public void delete(Long id) {
        if (!tacheRepo.existsById(id)) {
            throw new RuntimeException("Tâche introuvable avec l'id : " + id);
        }
        tacheRepo.deleteById(id);
    }

    // ─── Mappers ────────────────────────────────────────────────────────────────

    private TacheDTO toDTO(Tache t) {
        return TacheDTO.builder()
                .id(t.getId())
                .nom(t.getNom())
                .type(t.getType())
                .statut(t.getStatut())
                .description(t.getDescription())
                .assignee(t.getAssignee())
                .ordre(t.getOrdre())
                // getProcessus() can be null if not fetched (LAZY) — guard it
                .processusId(t.getProcessus() != null ? t.getProcessus().getId() : null)
                .build();
    }

    private Tache toEntity(TacheDTO dto, Processus processus) {
        return Tache.builder()
                .nom(dto.getNom())
                .type(dto.getType())
                .statut(dto.getStatut())
                .description(dto.getDescription())
                .assignee(dto.getAssignee())
                .ordre(dto.getOrdre())
                .processus(processus)
                .build();
    }
}