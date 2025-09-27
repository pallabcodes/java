package com.netflix.productivity.workflow.dto;

import lombok.Data;

@Data
public class WorkflowStateDto {
    private String id;
    private String key;
    private String name;
    private Boolean initial;
    private Boolean terminal;
    private Integer ordinal;
    private String status;
}


