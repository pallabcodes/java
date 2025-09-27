package com.netflix.productivity.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void publishIssueCreated(String tenantId, String issueId, String projectId, String title) {
        IssueCreatedEvent event = IssueCreatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .issueId(issueId)
            .projectId(projectId)
            .title(title)
            .timestamp(OffsetDateTime.now())
            .build();
        
        eventPublisher.publishEvent(event);
        log.debug("Published IssueCreatedEvent: {}", event.getEventId());
    }
    
    public void publishIssueUpdated(String tenantId, String issueId, String projectId, String title, String status) {
        IssueUpdatedEvent event = IssueUpdatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .issueId(issueId)
            .projectId(projectId)
            .title(title)
            .status(status)
            .timestamp(OffsetDateTime.now())
            .build();
        
        eventPublisher.publishEvent(event);
        log.debug("Published IssueUpdatedEvent: {}", event.getEventId());
    }
    
    public void publishIssueCompleted(String tenantId, String issueId, String projectId, String title) {
        IssueCompletedEvent event = IssueCompletedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .issueId(issueId)
            .projectId(projectId)
            .title(title)
            .timestamp(OffsetDateTime.now())
            .build();
        
        eventPublisher.publishEvent(event);
        log.debug("Published IssueCompletedEvent: {}", event.getEventId());
    }
    
    public void publishSlaBreached(String tenantId, String issueId, String projectId, String title, String slaType) {
        SlaBreachedEvent event = SlaBreachedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .issueId(issueId)
            .projectId(projectId)
            .title(title)
            .slaType(slaType)
            .timestamp(OffsetDateTime.now())
            .build();
        
        eventPublisher.publishEvent(event);
        log.debug("Published SlaBreachedEvent: {}", event.getEventId());
    }
    
    public void publishProjectCreated(String tenantId, String projectId, String name) {
        ProjectCreatedEvent event = ProjectCreatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .projectId(projectId)
            .name(name)
            .timestamp(OffsetDateTime.now())
            .build();
        
        eventPublisher.publishEvent(event);
        log.debug("Published ProjectCreatedEvent: {}", event.getEventId());
    }
    
    public void publishUserActivity(String tenantId, String userId, String activity, String resourceId) {
        UserActivityEvent event = UserActivityEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .userId(userId)
            .activity(activity)
            .resourceId(resourceId)
            .timestamp(OffsetDateTime.now())
            .build();
        
        eventPublisher.publishEvent(event);
        log.debug("Published UserActivityEvent: {}", event.getEventId());
    }
    
    public void publishSystemEvent(String eventType, String message, Object data) {
        SystemEvent event = SystemEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .message(message)
            .data(data)
            .timestamp(OffsetDateTime.now())
            .build();
        
        eventPublisher.publishEvent(event);
        log.debug("Published SystemEvent: {}", event.getEventId());
    }
}
