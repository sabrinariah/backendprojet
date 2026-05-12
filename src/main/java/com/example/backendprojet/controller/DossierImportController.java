package com.example.backendprojet.controller;



import com.example.backendprojet.dto.ProcessStartResponse;
import com.example.backendprojet.dto.TaskDTO;
import com.example.backendprojet.entity.DossierImport;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.repository.DossierImportRepository;
import com.example.backendprojet.services.DossierImportService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints REST liés aux dossiers d'importation.
 */
@RestController
@RequestMapping("/dossiers")
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequiredArgsConstructor
public class DossierImportController {

    private final DossierImportService service;
    private final DossierImportRepository repository;

    /** POST /api/dossiers — Crée un dossier et démarre le processus BPMN. */
    @PostMapping
    public ResponseEntity<ProcessStartResponse> creer(@RequestBody DossierImport dossier) {
        DossierImport result = service.demarrerProcessus(dossier);
        ProcessStartResponse response = new ProcessStartResponse(
                result,
                result.getProcessInstanceId(),
                "Processus d'import démarré avec succès"
        );
        return ResponseEntity.ok(response);
    }

    /** GET /api/dossiers — Liste tous les dossiers. */
    @GetMapping
    public ResponseEntity<List<DossierImport>> liste(
            @RequestParam(required = false) StatutDossier statut,
            @RequestParam(required = false) String importateur) {

        List<DossierImport> result;
        if (statut != null) {
            result = repository.findByStatut(statut);
        } else if (importateur != null) {
            result = repository.findByImportateur(importateur);
        } else {
            result = repository.findAll();
        }
        return ResponseEntity.ok(result);
    }

    /** GET /api/dossiers/{id} — Détail d'un dossier. */
    @GetMapping("/{id}")
    public ResponseEntity<DossierImport> detail(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/dossiers/by-dossier-id/{dossierId} — Recherche par numéro. */
    @GetMapping("/by-dossier-id/{dossierId}")
    public ResponseEntity<DossierImport> parDossierId(@PathVariable String dossierId) {
        return repository.findByDossierId(dossierId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/dossiers/{id}/tasks — Tâches actives pour un dossier. */
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskDTO>> tachesParDossier(@PathVariable Long id) {
        return repository.findById(id).map(dossier -> {
            if (dossier.getProcessInstanceId() == null) {
                return ResponseEntity.ok(List.<TaskDTO>of());
            }
            List<Task> tasks = service.listerMesTaches(null, null).stream()
                    .filter(t -> dossier.getProcessInstanceId().equals(t.getProcessInstanceId()))
                    .collect(Collectors.toList());
            List<TaskDTO> dtos = tasks.stream().map(this::toDTO).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }).orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/dossiers/{id}/variables — Variables Camunda du processus. */
    @GetMapping("/{id}/variables")
    public ResponseEntity<Map<String, Object>> variables(@PathVariable Long id) {
        return repository.findById(id)
                .map(d -> ResponseEntity.ok(service.getVariables(d.getProcessInstanceId())))
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/dossiers/{id} — Supprime un dossier (admin). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TaskDTO toDTO(Task t) {
        return TaskDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .taskDefinitionKey(t.getTaskDefinitionKey())
                .processInstanceId(t.getProcessInstanceId())
                .processDefinitionId(t.getProcessDefinitionId())
                .businessKey(null)
                .assignee(t.getAssignee())
                .created(t.getCreateTime())
                .due(t.getDueDate())
                .priority(String.valueOf(t.getPriority()))
                .variables(null)
                .build();
    }
}