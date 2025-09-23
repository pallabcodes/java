package com.netflix.productivity.dto;

import com.netflix.productivity.entity.Project.ProjectStatus;
import com.netflix.productivity.entity.Project.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ProjectDto {
    String id;

    @NotBlank
    @Size(max = 50)
    String tenantId;

    @NotBlank
    @Size(max = 10)
    String key;

    @NotBlank
    @Size(max = 255)
    String name;

    @Size(max = 1000)
    String description;

    @NotNull
    ProjectStatus status;

    @NotNull
    ProjectType type;

    @NotBlank
    @Size(max = 36)
    String ownerId;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Long version;
}


