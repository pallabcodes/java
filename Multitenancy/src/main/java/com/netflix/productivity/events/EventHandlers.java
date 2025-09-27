package com.netflix.productivity.events;

import com.netflix.productivity.audit.service.AuditService;
import com.netflix.productivity.notification.service.NotificationService;
import com.netflix.productivity.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandlers {
    
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final WebhookService webhookService;
    
    @EventListener
    @Async
    public void handleIssueCreated(IssueCreatedEvent event) {
        log.info("Handling IssueCreatedEvent: {}", event.getEventId());
        
        try {
            // Audit logging
            auditService.logEvent(
                event.getTenantId(),
                "ISSUE_CREATED",
                event.getIssueId(),
                "Issue created: " + event.getTitle()
            );
            
            // Send notifications
            notificationService.notifyIssueCreated(event);
            
            // Trigger webhooks
            webhookService.triggerWebhook(event.getTenantId(), "issue.created", event);
            
        } catch (Exception e) {
            log.error("Error handling IssueCreatedEvent: {}", event.getEventId(), e);
        }
    }
    
    @EventListener
    @Async
    public void handleIssueUpdated(IssueUpdatedEvent event) {
        log.info("Handling IssueUpdatedEvent: {}", event.getEventId());
        
        try {
            // Audit logging
            auditService.logEvent(
                event.getTenantId(),
                "ISSUE_UPDATED",
                event.getIssueId(),
                "Issue updated: " + event.getTitle() + " -> " + event.getStatus()
            );
            
            // Send notifications
            notificationService.notifyIssueUpdated(event);
            
            // Trigger webhooks
            webhookService.triggerWebhook(event.getTenantId(), "issue.updated", event);
            
        } catch (Exception e) {
            log.error("Error handling IssueUpdatedEvent: {}", event.getEventId(), e);
        }
    }
    
    @EventListener
    @Async
    public void handleIssueCompleted(IssueCompletedEvent event) {
        log.info("Handling IssueCompletedEvent: {}", event.getEventId());
        
        try {
            // Audit logging
            auditService.logEvent(
                event.getTenantId(),
                "ISSUE_COMPLETED",
                event.getIssueId(),
                "Issue completed: " + event.getTitle()
            );
            
            // Send notifications
            notificationService.notifyIssueCompleted(event);
            
            // Trigger webhooks
            webhookService.triggerWebhook(event.getTenantId(), "issue.completed", event);
            
        } catch (Exception e) {
            log.error("Error handling IssueCompletedEvent: {}", event.getEventId(), e);
        }
    }
    
    @EventListener
    @Async
    public void handleSlaBreached(SlaBreachedEvent event) {
        log.warn("Handling SlaBreachedEvent: {}", event.getEventId());
        
        try {
            // Audit logging
            auditService.logEvent(
                event.getTenantId(),
                "SLA_BREACHED",
                event.getIssueId(),
                "SLA breached for issue: " + event.getTitle() + " (Type: " + event.getSlaType() + ")"
            );
            
            // Send urgent notifications
            notificationService.notifySlaBreached(event);
            
            // Trigger webhooks
            webhookService.triggerWebhook(event.getTenantId(), "sla.breached", event);
            
        } catch (Exception e) {
            log.error("Error handling SlaBreachedEvent: {}", event.getEventId(), e);
        }
    }
    
    @EventListener
    @Async
    public void handleProjectCreated(ProjectCreatedEvent event) {
        log.info("Handling ProjectCreatedEvent: {}", event.getEventId());
        
        try {
            // Audit logging
            auditService.logEvent(
                event.getTenantId(),
                "PROJECT_CREATED",
                event.getProjectId(),
                "Project created: " + event.getName()
            );
            
            // Send notifications
            notificationService.notifyProjectCreated(event);
            
            // Trigger webhooks
            webhookService.triggerWebhook(event.getTenantId(), "project.created", event);
            
        } catch (Exception e) {
            log.error("Error handling ProjectCreatedEvent: {}", event.getEventId(), e);
        }
    }
    
    @EventListener
    @Async
    public void handleUserActivity(UserActivityEvent event) {
        log.debug("Handling UserActivityEvent: {}", event.getEventId());
        
        try {
            // Audit logging
            auditService.logEvent(
                event.getTenantId(),
                "USER_ACTIVITY",
                event.getUserId(),
                "User activity: " + event.getActivity() + " on " + event.getResourceId()
            );
            
            // Update user activity metrics
            notificationService.updateUserActivityMetrics(event);
            
        } catch (Exception e) {
            log.error("Error handling UserActivityEvent: {}", event.getEventId(), e);
        }
    }
    
    @EventListener
    @Async
    public void handleSystemEvent(SystemEvent event) {
        log.info("Handling SystemEvent: {}", event.getEventId());
        
        try {
            // Audit logging
            auditService.logEvent(
                event.getTenantId(),
                "SYSTEM_EVENT",
                "SYSTEM",
                "System event: " + event.getMessage()
            );
            
            // Handle system events based on type
            switch (event.getEventType()) {
                case "SYSTEM_STARTUP":
                    handleSystemStartup(event);
                    break;
                case "SYSTEM_SHUTDOWN":
                    handleSystemShutdown(event);
                    break;
                case "CONFIGURATION_CHANGED":
                    handleConfigurationChanged(event);
                    break;
                default:
                    log.debug("Unhandled system event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error handling SystemEvent: {}", event.getEventId(), e);
        }
    }
    
    private void handleSystemStartup(SystemEvent event) {
        log.info("System startup event received");
        // Handle system startup logic
    }
    
    private void handleSystemShutdown(SystemEvent event) {
        log.info("System shutdown event received");
        // Handle system shutdown logic
    }
    
    private void handleConfigurationChanged(SystemEvent event) {
        log.info("Configuration changed event received");
        // Handle configuration change logic
    }
}
