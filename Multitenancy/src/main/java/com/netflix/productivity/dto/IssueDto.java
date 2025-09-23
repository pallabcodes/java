package com.netflix.productivity.dto;

import com.netflix.productivity.entity.Issue.IssuePriority;
import com.netflix.productivity.entity.Issue.IssueStatus;
import com.netflix.productivity.entity.Issue.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class IssueDto {
    String id;

    @NotBlank
    @Size(max = 50)
    String tenantId;

    @NotBlank
    @Size(max = 20)
    String key;

    @NotBlank
    @Size(max = 255)
    String title;

    @Size(max = 5000)
    String description;

    @NotNull
    IssueStatus status;

    @NotNull
    IssuePriority priority;

    @NotNull
    IssueType type;

    @NotBlank
    @Size(max = 36)
    String projectId;

    @Size(max = 36)
    String assigneeId;

    @NotBlank
    @Size(max = 36)
    String reporterId;

    Integer storyPoints;
    Long timeEstimate;
    Long timeSpent;
    LocalDateTime dueDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Long version;
}


