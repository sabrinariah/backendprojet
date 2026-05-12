package com.example.backendprojet.controller;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
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

    // ⚠️ DOIT correspondre au id du <bpmn:process> dans ton .bpmn
    private static final String PROCESS_KEY = "import-process";

    @Autowired private RuntimeService runtimeService;
    @Autowired private TaskService taskService;
    @Autowired private HistoryService historyService;

    // ====================================================
    // ▶️ Démarrer le processus
    // ====================================================
    @PostMapping("/demarrer")
    public ResponseEntity<Map<String, Object>> demarrerImport(
            @RequestBody Map<String, Object> payload) {

        // Extraire les variables (gère les 2 formats)
        Map<String, Object> variables = extraireVariables(payload);

        var instance = runtimeService
                .startProcessInstanceByKey(PROCESS_KEY, variables);

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", instance.getId());
        response.put("message", "Processus import démarré ✅");

        return ResponseEntity.ok(response);
    }

    // ====================================================
    // 📋 Lister les tâches
    // ====================================================
    @GetMapping("/taches")
    public ResponseEntity<List<Map<String, Object>>> getTaches() {

        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey(PROCESS_KEY)
                .list();

        List<Map<String, Object>> result = tasks.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("taskId", t.getId());
            m.put("taskName", t.getName());
            m.put("taskDefinitionKey", t.getTaskDefinitionKey());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("assignee", t.getAssignee());
            m.put("created", t.getCreateTime());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ====================================================
    // ✅ Compléter une tâche  ⚡ CORRIGÉ
    // ====================================================
    @PostMapping("/taches/{taskId}/completer")
    public ResponseEntity<?> completerTache(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> payload) {

        try {
            System.out.println("👉 TaskID reçu : " + taskId);
            System.out.println("👉 Payload brut : " + payload);

            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();

            if (task == null) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "Tâche introuvable ou déjà complétée")
                );
            }

            // ⚡ EXTRACTION CORRECTE DES VARIABLES
            Map<String, Object> variables = extraireVariables(payload);

            System.out.println("✅ Variables APRÈS extraction : " + variables);
            System.out.println("✅ Tâche : " + task.getName());

            taskService.complete(taskId, variables);

            return ResponseEntity.ok(Map.of(
                    "message", "Tâche complétée avec succès",
                    "taskId", taskId,
                    "variables", variables
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur serveur",
                    "details", e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().getMessage() : "N/A"
            ));
        }
    }

    // ====================================================
    // 🔍 Variables d'une instance
    // ====================================================
    @GetMapping("/instance/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getVariables(
            @PathVariable String processInstanceId) {

        List<HistoricVariableInstance> variables =
                historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .list();

        // Format compatible avec ton frontend : { key: { value: X } }
        Map<String, Object> result = new HashMap<>();
        for (HistoricVariableInstance v : variables) {
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("value", v.getValue());
            wrapper.put("type", v.getTypeName());
            result.put(v.getName(), wrapper);
        }

        return ResponseEntity.ok(result);
    }

    // ====================================================
    // 📊 Instances
    // ====================================================
    @GetMapping("/instances")
    public ResponseEntity<List<Map<String, Object>>> getInstances() {

        List<HistoricProcessInstance> instances = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(PROCESS_KEY)
                .orderByProcessInstanceStartTime()
                .desc()
                .list();

        List<Map<String, Object>> result = instances.stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", i.getId());
            m.put("processInstanceId", i.getId());
            m.put("processDefinitionId", i.getProcessDefinitionId());
            m.put("processDefinitionKey", PROCESS_KEY);
            m.put("startTime", i.getStartTime());
            m.put("endTime", i.getEndTime());
            m.put("durationInMillis", i.getDurationInMillis());
            m.put("ended", i.getEndTime() != null);
            m.put("state", i.getState()); // ACTIVE, COMPLETED, EXTERNALLY_TERMINATED, INTERNALLY_TERMINATED
            m.put("businessKey", i.getBusinessKey());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
    // ====================================================
    // 🛠️ MÉTHODE UTILITAIRE — EXTRACTION DES VARIABLES
    // ====================================================
    /**
     * Gère les 3 formats de payload possibles :
     *
     * Format 1 (Camunda REST avec wrapper) :
     *   { "variables": { "key": { "value": X, "type": "Boolean" } } }
     *
     * Format 2 (Wrapper sans typage) :
     *   { "variables": { "key": X } }
     *
     * Format 3 (Plat) :
     *   { "key": X }
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extraireVariables(Map<String, Object> payload) {
        Map<String, Object> variables = new HashMap<>();

        if (payload == null || payload.isEmpty()) {
            return variables;
        }

        // Détermine la source : payload.variables si présent, sinon payload directement
        Map<String, Object> source;
        if (payload.containsKey("variables") && payload.get("variables") instanceof Map) {
            source = (Map<String, Object>) payload.get("variables");
        } else {
            source = payload;
        }

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();

            if (val == null) continue;

            if (val instanceof Map) {
                // Format Camunda { "value": X, "type": "Y" }
                Map<String, Object> typed = (Map<String, Object>) val;
                if (typed.containsKey("value")) {
                    Object value = typed.get("value");
                    String type = (String) typed.getOrDefault("type", "String");
                    variables.put(key, convertirValeur(value, type));
                } else {
                    variables.put(key, val);
                }
            } else {
                // Valeur primitive directe
                variables.put(key, val);
            }
        }

        return variables;
    }

    /**
     * Convertit la valeur selon son type Camunda déclaré.
     */
    private Object convertirValeur(Object value, String type) {
        if (value == null) return null;

        try {
            switch (type) {
                case "Boolean":
                    if (value instanceof Boolean) return value;
                    return Boolean.parseBoolean(value.toString());

                case "Long":
                case "Integer":
                    if (value instanceof Number) return ((Number) value).longValue();
                    return Long.parseLong(value.toString());

                case "Double":
                    if (value instanceof Number) return ((Number) value).doubleValue();
                    return Double.parseDouble(value.toString());

                case "Date":
                case "String":
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur conversion " + value + " (" + type + ") : " + e.getMessage());
            return value;
        }
    }
}