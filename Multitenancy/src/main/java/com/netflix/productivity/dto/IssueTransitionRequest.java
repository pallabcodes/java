package com.netflix.productivity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "IssueTransitionRequest")
public class IssueTransitionRequest {
    @NotBlank
    @Schema(description = "Tenant id")
    private String tenantId;

    @NotBlank
    @Schema(description = "Project id")
    private String projectId;

    @NotBlank
    @Schema(description = "Workflow id")
    private String workflowId;

    @NotBlank
    @Schema(description = "From state id (current state)")
    private String fromStateId;

    @NotBlank
    @Schema(description = "To state id")
    private String toStateId;
}


