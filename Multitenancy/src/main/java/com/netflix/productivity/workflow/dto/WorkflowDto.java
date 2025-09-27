package com.netflix.productivity.workflow.dto;

import lombok.Data;

@Data
public class WorkflowDto {
    private String id;
    private String tenantId;
    private String projectId;
    private String key;
    private String name;
    private String description;
    private Boolean active;
}


