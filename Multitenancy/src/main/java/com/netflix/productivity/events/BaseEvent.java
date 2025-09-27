package com.netflix.productivity.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String eventId;
    private String tenantId;
    private OffsetDateTime timestamp;
    private String eventType;
    
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class IssueCreatedEvent extends BaseEvent {
    private String issueId;
    private String projectId;
    private String title;
    private String reporterId;
    private String assigneeId;
    private String priority;
    private String type;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class IssueUpdatedEvent extends BaseEvent {
    private String issueId;
    private String projectId;
    private String title;
    private String status;
    private String assigneeId;
    private String priority;
    private String type;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class IssueCompletedEvent extends BaseEvent {
    private String issueId;
    private String projectId;
    private String title;
    private String assigneeId;
    private String resolution;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SlaBreachedEvent extends BaseEvent {
    private String issueId;
    private String projectId;
    private String title;
    private String slaType;
    private String assigneeId;
    private OffsetDateTime slaDueAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProjectCreatedEvent extends BaseEvent {
    private String projectId;
    private String name;
    private String description;
    private String ownerId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UserActivityEvent extends BaseEvent {
    private String userId;
    private String activity;
    private String resourceId;
    private String resourceType;
    private String ipAddress;
    private String userAgent;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SystemEvent extends BaseEvent {
    private String eventType;
    private String message;
    private Object data;
    private String severity;
}
