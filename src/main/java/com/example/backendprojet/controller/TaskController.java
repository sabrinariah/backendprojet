package com.example.backendprojet.controller;



import com.example.backendprojet.dto.TaskDTO;
import com.example.backendprojet.services.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tâches Workflow")
@CrossOrigin(origins = "*")
public class TaskController {

    private final WorkflowService workflowService;

    @GetMapping("/my-tasks")
    @Operation(summary = "Mes tâches en attente")
    public List<TaskDTO> mesTaches(Authentication auth,
                                   @RequestParam(required = false) List<String> roles) {
        String username = auth != null ? auth.getName() : "anonyme";
        List<String> userRoles;

        if (roles != null && !roles.isEmpty()) {
            userRoles = roles;
        } else if (auth != null) {
            userRoles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(r -> r.replace("ROLE_", ""))
                    .toList();
        } else {
            userRoles = List.of("EXPORTATEUR", "TRANSITAIRE", "DOUANE");
        }

        return workflowService.mesTaches(username, userRoles);
    }

    @GetMapping("/process/{processInstanceId}")
    public List<TaskDTO> tachesParDossier(@PathVariable String processInstanceId) {
        return workflowService.tachesParDossier(processInstanceId);
    }

    @GetMapping("/{taskId}")
    public TaskDTO getTask(@PathVariable String taskId) {
        return workflowService.getTask(taskId);
    }

    @GetMapping("/{taskId}/variables")
    public Map<String, Object> getVariables(@PathVariable String taskId) {
        return workflowService.getVariables(taskId);
    }

    @PostMapping("/{taskId}/claim")
    public ResponseEntity<Void> claim(@PathVariable String taskId, Principal principal) {
        String username = principal != null ? principal.getName() : "demo";
        workflowService.claim(taskId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<Void> unclaim(@PathVariable String taskId) {
        workflowService.unclaim(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> complete(@PathVariable String taskId,
                                         @RequestBody(required = false) Map<String, Object> variables) {
        workflowService.complete(taskId, variables != null ? variables : Map.of());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/process/{processInstanceId}/history")
    public List<?> historique(@PathVariable String processInstanceId) {
        return workflowService.historique(processInstanceId);
    }
}