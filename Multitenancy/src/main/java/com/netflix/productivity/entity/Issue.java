package com.netflix.productivity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Netflix Production-Grade Issue Entity
 * 
 * This entity demonstrates Netflix production standards for issue tracking including:
 * 1. Comprehensive issue management and tracking
 * 2. Multi-tenant data isolation
 * 3. Workflow state management
 * 4. Priority and severity classification
 * 5. Assignment and ownership tracking
 * 6. Audit trail for issue changes
 * 7. Performance optimization for high-throughput
 * 8. Caching for fast issue retrieval
 * 
 * For C/C++ engineers:
 * - JPA entities are like database table mappings in C++ ORM libraries
 * - @Entity is like marking a class as a database table
 * - @Id is like primary key in database tables
 * - Validation annotations are like input validation in C++
 * - @CreationTimestamp is like automatic timestamp fields
 * - Issue tracking is like bug tracking systems in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Table(name = "issues",
       indexes = {
           @Index(name = "idx_issue_tenant_id", columnList = "tenantId"),
           @Index(name = "idx_issue_project_id", columnList = "projectId"),
           @Index(name = "idx_issue_assignee_id", columnList = "assigneeId"),
           @Index(name = "idx_issue_reporter_id", columnList = "reporterId"),
           @Index(name = "idx_issue_status", columnList = "status"),
           @Index(name = "idx_issue_priority", columnList = "priority"),
           @Index(name = "idx_issue_type", columnList = "type"),
           @Index(name = "idx_issue_created_at", columnList = "createdAt"),
           @Index(name = "idx_issue_updated_at", columnList = "updatedAt"),
           @Index(name = "idx_issue_deleted_at", columnList = "deletedAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_issue_tenant_key", columnNames = {"tenantId", "key"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    @Id
    @GeneratedValue(generator = "issue-uuid")
    @GenericGenerator(name = "issue-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @NotBlank(message = "Tenant ID is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 50, message = "Tenant ID must not exceed 50 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotBlank(message = "Issue key is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 20, message = "Issue key must not exceed 20 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "key", nullable = false, length = 20)
    private String key;

    @NotBlank(message = "Title is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 255, message = "Title must not exceed 255 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "description", length = 5000)
    private String description;

    @NotNull(message = "Status is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IssueStatus status;

    @NotNull(message = "Priority is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private IssuePriority priority;

    @NotNull(message = "Type is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private IssueType type;

    @NotBlank(message = "Project ID is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 36, message = "Project ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Size(max = 36, message = "Assignee ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "assignee_id", length = 36)
    private String assigneeId;

    @NotBlank(message = "Reporter ID is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 36, message = "Reporter ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "reporter_id", nullable = false, length = 36)
    private String reporterId;

    @Size(max = 36, message = "Parent issue ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "parent_issue_id", length = 36)
    private String parentIssueId;

    @Size(max = 36, message = "Epic ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "epic_id", length = 36)
    private String epicId;

    @Size(max = 36, message = "Sprint ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "sprint_id", length = 36)
    private String sprintId;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "time_estimate")
    private Long timeEstimate; // in minutes

    @Column(name = "time_spent")
    private Long timeSpent; // in minutes

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "sla_breached_at")
    private LocalDateTime slaBreachedAt;

    @Column(name = "resolution")
    @Enumerated(EnumType.STRING)
    private IssueResolution resolution;

    @Size(max = 1000, message = "Resolution comment must not exceed 1000 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "resolution_comment", length = 1000)
    private String resolutionComment;

    @Column(name = "labels", length = 500)
    private String labels; // Comma-separated labels

    @Column(name = "components", length = 500)
    private String components; // Comma-separated components

    @Column(name = "fix_versions", length = 500)
    private String fixVersions; // Comma-separated fix versions

    @Column(name = "affected_versions", length = 500)
    private String affectedVersions; // Comma-separated affected versions

    @Column(name = "environment", length = 100)
    private String environment;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "device", length = 100)
    private String device;

    @Column(name = "attachment_count")
    private Integer attachmentCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "watcher_count")
    private Integer watcherCount;

    @Column(name = "vote_count")
    private Integer voteCount;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Size(max = 1000, message = "Blocking reason must not exceed 1000 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "blocking_reason", length = 1000)
    private String blockingReason;

    @Column(name = "is_confidential")
    private Boolean isConfidential;

    @Column(name = "is_subtask")
    private Boolean isSubtask;

    @Column(name = "is_epic")
    private Boolean isEpic;

    @Column(name = "is_story")
    private Boolean isStory;

    @Column(name = "is_bug")
    private Boolean isBug;

    @Column(name = "is_task")
    private Boolean isTask;

    @Column(name = "is_improvement")
    private Boolean isImprovement;

    @Column(name = "is_new_feature")
    private Boolean isNewFeature;

    @Column(name = "workflow_id", length = 36)
    private String workflowId;

    @Column(name = "workflow_state_id", length = 36)
    private String workflowStateId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Business method to check if issue is open
     * 
     * @return true if issue is open
     */
    public boolean isOpen() {
        return this.status == IssueStatus.OPEN || this.status == IssueStatus.IN_PROGRESS || 
               this.status == IssueStatus.REOPENED || this.status == IssueStatus.IN_REVIEW;
    }

    /**
     * Business method to check if issue is closed
     * 
     * @return true if issue is closed
     */
    public boolean isClosed() {
        return this.status == IssueStatus.CLOSED || this.status == IssueStatus.RESOLVED;
    }

    /**
     * Business method to check if issue is resolved
     * 
     * @return true if issue is resolved
     */
    public boolean isResolved() {
        return this.status == IssueStatus.RESOLVED;
    }

    /**
     * Business method to check if issue is high priority
     * 
     * @return true if issue is high priority
     */
    public boolean isHighPriority() {
        return this.priority == IssuePriority.HIGH || this.priority == IssuePriority.CRITICAL;
    }

    /**
     * Business method to check if issue is critical
     * 
     * @return true if issue is critical
     */
    public boolean isCritical() {
        return this.priority == IssuePriority.CRITICAL;
    }

    /**
     * Business method to check if issue is overdue
     * 
     * @return true if issue is overdue
     */
    public boolean isOverdue() {
        return this.dueDate != null && this.dueDate.isBefore(LocalDateTime.now()) && !isClosed();
    }

    /**
     * Business method to check if issue is assigned
     * 
     * @return true if issue is assigned
     */
    public boolean isAssigned() {
        return this.assigneeId != null && !this.assigneeId.trim().isEmpty();
    }

    /**
     * Business method to check if issue is blocked
     * 
     * @return true if issue is blocked
     */
    public boolean isBlocked() {
        return this.isBlocked != null && this.isBlocked;
    }

    /**
     * Business method to check if issue is confidential
     * 
     * @return true if issue is confidential
     */
    public boolean isConfidential() {
        return this.isConfidential != null && this.isConfidential;
    }

    /**
     * Business method to get formatted time estimate
     * 
     * @return formatted time estimate
     */
    public String getFormattedTimeEstimate() {
        if (this.timeEstimate == null) {
            return "Not estimated";
        }
        
        long hours = this.timeEstimate / 60;
        long minutes = this.timeEstimate % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    /**
     * Business method to get formatted time spent
     * 
     * @return formatted time spent
     */
    public String getFormattedTimeSpent() {
        if (this.timeSpent == null) {
            return "No time spent";
        }
        
        long hours = this.timeSpent / 60;
        long minutes = this.timeSpent % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    /**
     * Business method to get progress percentage
     * 
     * @return progress percentage
     */
    public double getProgressPercentage() {
        if (this.timeEstimate == null || this.timeEstimate == 0) {
            return 0.0;
        }
        
        if (this.timeSpent == null) {
            return 0.0;
        }
        
        return Math.min(100.0, (this.timeSpent.doubleValue() / this.timeEstimate.doubleValue()) * 100.0);
    }

    /**
     * Check if issue is deleted (soft delete)
     * 
     * @return true if issue is deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft delete issue
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = IssueStatus.CLOSED;
    }

    /**
     * Restore issue from soft delete
     */
    public void restore() {
        this.deletedAt = null;
        this.status = IssueStatus.OPEN;
    }

    /**
     * JPA lifecycle callback method invoked before persisting a new entity.
     * This can be used for setting default values or auditing.
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.version == null) {
            this.version = 0L;
        }
        if (this.attachmentCount == null) {
            this.attachmentCount = 0;
        }
        if (this.commentCount == null) {
            this.commentCount = 0;
        }
        if (this.watcherCount == null) {
            this.watcherCount = 0;
        }
        if (this.voteCount == null) {
            this.voteCount = 0;
        }
        if (this.isBlocked == null) {
            this.isBlocked = false;
        }
        if (this.isConfidential == null) {
            this.isConfidential = false;
        }
        if (this.isSubtask == null) {
            this.isSubtask = false;
        }
        if (this.isEpic == null) {
            this.isEpic = false;
        }
        if (this.isStory == null) {
            this.isStory = false;
        }
        if (this.isBug == null) {
            this.isBug = false;
        }
        if (this.isTask == null) {
            this.isTask = false;
        }
        if (this.isImprovement == null) {
            this.isImprovement = false;
        }
        if (this.isNewFeature == null) {
            this.isNewFeature = false;
        }
    }

    /**
     * JPA lifecycle callback method invoked before updating an existing entity.
     * This can be used for updating audit fields.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback method invoked after loading an entity from the database.
     * This can be used for post-processing or initializing transient fields.
     */
    @PostLoad
    public void postLoad() {
        // Example: Log issue load
        // logger.debug("Issue loaded: {}", this.id);
    }

    /**
     * Validation groups for different scenarios
     */
    public interface CreateValidation {}
    public interface UpdateValidation {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return Objects.equals(tenantId, issue.tenantId) && Objects.equals(key, issue.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, key);
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id='" + id + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", type=" + type +
                ", projectId='" + projectId + '\'' +
                ", assigneeId='" + assigneeId + '\'' +
                ", reporterId='" + reporterId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                ", deletedAt=" + deletedAt +
                '}';
    }

    /**
     * Issue Status Enumeration
     */
    public enum IssueStatus {
        OPEN,
        IN_PROGRESS,
        IN_REVIEW,
        RESOLVED,
        CLOSED,
        REOPENED,
        CANCELLED
    }

    /**
     * Issue Priority Enumeration
     */
    public enum IssuePriority {
        LOWEST,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Issue Type Enumeration
     */
    public enum IssueType {
        BUG,
        TASK,
        STORY,
        EPIC,
        IMPROVEMENT,
        NEW_FEATURE,
        SUBTASK
    }

    /**
     * Issue Resolution Enumeration
     */
    public enum IssueResolution {
        FIXED,
        WONTFIX,
        DUPLICATE,
        INVALID,
        WORKSFORME,
        CANNOTREPRODUCE
    }
}
