package com.example.backendprojet.dto;



import lombok.*;

import java.util.Date;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TaskDTO {
    private String id;
    private String name;
    private String taskDefinitionKey;
    private String processInstanceId;
    private String processDefinitionId;
    private String businessKey;
    private String assignee;
    private Date created;
    private Date due;
    private String priority;
    private Map<String, Object> variables;
}