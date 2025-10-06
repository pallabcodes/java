package com.netflix.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreIssueData {
    private String id;
    private String tenantId;
    private String projectId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String type;
    private String assigneeId;
    private String reporterId;
    private Integer storyPoints;
    private Long timeEstimate;
    private Long timeSpent;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime slaBreachedAt;
    private String workflowState;
    private Long commentsCount;
    private Long attachmentsCount;
    private Long watchersCount;
}
