package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.services.ProcessusService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/processus")
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessusController {

    private final ProcessusService processusService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    public ProcessusController(ProcessusService processusService,
                               RuntimeService runtimeService,
                               TaskService taskService,
                               HistoryService historyService) {
        this.processusService = processusService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
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
                return ResponseEntity.status(404).body("Processus introuvable");
            }
            return ResponseEntity.ok(p);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur lors du chargement");
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
                    "Process_Export",
                    variables
            );

            return ResponseEntity.ok(Map.of(
                    "processInstanceId", instance.getId(),
                    "message", "Processus démarré ✅"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Erreur démarrage Camunda: " + e.getMessage())
            );
        }
    }

    // =========================
    // GET TASKS
    // =========================

    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getTasks(
            @RequestParam(required = false) String processInstanceId,
            @RequestParam(required = false) String taskDefinitionKey) {

        var query = taskService.createTaskQuery();

        if (processInstanceId != null && !processInstanceId.isEmpty()) {
            query = query.processInstanceId(processInstanceId);
        }

        if (taskDefinitionKey != null && !taskDefinitionKey.isEmpty()) {
            query = query.taskDefinitionKey(taskDefinitionKey);
        }

        List<Map<String, Object>> tasks = query.list()
                .stream()
                .map(task -> {
                    Map<String, Object> t = new HashMap<>();
                    t.put("id", task.getId());
                    t.put("name", task.getName());
                    t.put("assignee", task.getAssignee());
                    t.put("processInstanceId", task.getProcessInstanceId());
                    t.put("taskDefinitionKey", task.getTaskDefinitionKey());
                    t.put("created", task.getCreateTime());
                    t.put("priority", task.getPriority());
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
                        .body(Map.of("message", "Task introuvable: " + taskId));
            }

            if (variables.containsKey("exportAutorise")) {
                variables.put("exportAutorise",
                        Boolean.valueOf(variables.get("exportAutorise").toString()));
            }
            variables.putIfAbsent("motifRefus", "");

            taskService.complete(taskId, variables);

            return ResponseEntity.ok(Map.of(
                    "message", "Task complétée ✅",
                    "taskId", taskId
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur Camunda: " + e.getMessage()));
        }
    }

    // =========================
    // VARIABLES D'UNE INSTANCE
    // GET /api/processus/instance/{id}/variables
    // =========================

    @GetMapping("/instance/{processInstanceId}/variables")
    public ResponseEntity<?> getVariables(@PathVariable String processInstanceId) {
        try {
            // ✅ 1) Vérifier que l'instance est encore active
            long activeCount = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .count();

            if (activeCount > 0) {
                // Instance active → variables runtime
                Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
                return ResponseEntity.ok(vars);
            }

            // ✅ 2) Instance terminée ou en erreur → historique
            Map<String, Object> historicVars = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(
                            v -> v.getName(),
                            v -> v.getValue() == null ? "" : v.getValue(),
                            (a, b) -> b
                    ));

            if (historicVars.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("message", "Aucune variable trouvée pour: " + processInstanceId));
            }

            return ResponseEntity.ok(historicVars);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur lecture variables: " + e.getMessage()));
        }
    }

    // =========================
    // HISTORIQUE DES TÂCHES
    // GET /api/processus/instance/{id}/history
    // =========================

    @GetMapping("/instance/{processInstanceId}/history")
    public ResponseEntity<?> getHistorique(@PathVariable String processInstanceId) {
        try {
            List<Map<String, Object>> history = historyService
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .sorted(Comparator.comparing(
                            HistoricTaskInstance::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ))
                    .map(this::mapHistoricTask)
                    .toList();

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur historique: " + e.getMessage()));
        }
    }

    // =========================
    // STATUT D'UNE INSTANCE
    // GET /api/processus/instance/{id}/status
    // =========================

    @GetMapping("/instance/{processInstanceId}/status")
    public ResponseEntity<?> getStatutInstance(@PathVariable String processInstanceId) {
        try {
            HistoricProcessInstance hpi = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (hpi == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("message", "Instance introuvable"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", hpi.getId());
            result.put("processDefinitionKey", hpi.getProcessDefinitionKey());
            result.put("startTime", hpi.getStartTime());
            result.put("endTime", hpi.getEndTime());
            result.put("state", hpi.getState());
            result.put("durationInMillis", hpi.getDurationInMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur statut: " + e.getMessage()));
        }
    }

    // =========================
    // Helper
    // =========================
    private Map<String, Object> mapHistoricTask(HistoricTaskInstance t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        m.put("taskDefinitionKey", t.getTaskDefinitionKey());
        m.put("assignee", t.getAssignee());
        m.put("startTime", t.getStartTime());
        m.put("endTime", t.getEndTime());
        m.put("durationInMillis", t.getDurationInMillis());
        m.put("deleteReason", t.getDeleteReason());
        return m;
    }
}