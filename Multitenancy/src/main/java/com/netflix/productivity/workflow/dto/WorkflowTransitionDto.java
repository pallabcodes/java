package com.netflix.productivity.workflow.dto;

import lombok.Data;

@Data
public class WorkflowTransitionDto {
    private String id;
    private String fromStateId;
    private String toStateId;
    private String key;
    private String name;
    private String requiredPermission;
    private Integer ordinal;
}


