package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.entity.Tache;
import com.example.backendprojet.services.ProcessusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/processus")
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessusController {

    private final ProcessusService processusService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    public ProcessusController(ProcessusService processusService) {
        this.processusService = processusService;
    }

    // =========================
    // CRUD PROCESSUS
    // =========================

    @GetMapping
    public List<Processus> getAll() {
        return processusService.getAllProcessus();
    }

    @GetMapping("/{id}")
    public Processus getById(@PathVariable Long id) {
        return processusService.getProcessusById(id);
    }

    @PostMapping
    public Processus create(@RequestBody Processus processus) {
        return processusService.createProcessus(processus);
    }

    @PutMapping("/{id}")
    public Processus update(@PathVariable Long id, @RequestBody Processus processus) {
        return processusService.updateProcessus(id, processus);
    }

    @PatchMapping("/{id}/toggle")
    public Processus toggle(@PathVariable Long id) {
        return processusService.toggleProcessus(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcessus(@PathVariable Long id) {
        processusService.deleteProcessus(id);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // TACHES
    // =========================

    @PostMapping("/{id}/taches")
    public Tache addTache(@PathVariable Long id, @RequestBody Tache tache) {
        return processusService.addTacheToProcessus(id, tache);
    }

    @GetMapping("/{id}/taches")
    public List<Tache> getTaches(@PathVariable Long id) {
        return processusService.getTachesByProcessus(id);
    }

    @PutMapping("/taches/{id}")
    public Tache updateTache(@PathVariable Long id, @RequestBody Tache tache) {
        return processusService.updateTache(id, tache);
    }

    @PutMapping("/{processusId}/taches/{tacheId}")
    public Tache updateTache(
            @PathVariable Long processusId,
            @PathVariable Long tacheId,
            @RequestBody Tache tache
    ) {
        return processusService.updateTache(processusId, tacheId, tache);
    }

    @DeleteMapping("/taches/{id}")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        processusService.deleteTache(id);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // CAMUNDA - DEMARRER PROCESSUS
    // =========================

    @PostMapping("/demarrer")
    public ResponseEntity<Map<String, Object>> demarrerExport(@RequestBody Map<String, Object> variables) {

        var instance = runtimeService.startProcessInstanceByKey("process_export", variables);

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", instance.getId());
        response.put("message", "Processus export démarré avec succès");

        return ResponseEntity.ok(response);
    }

    // =========================
    // CAMUNDA - COMPLETER TASK
    // =========================

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<String> completeTask(@PathVariable String taskId) {

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            return ResponseEntity.status(404)
                    .body("Task introuvable ou déjà complétée");
        }

        System.out.println("Task trouvée : " + task.getId());

        // ✅ Ajouter les variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("exportAutorise", true); // ou false selon ton cas

        taskService.complete(taskId, variables);

        return ResponseEntity.ok("Task completed");
    }
    @GetMapping("/tasks")
    public List<Map<String, Object>> getTasks() {

        return taskService.createTaskQuery()
                .list()
                .stream()
                .map(task -> {
                    Map<String, Object> t = new HashMap<>();
                    t.put("id", task.getId());
                    t.put("name", task.getName());
                    t.put("assignee", task.getAssignee());
                    return t;
                })
                .toList();
    }
}