package com.example.backendprojet.Mappers;


import com.example.backendprojet.dto.TaskDTO;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDTO toDTO(Task task) {
        if (task == null) return null;
        return TaskDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .processInstanceId(task.getProcessInstanceId())
                .processDefinitionId(task.getProcessDefinitionId())
                .assignee(task.getAssignee())
                .created(task.getCreateTime())
                .due(task.getDueDate())
                .priority(String.valueOf(task.getPriority()))
                .build();
    }
}