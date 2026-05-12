package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.services.ProcessusService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/processus")
@CrossOrigin(
        origins = "http://localhost:4200",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS },
        allowedHeaders = "*",
        maxAge = 3600
)
public class ProcessusController {

    // ═══════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═══════════════════════════════════════════════════════════
    private static final String PROCESS_KEY = "process_export_corrected";

    /**
     * Liste de toutes les variables BPMN de type="date" déclarées dans
     * process_export_corrected.bpmn. Toute valeur reçue en String sera
     * automatiquement convertie en java.util.Date avant d'être envoyée à Camunda.
     */
    private static final Set<String> DATE_FIELDS = Set.of(
            "dateDepot",
            "dateInspectionDemande",
            "dateInspectionPV",
            "dateDelivrancePhyto",
            "dateEmpotage",
            "dateDelivranceOrigine",
            "dateBooking",
            "dateETD",
            "dateDepotDouane",
            "dateInspectionPhys",
            "datePaiement",
            "nouvelleDatePaiement",
            "dateDelivranceBon",
            "dateAutorisation",
            "dateEmbarquement",
            "dateConstat"
    );

    // ═══════════════════════════════════════════════════════════
    //  INJECTION PAR CONSTRUCTEUR
    // ═══════════════════════════════════════════════════════════
    private final ProcessusService processusService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;

    public ProcessusController(ProcessusService processusService,
                               RuntimeService runtimeService,
                               TaskService taskService,
                               HistoryService historyService,
                               RepositoryService repositoryService) {
        this.processusService = processusService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
    }

