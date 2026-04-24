package com.example.backendprojet.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "http://localhost:4200")
public class ImportController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    // ▶️ Démarrer le processus
    @PostMapping("/demarrer")
    public ResponseEntity<Map<String, Object>> demarrerImport(
            @RequestBody Map<String, Object> variables) {

        var instance = runtimeService
                .startProcessInstanceByKey("process_import", variables);

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", instance.getId());
        response.put("message", "Processus import démarré ✅");

        return ResponseEntity.ok(response);
    }

    // 📋 Lister les tâches
    @GetMapping("/taches")
    public ResponseEntity<List<Map<String, Object>>> getTaches() {

        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("process_import")
                .list();

        List<Map<String, Object>> result = tasks.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("taskId", t.getId());
            m.put("taskName", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("assignee", t.getAssignee());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ✅ Compléter une tâche
    @PostMapping("/taches/{taskId}/completer")
    public ResponseEntity<?> completerTache(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {

        try {

            System.out.println("👉 TaskID reçu: " + taskId);
            System.out.println("👉 Variables reçues: " + variables);

            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();

            if (task == null) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "Tâche introuvable ou déjà complétée")
                );
            }

            if (variables == null) {
                variables = new HashMap<>();
            }

            String taskName = task.getName();

            if ("Inspection physique des marchandises".equals(taskName)) {

                variables.putIfAbsent("conformeMarchandise", "non");

                System.out.println("✔ conformeMarchandise = "
                        + variables.get("conformeMarchandise"));
            }

            taskService.complete(taskId, variables);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Tâche complétée avec succès",
                            "taskId", taskId
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(500).body(
                    Map.of(
                            "error", "Erreur serveur",
                            "details", e.getMessage()
                    )
            );
        }
    }

    // 🔍 VARIABLES D’UNE INSTANCE (CORRIGÉ ✔️)
    @GetMapping("/instance/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getVariables(
            @PathVariable String processInstanceId) {

        List<HistoricVariableInstance> variables =
                historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .list();

        Map<String, Object> result = variables.stream()
                .collect(Collectors.toMap(
                        HistoricVariableInstance::getName,
                        HistoricVariableInstance::getValue
                ));

        return ResponseEntity.ok(result);
    }

    // 📊 Instances
    @GetMapping("/instances")
    public ResponseEntity<List<Map<String, Object>>> getInstances() {

        var instances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("process_import")
                .list();

        List<Map<String, Object>> result = instances.stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("processInstanceId", i.getId());
            m.put("processDefinitionId", i.getProcessDefinitionId());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}