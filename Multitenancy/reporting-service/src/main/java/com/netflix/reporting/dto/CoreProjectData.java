package com.netflix.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreProjectData {
    private String id;
    private String tenantId;
    private String name;
    private String description;
    private String status;
    private String ownerId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long totalIssues;
    private Long completedIssues;
}
