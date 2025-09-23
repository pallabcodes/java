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
 * Netflix Production-Grade Project Entity
 * 
 * This entity demonstrates Netflix production standards for project management including:
 * 1. Comprehensive project management and tracking
 * 2. Multi-tenant data isolation
 * 3. Project lifecycle management
 * 4. Team and member management
 * 5. Project configuration and settings
 * 6. Audit trail for project changes
 * 7. Performance optimization for high-throughput
 * 8. Caching for fast project retrieval
 * 
 * For C/C++ engineers:
 * - JPA entities are like database table mappings in C++ ORM libraries
 * - @Entity is like marking a class as a database table
 * - @Id is like primary key in database tables
 * - Validation annotations are like input validation in C++
 * - @CreationTimestamp is like automatic timestamp fields
 * - Project management is like workspace management in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Table(name = "projects",
       indexes = {
           @Index(name = "idx_project_tenant_id", columnList = "tenantId"),
           @Index(name = "idx_project_key", columnList = "key"),
           @Index(name = "idx_project_owner_id", columnList = "ownerId"),
           @Index(name = "idx_project_status", columnList = "status"),
           @Index(name = "idx_project_type", columnList = "type"),
           @Index(name = "idx_project_created_at", columnList = "createdAt"),
           @Index(name = "idx_project_updated_at", columnList = "updatedAt"),
           @Index(name = "idx_project_deleted_at", columnList = "deletedAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_project_tenant_key", columnNames = {"tenantId", "key"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(generator = "project-uuid")
    @GenericGenerator(name = "project-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @NotBlank(message = "Tenant ID is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 50, message = "Tenant ID must not exceed 50 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotBlank(message = "Project key is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 10, message = "Project key must not exceed 10 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "key", nullable = false, length = 10)
    private String key;

    @NotBlank(message = "Name is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 255, message = "Name must not exceed 255 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "Status is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status;

    @NotNull(message = "Type is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ProjectType type;

    @NotBlank(message = "Owner ID is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 36, message = "Owner ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Size(max = 100, message = "Lead ID must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "lead_id", length = 100)
    private String leadId;

    @Size(max = 100, message = "Manager ID must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "manager_id", length = 100)
    private String managerId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDateTime plannedEndDate;

    @Column(name = "budget")
    private Double budget;

    @Size(max = 3, message = "Currency must not exceed 3 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "progress_percentage")
    private Double progressPercentage;

    @Column(name = "story_points_total")
    private Integer storyPointsTotal;

    @Column(name = "story_points_completed")
    private Integer storyPointsCompleted;

    @Column(name = "story_points_remaining")
    private Integer storyPointsRemaining;

    @Column(name = "time_estimate_total")
    private Long timeEstimateTotal; // in minutes

    @Column(name = "time_spent_total")
    private Long timeSpentTotal; // in minutes

    @Column(name = "time_remaining_total")
    private Long timeRemainingTotal; // in minutes

    @Column(name = "issue_count_total")
    private Integer issueCountTotal;

    @Column(name = "issue_count_open")
    private Integer issueCountOpen;

    @Column(name = "issue_count_in_progress")
    private Integer issueCountInProgress;

    @Column(name = "issue_count_resolved")
    private Integer issueCountResolved;

    @Column(name = "issue_count_closed")
    private Integer issueCountClosed;

    @Column(name = "bug_count_total")
    private Integer bugCountTotal;

    @Column(name = "bug_count_open")
    private Integer bugCountOpen;

    @Column(name = "bug_count_resolved")
    private Integer bugCountResolved;

    @Column(name = "bug_count_closed")
    private Integer bugCountClosed;

    @Column(name = "task_count_total")
    private Integer taskCountTotal;

    @Column(name = "task_count_open")
    private Integer taskCountOpen;

    @Column(name = "task_count_in_progress")
    private Integer taskCountInProgress;

    @Column(name = "task_count_resolved")
    private Integer taskCountResolved;

    @Column(name = "task_count_closed")
    private Integer taskCountClosed;

    @Column(name = "story_count_total")
    private Integer storyCountTotal;

    @Column(name = "story_count_open")
    private Integer storyCountOpen;

    @Column(name = "story_count_in_progress")
    private Integer storyCountInProgress;

    @Column(name = "story_count_resolved")
    private Integer storyCountResolved;

    @Column(name = "story_count_closed")
    private Integer storyCountClosed;

    @Column(name = "epic_count_total")
    private Integer epicCountTotal;

    @Column(name = "epic_count_open")
    private Integer epicCountOpen;

    @Column(name = "epic_count_in_progress")
    private Integer epicCountInProgress;

    @Column(name = "epic_count_resolved")
    private Integer epicCountResolved;

    @Column(name = "epic_count_closed")
    private Integer epicCountClosed;

    @Column(name = "sprint_count_total")
    private Integer sprintCountTotal;

    @Column(name = "sprint_count_active")
    private Integer sprintCountActive;

    @Column(name = "sprint_count_completed")
    private Integer sprintCountCompleted;

    @Column(name = "member_count")
    private Integer memberCount;

    @Column(name = "watcher_count")
    private Integer watcherCount;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Column(name = "is_template")
    private Boolean isTemplate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Size(max = 500, message = "Tags must not exceed 500 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated tags

    @Size(max = 500, message = "Categories must not exceed 500 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "categories", length = 500)
    private String categories; // Comma-separated categories

    @Size(max = 500, message = "Labels must not exceed 500 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "labels", length = 500)
    private String labels; // Comma-separated labels

    @Size(max = 100, message = "Icon URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "icon_url", length = 100)
    private String iconUrl;

    @Size(max = 100, message = "Avatar URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "avatar_url", length = 100)
    private String avatarUrl;

    @Size(max = 100, message = "Banner URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "banner_url", length = 100)
    private String bannerUrl;

    @Size(max = 100, message = "Website URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "website_url", length = 100)
    private String websiteUrl;

    @Size(max = 100, message = "Repository URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "repository_url", length = 100)
    private String repositoryUrl;

    @Size(max = 100, message = "Documentation URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "documentation_url", length = 100)
    private String documentationUrl;

    @Size(max = 100, message = "Support URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "support_url", length = 100)
    private String supportUrl;

    @Size(max = 100, message = "Wiki URL must not exceed 100 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "wiki_url", length = 100)
    private String wikiUrl;

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
     * Business method to check if project is active
     * 
     * @return true if project is active
     */
    public boolean isActive() {
        return this.isActive != null && this.isActive && !isArchived();
    }

    /**
     * Business method to check if project is archived
     * 
     * @return true if project is archived
     */
    public boolean isArchived() {
        return this.isArchived != null && this.isArchived;
    }

    /**
     * Business method to check if project is public
     * 
     * @return true if project is public
     */
    public boolean isPublic() {
        return this.isPublic != null && this.isPublic;
    }

    /**
     * Business method to check if project is template
     * 
     * @return true if project is template
     */
    public boolean isTemplate() {
        return this.isTemplate != null && this.isTemplate;
    }

    /**
     * Business method to check if project is featured
     * 
     * @return true if project is featured
     */
    public boolean isFeatured() {
        return this.isFeatured != null && this.isFeatured;
    }

    /**
     * Business method to check if project is pinned
     * 
     * @return true if project is pinned
     */
    public boolean isPinned() {
        return this.isPinned != null && this.isPinned;
    }

    /**
     * Business method to check if project is favorite
     * 
     * @return true if project is favorite
     */
    public boolean isFavorite() {
        return this.isFavorite != null && this.isFavorite;
    }

    /**
     * Business method to check if project is overdue
     * 
     * @return true if project is overdue
     */
    public boolean isOverdue() {
        return this.endDate != null && this.endDate.isBefore(LocalDateTime.now()) && !isCompleted();
    }

    /**
     * Business method to check if project is completed
     * 
     * @return true if project is completed
     */
    public boolean isCompleted() {
        return this.status == ProjectStatus.COMPLETED;
    }

    /**
     * Business method to check if project is in progress
     * 
     * @return true if project is in progress
     */
    public boolean isInProgress() {
        return this.status == ProjectStatus.IN_PROGRESS;
    }

    /**
     * Business method to check if project is planned
     * 
     * @return true if project is planned
     */
    public boolean isPlanned() {
        return this.status == ProjectStatus.PLANNED;
    }

    /**
     * Business method to check if project is on hold
     * 
     * @return true if project is on hold
     */
    public boolean isOnHold() {
        return this.status == ProjectStatus.ON_HOLD;
    }

    /**
     * Business method to check if project is cancelled
     * 
     * @return true if project is cancelled
     */
    public boolean isCancelled() {
        return this.status == ProjectStatus.CANCELLED;
    }

    /**
     * Business method to get formatted budget
     * 
     * @return formatted budget
     */
    public String getFormattedBudget() {
        if (this.budget == null) {
            return "No budget set";
        }
        
        String currencySymbol = getCurrencySymbol();
        return String.format("%s%.2f", currencySymbol, this.budget);
    }

    /**
     * Business method to get currency symbol
     * 
     * @return currency symbol
     */
    private String getCurrencySymbol() {
        if (this.currency == null) {
            return "$";
        }
        
        switch (this.currency.toUpperCase()) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CAD": return "C$";
            case "AUD": return "A$";
            default: return this.currency + " ";
        }
    }

    /**
     * Business method to get formatted time estimate
     * 
     * @return formatted time estimate
     */
    public String getFormattedTimeEstimate() {
        if (this.timeEstimateTotal == null) {
            return "Not estimated";
        }
        
        long hours = this.timeEstimateTotal / 60;
        long days = hours / 8;
        hours = hours % 8;
        
        if (days > 0) {
            return String.format("%dd %dh", days, hours);
        } else {
            return String.format("%dh", hours);
        }
    }

    /**
     * Business method to get formatted time spent
     * 
     * @return formatted time spent
     */
    public String getFormattedTimeSpent() {
        if (this.timeSpentTotal == null) {
            return "No time spent";
        }
        
        long hours = this.timeSpentTotal / 60;
        long days = hours / 8;
        hours = hours % 8;
        
        if (days > 0) {
            return String.format("%dd %dh", days, hours);
        } else {
            return String.format("%dh", hours);
        }
    }

    /**
     * Business method to get progress percentage
     * 
     * @return progress percentage
     */
    public double getProgressPercentage() {
        if (this.progressPercentage != null) {
            return this.progressPercentage;
        }
        
        if (this.storyPointsTotal == null || this.storyPointsTotal == 0) {
            return 0.0;
        }
        
        if (this.storyPointsCompleted == null) {
            return 0.0;
        }
        
        return Math.min(100.0, (this.storyPointsCompleted.doubleValue() / this.storyPointsTotal.doubleValue()) * 100.0);
    }

    /**
     * Business method to get completion rate
     * 
     * @return completion rate
     */
    public double getCompletionRate() {
        if (this.issueCountTotal == null || this.issueCountTotal == 0) {
            return 0.0;
        }
        
        int completed = (this.issueCountResolved != null ? this.issueCountResolved : 0) + 
                       (this.issueCountClosed != null ? this.issueCountClosed : 0);
        
        return Math.min(100.0, (completed / (double) this.issueCountTotal) * 100.0);
    }

    /**
     * Business method to get bug resolution rate
     * 
     * @return bug resolution rate
     */
    public double getBugResolutionRate() {
        if (this.bugCountTotal == null || this.bugCountTotal == 0) {
            return 0.0;
        }
        
        int resolved = (this.bugCountResolved != null ? this.bugCountResolved : 0) + 
                      (this.bugCountClosed != null ? this.bugCountClosed : 0);
        
        return Math.min(100.0, (resolved / (double) this.bugCountTotal) * 100.0);
    }

    /**
     * Check if project is deleted (soft delete)
     * 
     * @return true if project is deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft delete project
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ProjectStatus.CANCELLED;
        this.isActive = false;
    }

    /**
     * Restore project from soft delete
     */
    public void restore() {
        this.deletedAt = null;
        this.status = ProjectStatus.PLANNED;
        this.isActive = true;
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
        if (this.progressPercentage == null) {
            this.progressPercentage = 0.0;
        }
        if (this.storyPointsTotal == null) {
            this.storyPointsTotal = 0;
        }
        if (this.storyPointsCompleted == null) {
            this.storyPointsCompleted = 0;
        }
        if (this.storyPointsRemaining == null) {
            this.storyPointsRemaining = 0;
        }
        if (this.timeEstimateTotal == null) {
            this.timeEstimateTotal = 0L;
        }
        if (this.timeSpentTotal == null) {
            this.timeSpentTotal = 0L;
        }
        if (this.timeRemainingTotal == null) {
            this.timeRemainingTotal = 0L;
        }
        if (this.issueCountTotal == null) {
            this.issueCountTotal = 0;
        }
        if (this.issueCountOpen == null) {
            this.issueCountOpen = 0;
        }
        if (this.issueCountInProgress == null) {
            this.issueCountInProgress = 0;
        }
        if (this.issueCountResolved == null) {
            this.issueCountResolved = 0;
        }
        if (this.issueCountClosed == null) {
            this.issueCountClosed = 0;
        }
        if (this.bugCountTotal == null) {
            this.bugCountTotal = 0;
        }
        if (this.bugCountOpen == null) {
            this.bugCountOpen = 0;
        }
        if (this.bugCountResolved == null) {
            this.bugCountResolved = 0;
        }
        if (this.bugCountClosed == null) {
            this.bugCountClosed = 0;
        }
        if (this.taskCountTotal == null) {
            this.taskCountTotal = 0;
        }
        if (this.taskCountOpen == null) {
            this.taskCountOpen = 0;
        }
        if (this.taskCountInProgress == null) {
            this.taskCountInProgress = 0;
        }
        if (this.taskCountResolved == null) {
            this.taskCountResolved = 0;
        }
        if (this.taskCountClosed == null) {
            this.taskCountClosed = 0;
        }
        if (this.storyCountTotal == null) {
            this.storyCountTotal = 0;
        }
        if (this.storyCountOpen == null) {
            this.storyCountOpen = 0;
        }
        if (this.storyCountInProgress == null) {
            this.storyCountInProgress = 0;
        }
        if (this.storyCountResolved == null) {
            this.storyCountResolved = 0;
        }
        if (this.storyCountClosed == null) {
            this.storyCountClosed = 0;
        }
        if (this.epicCountTotal == null) {
            this.epicCountTotal = 0;
        }
        if (this.epicCountOpen == null) {
            this.epicCountOpen = 0;
        }
        if (this.epicCountInProgress == null) {
            this.epicCountInProgress = 0;
        }
        if (this.epicCountResolved == null) {
            this.epicCountResolved = 0;
        }
        if (this.epicCountClosed == null) {
            this.epicCountClosed = 0;
        }
        if (this.sprintCountTotal == null) {
            this.sprintCountTotal = 0;
        }
        if (this.sprintCountActive == null) {
            this.sprintCountActive = 0;
        }
        if (this.sprintCountCompleted == null) {
            this.sprintCountCompleted = 0;
        }
        if (this.memberCount == null) {
            this.memberCount = 0;
        }
        if (this.watcherCount == null) {
            this.watcherCount = 0;
        }
        if (this.isPublic == null) {
            this.isPublic = false;
        }
        if (this.isArchived == null) {
            this.isArchived = false;
        }
        if (this.isTemplate == null) {
            this.isTemplate = false;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isFeatured == null) {
            this.isFeatured = false;
        }
        if (this.isPinned == null) {
            this.isPinned = false;
        }
        if (this.isFavorite == null) {
            this.isFavorite = false;
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
        // Example: Log project load
        // logger.debug("Project loaded: {}", this.id);
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
        Project project = (Project) o;
        return Objects.equals(tenantId, project.tenantId) && Objects.equals(key, project.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, key);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", ownerId='" + ownerId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                ", deletedAt=" + deletedAt +
                '}';
    }

    /**
     * Project Status Enumeration
     */
    public enum ProjectStatus {
        PLANNED,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        CANCELLED
    }

    /**
     * Project Type Enumeration
     */
    public enum ProjectType {
        SOFTWARE,
        HARDWARE,
        RESEARCH,
        MARKETING,
        SALES,
        SUPPORT,
        TRAINING,
        CONSULTING,
        OTHER
    }
}
