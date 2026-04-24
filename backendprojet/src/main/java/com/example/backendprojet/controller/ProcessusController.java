package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.services.ProcessusService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/processus")
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessusController {

    private final ProcessusService processusService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public ProcessusController(ProcessusService processusService,
                               RuntimeService runtimeService,
                               TaskService taskService) {
        this.processusService = processusService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    // =========================
    // CRUD PROCESSUS
    // =========================

    @GetMapping
    public ResponseEntity<List<Processus>> getAll() {
        return ResponseEntity.ok(processusService.getAllProcessus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        try {
            Processus p = processusService.getProcessusById(id);

            if (p == null) {
                return ResponseEntity.status(404)
                        .body("Processus introuvable");
            }

            return ResponseEntity.ok(p);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Erreur serveur lors du chargement");
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Processus processus) {
        try {
            return ResponseEntity.ok(processusService.createProcessus(processus));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur création processus");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Processus processus) {
        try {
            return ResponseEntity.ok(processusService.updateProcessus(id, processus));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur update");
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(processusService.toggleProcessus(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur toggle");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            processusService.deleteProcessus(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur suppression");
        }
    }

    // =========================
    // START PROCESS (Camunda)
    // =========================

    @PostMapping("/demarrer")
    public ResponseEntity<?> startProcess(
            @RequestBody(required = false) Map<String, Object> variables) {

        try {

            if (variables == null) {
                variables = new HashMap<>();
            }

            System.out.println("👉 START VARIABLES: " + variables);

            var instance = runtimeService.startProcessInstanceByKey(
                    "process_export",
                    variables
            );

            return ResponseEntity.ok(Map.of(
                    "processInstanceId", instance.getId(),
                    "message", "Processus démarré ✅"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur démarrage Camunda");
        }
    }

    // =========================
    // GET TASKS
    // =========================

    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getTasks() {

        List<Map<String, Object>> tasks = taskService.createTaskQuery()
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

        return ResponseEntity.ok(tasks);
    }

    // =========================
    // COMPLETE TASK
    // =========================

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {

        try {

            if (variables == null) {
                variables = new HashMap<>();
            }

            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();

            if (task == null) {
                return ResponseEntity.status(404)
                        .body("Task introuvable");
            }

            // 🔥 sécurisation variables
            Boolean exportAutorise = variables.get("exportAutorise") != null
                    ? Boolean.valueOf(variables.get("exportAutorise").toString())
                    : false;

            variables.put("exportAutorise", exportAutorise);
            variables.putIfAbsent("motifRefus", "");

            taskService.complete(taskId, variables);

            return ResponseEntity.ok(Map.of(
                    "message", "Task complétée ✅",
                    "taskId", taskId
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur Camunda");
        }
    }
}