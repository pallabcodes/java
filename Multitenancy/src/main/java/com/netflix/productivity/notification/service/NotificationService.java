package com.netflix.productivity.notification.service;

import com.netflix.productivity.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final Map<String, Long> userActivityMetrics = new ConcurrentHashMap<>();
    
    public void notifyIssueCreated(IssueCreatedEvent event) {
        log.info("Sending notification for issue created: {} in project {}", 
                event.getIssueId(), event.getProjectId());
        
        // Send email notification
        sendEmailNotification(event.getTenantId(), "issue.created", event);
        
        // Send in-app notification
        sendInAppNotification(event.getTenantId(), "issue.created", event);
        
        // Send Slack notification (if configured)
        sendSlackNotification(event.getTenantId(), "issue.created", event);
    }
    
    public void notifyIssueUpdated(IssueUpdatedEvent event) {
        log.info("Sending notification for issue updated: {} in project {}", 
                event.getIssueId(), event.getProjectId());
        
        // Send email notification
        sendEmailNotification(event.getTenantId(), "issue.updated", event);
        
        // Send in-app notification
        sendInAppNotification(event.getTenantId(), "issue.updated", event);
    }
    
    public void notifyIssueCompleted(IssueCompletedEvent event) {
        log.info("Sending notification for issue completed: {} in project {}", 
                event.getIssueId(), event.getProjectId());
        
        // Send email notification
        sendEmailNotification(event.getTenantId(), "issue.completed", event);
        
        // Send in-app notification
        sendInAppNotification(event.getTenantId(), "issue.completed", event);
        
        // Send Slack notification
        sendSlackNotification(event.getTenantId(), "issue.completed", event);
    }
    
    public void notifySlaBreached(SlaBreachedEvent event) {
        log.warn("Sending urgent notification for SLA breach: {} in project {}", 
                event.getIssueId(), event.getProjectId());
        
        // Send urgent email notification
        sendUrgentEmailNotification(event.getTenantId(), "sla.breached", event);
        
        // Send urgent in-app notification
        sendUrgentInAppNotification(event.getTenantId(), "sla.breached", event);
        
        // Send urgent Slack notification
        sendUrgentSlackNotification(event.getTenantId(), "sla.breached", event);
    }
    
    public void notifyProjectCreated(ProjectCreatedEvent event) {
        log.info("Sending notification for project created: {} in tenant {}", 
                event.getProjectId(), event.getTenantId());
        
        // Send email notification
        sendEmailNotification(event.getTenantId(), "project.created", event);
        
        // Send in-app notification
        sendInAppNotification(event.getTenantId(), "project.created", event);
    }
    
    public void updateUserActivityMetrics(UserActivityEvent event) {
        String key = event.getTenantId() + ":" + event.getUserId() + ":" + event.getActivity();
        userActivityMetrics.merge(key, 1L, Long::sum);
        
        log.debug("Updated user activity metrics for key: {}", key);
    }
    
    private void sendEmailNotification(String tenantId, String eventType, BaseEvent event) {
        try {
            // This would integrate with an email service like SendGrid, SES, etc.
            log.debug("Sending email notification for {} to tenant {}", eventType, tenantId);
            
            // Simulate email sending
            Thread.sleep(100);
            
        } catch (Exception e) {
            log.error("Error sending email notification for {} to tenant {}", eventType, tenantId, e);
        }
    }
    
    private void sendInAppNotification(String tenantId, String eventType, BaseEvent event) {
        try {
            // This would store notifications in the database for in-app display
            log.debug("Sending in-app notification for {} to tenant {}", eventType, tenantId);
            
            // Simulate in-app notification
            Thread.sleep(50);
            
        } catch (Exception e) {
            log.error("Error sending in-app notification for {} to tenant {}", eventType, tenantId, e);
        }
    }
    
    private void sendSlackNotification(String tenantId, String eventType, BaseEvent event) {
        try {
            // This would integrate with Slack API
            log.debug("Sending Slack notification for {} to tenant {}", eventType, tenantId);
            
            // Simulate Slack notification
            Thread.sleep(200);
            
        } catch (Exception e) {
            log.error("Error sending Slack notification for {} to tenant {}", eventType, tenantId, e);
        }
    }
    
    private void sendUrgentEmailNotification(String tenantId, String eventType, BaseEvent event) {
        try {
            // This would send urgent email notifications with higher priority
            log.warn("Sending URGENT email notification for {} to tenant {}", eventType, tenantId);
            
            // Simulate urgent email sending
            Thread.sleep(50);
            
        } catch (Exception e) {
            log.error("Error sending urgent email notification for {} to tenant {}", eventType, tenantId, e);
        }
    }
    
    private void sendUrgentInAppNotification(String tenantId, String eventType, BaseEvent event) {
        try {
            // This would store urgent notifications in the database
            log.warn("Sending URGENT in-app notification for {} to tenant {}", eventType, tenantId);
            
            // Simulate urgent in-app notification
            Thread.sleep(25);
            
        } catch (Exception e) {
            log.error("Error sending urgent in-app notification for {} to tenant {}", eventType, tenantId, e);
        }
    }
    
    private void sendUrgentSlackNotification(String tenantId, String eventType, BaseEvent event) {
        try {
            // This would send urgent Slack notifications
            log.warn("Sending URGENT Slack notification for {} to tenant {}", eventType, tenantId);
            
            // Simulate urgent Slack notification
            Thread.sleep(100);
            
        } catch (Exception e) {
            log.error("Error sending urgent Slack notification for {} to tenant {}", eventType, tenantId, e);
        }
    }
    
    public Map<String, Long> getUserActivityMetrics() {
        return Map.copyOf(userActivityMetrics);
    }
    
    public void clearUserActivityMetrics() {
        userActivityMetrics.clear();
        log.info("Cleared user activity metrics");
    }
}
