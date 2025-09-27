package com.netflix.productivity.repository.projection;

import java.time.LocalDateTime;

public interface IssueListProjection {
    String getId();
    String getKey();
    String getTitle();
    String getStatus();
    String getPriority();
    String getAssigneeId();
    LocalDateTime getUpdatedAt();
}

