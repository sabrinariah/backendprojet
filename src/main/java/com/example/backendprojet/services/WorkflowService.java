package com.example.backendprojet.services;


import com.example.backendprojet.dto.TaskDTO;
import com.example.backendprojet.Mappers.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final TaskMapper taskMapper;

    public List<TaskDTO> mesTaches(String username, List<String> roles) {
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                .taskCandidateGroupIn(roles)
                .taskAssignee(username)
                .endOr()
                .active()
                .orderByTaskCreateTime().desc()
                .list();

        return tasks.stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> tachesParDossier(String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .list();
        return tasks.stream().map(taskMapper::toDTO).collect(Collectors.toList());
    }

    public TaskDTO getTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return null;
        TaskDTO dto = taskMapper.toDTO(task);
        dto.setVariables(taskService.getVariables(taskId));
        return dto;
    }

    public void claim(String taskId, String username) {
        log.info("Réservation tâche {} par {}", taskId, username);
        taskService.claim(taskId, username);
    }

    public void unclaim(String taskId) {
        log.info("Libération tâche {}", taskId);
        taskService.setAssignee(taskId, null);
    }

    public void complete(String taskId, Map<String, Object> variables) {
        log.info("Complétion tâche {} avec variables : {}", taskId, variables);
        taskService.complete(taskId, variables);
    }

    public Map<String, Object> getVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    public List<HistoricActivityInstance> historique(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
    }
}