    // ═══════════════════════════════════════════════════════════
    //  CRUD PROCESSUS
    // ═══════════════════════════════════════════════════════════

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
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Processus processus) {
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

    // ═══════════════════════════════════════════════════════════
    //  DÉMARRER LE PROCESSUS (Camunda)
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/demarrer")
    public ResponseEntity<?> startProcess(
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            if (variables == null) {
                variables = new HashMap<>();
            }

            // ✅ FIX : convertir toutes les dates String → Date typée Camunda
            variables = convertDateVariables(variables);

            System.out.println("👉 START VARIABLES: " + variables);

            var instance = runtimeService.startProcessInstanceByKey(
                    PROCESS_KEY,
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

    // ═══════════════════════════════════════════════════════════
    //  GET TASKS
    // ═══════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════
    //  COMPLETE TASK
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            if (variables == null) {
                variables = new HashMap<>();
            }

            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("message", "Task introuvable: " + taskId));
            }

            if (variables.containsKey("exportAutorise")) {
                variables.put("exportAutorise",
                        Boolean.valueOf(variables.get("exportAutorise").toString()));
            }
            variables.putIfAbsent("motifRefus", "");

            // ✅ FIX : convertir toutes les dates String → Date typée Camunda
            variables = convertDateVariables(variables);

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

    // ═══════════════════════════════════════════════════════════
    //  VARIABLES D'UNE INSTANCE
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/instance/{processInstanceId}/variables")
    public ResponseEntity<?> getVariables(@PathVariable String processInstanceId) {
        try {
            long activeCount = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .count();

            if (activeCount > 0) {
                Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
                return ResponseEntity.ok(vars);
            }

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

    // ═══════════════════════════════════════════════════════════
    //  HISTORIQUE DES TÂCHES
    // ═══════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════
    //  STATUT D'UNE INSTANCE
    // ═══════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════
    //  TOUTES LES INSTANCES
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/instances")
    public ResponseEntity<?> getAllInstances() {
        try {
            List<HistoricProcessInstance> instances = historyService
                    .createHistoricProcessInstanceQuery()
                    .processDefinitionKey(PROCESS_KEY)
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list();

            List<Map<String, Object>> result = instances.stream().map(hpi -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", hpi.getId());
                m.put("processInstanceId", hpi.getId());
                m.put("processDefinitionKey", hpi.getProcessDefinitionKey());
                m.put("startTime", hpi.getStartTime());
                m.put("endTime", hpi.getEndTime());
                m.put("durationInMillis", hpi.getDurationInMillis());
                m.put("state", hpi.getState());
                m.put("ended", hpi.getEndTime() != null);
                m.put("businessKey", hpi.getBusinessKey());

                Map<String, Object> vars = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(hpi.getId())
                        .list()
                        .stream()
                        .collect(Collectors.toMap(
                                v -> v.getName(),
                                v -> v.getValue() == null ? "" : v.getValue(),
                                (a, b) -> b
                        ));

                m.put("exportateur", vars.getOrDefault("exportateur", "—"));
                m.put("paysDestination", vars.getOrDefault("paysDestination", "—"));
                m.put("typeProduit", vars.getOrDefault("typeProduit", "—"));
                m.put("dossierId", vars.getOrDefault("dossierId", "—"));

                boolean rejete = Boolean.FALSE.equals(vars.get("exportEligible"));
                m.put("rejete", rejete);

                return m;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur instances: " + e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — ÉTAT DE LA DÉFINITION
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/definition/etat")
    public ResponseEntity<Map<String, Object>> getEtatDefinition() {
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS_KEY)
                .latestVersion()
                .singleResult();

        if (def == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("key", def.getKey());
        res.put("version", def.getVersion());
        res.put("suspended", def.isSuspended());
        res.put("id", def.getId());
        return ResponseEntity.ok(res);
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — SUSPENDRE / ACTIVER LA DÉFINITION
    // ═══════════════════════════════════════════════════════════

    @PutMapping("/definition/suspendre")
    public ResponseEntity<Map<String, Object>> suspendreDefinition(
            @RequestParam(defaultValue = "false") boolean includeInstances) {
        try {
            repositoryService.suspendProcessDefinitionByKey(PROCESS_KEY, includeInstances, null);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Définition suspendue avec succès",
                    "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur : " + e.getMessage()
            ));
        }
    }

    @PutMapping("/definition/activer")
    public ResponseEntity<Map<String, Object>> activerDefinition(
            @RequestParam(defaultValue = "false") boolean includeInstances) {
        try {
            repositoryService.activateProcessDefinitionByKey(PROCESS_KEY, includeInstances, null);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Définition activée avec succès",
                    "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur : " + e.getMessage()
            ));
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — SUSPENDRE / REPRENDRE UNE INSTANCE
    // ═══════════════════════════════════════════════════════════

    @PutMapping("/instance/{id}/suspendre")
    public ResponseEntity<Map<String, Object>> suspendreInstance(@PathVariable String id) {
        try {
            runtimeService.suspendProcessInstanceById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Instance " + id + " suspendue"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur : " + e.getMessage()
            ));
        }
    }

    @PutMapping("/instance/{id}/reprendre")
    public ResponseEntity<Map<String, Object>> reprendreInstance(@PathVariable String id) {
        try {
            runtimeService.activateProcessInstanceById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Instance " + id + " reprise"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur : " + e.getMessage()
            ));
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — ANNULER UNE INSTANCE (garde l'historique)
    // ═══════════════════════════════════════════════════════════

    @DeleteMapping("/instance/{id}")
    public ResponseEntity<Map<String, Object>> annulerInstance(
            @PathVariable String id,
            @RequestParam String raison) {
        try {
            runtimeService.deleteProcessInstance(id, raison);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Instance annulée : " + raison
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur : " + e.getMessage()
            ));
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — SUPPRIMER COMPLÈTEMENT UNE INSTANCE
    //  → Supprime runtime + historique → disparait totalement de Camunda
    // ═══════════════════════════════════════════════════════════

    @DeleteMapping("/instance/{id}/supprimer")
    public ResponseEntity<Map<String, Object>> supprimerInstanceCompletement(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Suppression depuis interface") String raison) {
        try {
            System.out.println("🗑️ Suppression complète de l'instance : " + id);

            // 1. Vérifier si l'instance est encore active (runtime)
            long activeCount = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(id)
                    .count();

            // 2. Si encore active → l'annuler d'abord (sinon erreur "still running")
            if (activeCount > 0) {
                System.out.println("   → Instance active, annulation runtime...");
                runtimeService.deleteProcessInstance(
                        id,
                        raison,
                        false,  // skipCustomListeners
                        true,   // externallyTerminated
                        false,  // skipIoMappings
                        false   // skipSubprocesses
                );
            }

            // 3. Vérifier que l'historique existe
            HistoricProcessInstance hpi = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(id)
                    .singleResult();

            if (hpi == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Instance introuvable : " + id
                ));
            }

            // 4. Supprimer définitivement l'historique
            System.out.println("   → Suppression de l'historique...");
            historyService.deleteHistoricProcessInstance(id);

            System.out.println("✅ Instance " + id + " complètement supprimée");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Instance supprimée définitivement de Camunda",
                    "processInstanceId", id,
                    "raison", raison
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur suppression : " + e.getMessage()
            ));
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILS PRIVÉS
    // ═══════════════════════════════════════════════════════════

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

    /**
     * ✅ Parcourt les variables, détecte les champs date, et convertit la String
     * en java.util.Date typé (Variables.dateValue) pour que Camunda
     * accepte la valeur sur les formField type="date".
     */
    private Map<String, Object> convertDateVariables(Map<String, Object> variables) {
        Map<String, Object> converted = new HashMap<>();

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (DATE_FIELDS.contains(key) && value instanceof String) {
                String dateStr = (String) value;

                if (dateStr.isBlank()) {
                    // String vide → on ignore le champ
                    continue;
                }

                try {
                    Date parsed = parseFlexibleDate(dateStr);
                    converted.put(key, Variables.dateValue(parsed));
                    System.out.println("📅 Converti " + key + " : " + dateStr + " → " + parsed);
                } catch (ParseException e) {
                    System.err.println("⚠️ Date invalide pour " + key + " : " + dateStr);
                    converted.put(key, value);
                }
            } else {
                converted.put(key, value);
            }
        }

        return converted;
    }

    /**
     * ✅ Parse différents formats de date possibles envoyés par Angular :
     * - "2026-05-12"
     * - "2026-05-12T00:00:00"
     * - "2026-05-12T00:00:00.000Z"
     */
    private Date parseFlexibleDate(String dateStr) throws ParseException {
        if (dateStr.contains("T")) {
            dateStr = dateStr.split("T")[0];
        }
        return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
    }
}