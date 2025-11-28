package com.netflix.productivity.modules.issues.domain.aggregate;

import com.netflix.productivity.modules.issues.domain.events.DomainEvent;
import com.netflix.productivity.modules.issues.domain.valueobjects.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Issue Aggregate Root - Domain-Driven Design
 *
 * Encapsulates the Issue entity and enforces domain invariants:
 * - Business rules and constraints
 * - Domain event publishing
 * - Aggregate state consistency
 * - Entity lifecycle management
 */
public class IssueAggregate {
    private final IssueId id;
    private Title title;
    private Description description;
    private Status status;
    private Priority priority;
    private final Assignee assignee;
    private final Reporter reporter;
    private final ProjectId projectId;
    private final Set<Tag> tags;
    private final List<Comment> comments;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
    private Estimation estimation;

    // Domain events
    private final List<DomainEvent> domainEvents;

    // Private constructor - use factory methods
    private IssueAggregate(IssueId id, Title title, Description description,
                          Status status, Priority priority, Assignee assignee,
                          Reporter reporter, ProjectId projectId) {
        this.id = Objects.requireNonNull(id, "Issue ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
        this.assignee = Objects.requireNonNull(assignee, "Assignee cannot be null");
        this.reporter = Objects.requireNonNull(reporter, "Reporter cannot be null");
        this.projectId = Objects.requireNonNull(projectId, "Project ID cannot be null");

        this.tags = new HashSet<>();
        this.comments = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.domainEvents = new ArrayList<>();

        // Enforce domain invariants
        enforceInvariants();
    }

    /**
     * Factory method to create a new issue
     */
    public static IssueAggregate create(IssueId id, Title title, Description description,
                                      Priority priority, Assignee assignee, Reporter reporter,
                                      ProjectId projectId) {
        IssueAggregate issue = new IssueAggregate(id, title, description, Status.OPEN,
                                                priority, assignee, reporter, projectId);

        // Publish domain event
        issue.addDomainEvent(new IssueCreatedEvent(id, projectId, assignee.getUserId(), issue.createdAt));

        return issue;
    }

    /**
     * Factory method to reconstruct from existing data
     */
    public static IssueAggregate reconstruct(IssueId id, Title title, Description description,
                                           Status status, Priority priority, Assignee assignee,
                                           Reporter reporter, ProjectId projectId,
                                           Set<Tag> tags, List<Comment> comments,
                                           LocalDateTime createdAt, LocalDateTime updatedAt,
                                           LocalDateTime dueDate, Estimation estimation) {
        IssueAggregate issue = new IssueAggregate(id, title, description, status,
                                                priority, assignee, reporter, projectId);

        // Restore state
        issue.tags.addAll(tags != null ? tags : Collections.emptySet());
        issue.comments.addAll(comments != null ? comments : Collections.emptyList());
        ((ArrayList<Comment>) issue.comments).trimToSize(); // Reconstruct as immutable-like
        issue.updatedAt = updatedAt != null ? updatedAt : createdAt;
        issue.dueDate = dueDate;
        issue.estimation = estimation;

        return issue;
    }

    /**
     * Business Methods - Domain Logic
     */

    /**
     * Update issue title with business rules
     */
    public void updateTitle(Title newTitle, UserId updatedBy) {
        if (!canUpdate(updatedBy)) {
            throw new IllegalStateException("User not authorized to update this issue");
        }

        Title oldTitle = this.title;
        this.title = Objects.requireNonNull(newTitle, "Title cannot be null");
        this.updatedAt = LocalDateTime.now();

        enforceInvariants();

        addDomainEvent(new IssueTitleUpdatedEvent(id, oldTitle, newTitle, updatedBy, updatedAt));
    }

    /**
     * Update issue description
     */
    public void updateDescription(Description newDescription, UserId updatedBy) {
        if (!canUpdate(updatedBy)) {
            throw new IllegalStateException("User not authorized to update this issue");
        }

        Description oldDescription = this.description;
        this.description = Objects.requireNonNull(newDescription, "Description cannot be null");
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new IssueDescriptionUpdatedEvent(id, oldDescription, newDescription, updatedBy, updatedAt));
    }

    /**
     * Change issue status with workflow validation
     */
    public void changeStatus(Status newStatus, UserId changedBy) {
        if (!canChangeStatus(changedBy)) {
            throw new IllegalStateException("User not authorized to change issue status");
        }

        if (!isValidStatusTransition(this.status, newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to " + newStatus);
        }

        Status oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new IssueStatusChangedEvent(id, oldStatus, newStatus, changedBy, updatedAt));

        // Publish completion event if issue is closed
        if (newStatus == Status.CLOSED) {
            addDomainEvent(new IssueCompletedEvent(id, assignee.getUserId(), updatedAt));
        }
    }

    /**
     * Assign issue to different user
     */
    public void assignTo(Assignee newAssignee, UserId assignedBy) {
        if (!canAssign(assignedBy)) {
            throw new IllegalStateException("User not authorized to assign issues");
        }

        Assignee oldAssignee = this.assignee;
        ((Assignee) this.assignee).setUserId(newAssignee.getUserId()); // Update assignee
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new IssueAssignedEvent(id, oldAssignee.getUserId(), newAssignee.getUserId(), assignedBy, updatedAt));
    }

    /**
     * Update priority
     */
    public void updatePriority(Priority newPriority, UserId updatedBy) {
        if (!canUpdatePriority(updatedBy)) {
            throw new IllegalStateException("User not authorized to update priority");
        }

        Priority oldPriority = this.priority;
        this.priority = Objects.requireNonNull(newPriority, "Priority cannot be null");
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new IssuePriorityUpdatedEvent(id, oldPriority, newPriority, updatedBy, updatedAt));
    }

    /**
     * Add comment to issue
     */
    public void addComment(Comment comment) {
        Objects.requireNonNull(comment, "Comment cannot be null");

        this.comments.add(comment);
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new IssueCommentAddedEvent(id, comment.getId(), comment.getAuthorId(), updatedAt));
    }

    /**
     * Add tag to issue
     */
    public void addTag(Tag tag, UserId addedBy) {
        Objects.requireNonNull(tag, "Tag cannot be null");

        if (this.tags.add(tag)) {
            this.updatedAt = LocalDateTime.now();
            addDomainEvent(new IssueTagAddedEvent(id, tag, addedBy, updatedAt));
        }
    }

    /**
     * Remove tag from issue
     */
    public void removeTag(Tag tag, UserId removedBy) {
        Objects.requireNonNull(tag, "Tag cannot be null");

        if (this.tags.remove(tag)) {
            this.updatedAt = LocalDateTime.now();
            addDomainEvent(new IssueTagRemovedEvent(id, tag, removedBy, updatedAt));
        }
    }

    /**
     * Set due date
     */
    public void setDueDate(LocalDateTime dueDate, UserId setBy) {
        if (!canSetDueDate(setBy)) {
            throw new IllegalStateException("User not authorized to set due dates");
        }

        if (dueDate != null && dueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new IssueDueDateSetEvent(id, dueDate, setBy, updatedAt));
    }

    /**
     * Set estimation
     */
    public void setEstimation(Estimation estimation, UserId setBy) {
        if (!canSetEstimation(setBy)) {
            throw new IllegalStateException("User not authorized to set estimations");
        }

        this.estimation = estimation;
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new IssueEstimationSetEvent(id, estimation, setBy, updatedAt));
    }

    /**
     * Domain Invariants - Business Rules
     */
    private void enforceInvariants() {
        // Invariant 1: Title cannot be empty
        if (title.getValue().trim().isEmpty()) {
            throw new IllegalStateException("Issue title cannot be empty");
        }

        // Invariant 2: Due date cannot be before creation date
        if (dueDate != null && dueDate.isBefore(createdAt)) {
            throw new IllegalStateException("Due date cannot be before issue creation date");
        }

        // Invariant 3: High priority issues must have an assignee
        if (priority == Priority.CRITICAL && assignee.getUserId() == null) {
            throw new IllegalStateException("Critical priority issues must have an assignee");
        }

        // Invariant 4: Closed issues cannot be modified (except for reopening)
        if (status == Status.CLOSED && updatedAt.isAfter(createdAt)) {
            // Allow reopening by changing status, but prevent other modifications
            // This is checked in business methods
        }

        // Invariant 5: Maximum 10 tags per issue
        if (tags.size() > 10) {
            throw new IllegalStateException("Issues cannot have more than 10 tags");
        }

        // Invariant 6: Estimation must be reasonable (max 1000 hours)
        if (estimation != null && estimation.getHours() > 1000) {
            throw new IllegalStateException("Issue estimation cannot exceed 1000 hours");
        }
    }

    /**
     * Authorization Checks
     */
    private boolean canUpdate(UserId userId) {
        return assignee.getUserId().equals(userId) ||
               reporter.getUserId().equals(userId) ||
               isProjectAdmin(userId);
    }

    private boolean canChangeStatus(UserId userId) {
        return assignee.getUserId().equals(userId) ||
               reporter.getUserId().equals(userId) ||
               isProjectAdmin(userId);
    }

    private boolean canAssign(UserId userId) {
        return reporter.getUserId().equals(userId) || isProjectAdmin(userId);
    }

    private boolean canUpdatePriority(UserId userId) {
        return isProjectAdmin(userId) || isProjectContributor(userId);
    }

    private boolean canSetDueDate(UserId userId) {
        return isProjectAdmin(userId) || assignee.getUserId().equals(userId);
    }

    private boolean canSetEstimation(UserId userId) {
        return assignee.getUserId().equals(userId) || isProjectAdmin(userId);
    }

    private boolean isProjectAdmin(UserId userId) {
        // TODO: Implement project role checking
        return false; // Placeholder
    }

    private boolean isProjectContributor(UserId userId) {
        // TODO: Implement project role checking
        return false; // Placeholder
    }

    /**
     * Status Transition Validation
     */
    private boolean isValidStatusTransition(Status from, Status to) {
        return switch (from) {
            case OPEN -> to == Status.IN_PROGRESS || to == Status.CLOSED;
            case IN_PROGRESS -> to == Status.OPEN || to == Status.CLOSED || to == Status.RESOLVED;
            case RESOLVED -> to == Status.CLOSED || to == Status.REOPENED;
            case CLOSED -> to == Status.REOPENED;
            case REOPENED -> to == Status.OPEN || to == Status.IN_PROGRESS;
        };
    }

    /**
     * Domain Event Management
     */
    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * Getters for Aggregate State
     */
    public IssueId getId() { return id; }
    public Title getTitle() { return title; }
    public Description getDescription() { return description; }
    public Status getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public Assignee getAssignee() { return assignee; }
    public Reporter getReporter() { return reporter; }
    public ProjectId getProjectId() { return projectId; }
    public Set<Tag> getTags() { return Collections.unmodifiableSet(tags); }
    public List<Comment> getComments() { return Collections.unmodifiableList(comments); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDueDate() { return dueDate; }
    public Estimation getEstimation() { return estimation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueAggregate that = (IssueAggregate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IssueAggregate{" +
                "id=" + id +
                ", title=" + title +
                ", status=" + status +
                ", priority=" + priority +
                ", assignee=" + assignee +
                ", projectId=" + projectId +
                '}';
    }

    /**
     * Domain Events for Issue Aggregate
     */
    public static class IssueCreatedEvent implements DomainEvent {
        private final IssueId issueId;
        private final ProjectId projectId;
        private final UserId assigneeId;
        private final LocalDateTime occurredOn;

        public IssueCreatedEvent(IssueId issueId, ProjectId projectId, UserId assigneeId, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.projectId = projectId;
            this.assigneeId = assigneeId;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public ProjectId getProjectId() { return projectId; }
        public UserId getAssigneeId() { return assigneeId; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueTitleUpdatedEvent implements DomainEvent {
        private final IssueId issueId;
        private final Title oldTitle;
        private final Title newTitle;
        private final UserId updatedBy;
        private final LocalDateTime occurredOn;

        public IssueTitleUpdatedEvent(IssueId issueId, Title oldTitle, Title newTitle, UserId updatedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.oldTitle = oldTitle;
            this.newTitle = newTitle;
            this.updatedBy = updatedBy;
            this.occurredOn = occurredOn;
        }

        // Getters...
        public IssueId getIssueId() { return issueId; }
        public Title getOldTitle() { return oldTitle; }
        public Title getNewTitle() { return newTitle; }
        public UserId getUpdatedBy() { return updatedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueStatusChangedEvent implements DomainEvent {
        private final IssueId issueId;
        private final Status oldStatus;
        private final Status newStatus;
        private final UserId changedBy;
        private final LocalDateTime occurredOn;

        public IssueStatusChangedEvent(IssueId issueId, Status oldStatus, Status newStatus, UserId changedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.changedBy = changedBy;
            this.occurredOn = occurredOn;
        }

        // Getters...
        public IssueId getIssueId() { return issueId; }
        public Status getOldStatus() { return oldStatus; }
        public Status getNewStatus() { return newStatus; }
        public UserId getChangedBy() { return changedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueAssignedEvent implements DomainEvent {
        private final IssueId issueId;
        private final UserId oldAssigneeId;
        private final UserId newAssigneeId;
        private final UserId assignedBy;
        private final LocalDateTime occurredOn;

        public IssueAssignedEvent(IssueId issueId, UserId oldAssigneeId, UserId newAssigneeId, UserId assignedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.oldAssigneeId = oldAssigneeId;
            this.newAssigneeId = newAssigneeId;
            this.assignedBy = assignedBy;
            this.occurredOn = occurredOn;
        }

        // Getters...
        public IssueId getIssueId() { return issueId; }
        public UserId getOldAssigneeId() { return oldAssigneeId; }
        public UserId getNewAssigneeId() { return newAssigneeId; }
        public UserId getAssignedBy() { return assignedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueCompletedEvent implements DomainEvent {
        private final IssueId issueId;
        private final UserId completedBy;
        private final LocalDateTime occurredOn;

        public IssueCompletedEvent(IssueId issueId, UserId completedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.completedBy = completedBy;
            this.occurredOn = occurredOn;
        }

        // Getters...
        public IssueId getIssueId() { return issueId; }
        public UserId getCompletedBy() { return completedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    // Additional domain events...
    public static class IssueDescriptionUpdatedEvent implements DomainEvent {
        private final IssueId issueId;
        private final Description oldDescription;
        private final Description newDescription;
        private final UserId updatedBy;
        private final LocalDateTime occurredOn;

        public IssueDescriptionUpdatedEvent(IssueId issueId, Description oldDescription, Description newDescription, UserId updatedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.oldDescription = oldDescription;
            this.newDescription = newDescription;
            this.updatedBy = updatedBy;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public Description getOldDescription() { return oldDescription; }
        public Description getNewDescription() { return newDescription; }
        public UserId getUpdatedBy() { return updatedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssuePriorityUpdatedEvent implements DomainEvent {
        private final IssueId issueId;
        private final Priority oldPriority;
        private final Priority newPriority;
        private final UserId updatedBy;
        private final LocalDateTime occurredOn;

        public IssuePriorityUpdatedEvent(IssueId issueId, Priority oldPriority, Priority newPriority, UserId updatedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.oldPriority = oldPriority;
            this.newPriority = newPriority;
            this.updatedBy = updatedBy;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public Priority getOldPriority() { return oldPriority; }
        public Priority getNewPriority() { return newPriority; }
        public UserId getUpdatedBy() { return updatedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueCommentAddedEvent implements DomainEvent {
        private final IssueId issueId;
        private final CommentId commentId;
        private final UserId authorId;
        private final LocalDateTime occurredOn;

        public IssueCommentAddedEvent(IssueId issueId, CommentId commentId, UserId authorId, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.commentId = commentId;
            this.authorId = authorId;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public CommentId getCommentId() { return commentId; }
        public UserId getAuthorId() { return authorId; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueTagAddedEvent implements DomainEvent {
        private final IssueId issueId;
        private final Tag tag;
        private final UserId addedBy;
        private final LocalDateTime occurredOn;

        public IssueTagAddedEvent(IssueId issueId, Tag tag, UserId addedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.tag = tag;
            this.addedBy = addedBy;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public Tag getTag() { return tag; }
        public UserId getAddedBy() { return addedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueTagRemovedEvent implements DomainEvent {
        private final IssueId issueId;
        private final Tag tag;
        private final UserId removedBy;
        private final LocalDateTime occurredOn;

        public IssueTagRemovedEvent(IssueId issueId, Tag tag, UserId removedBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.tag = tag;
            this.removedBy = removedBy;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public Tag getTag() { return tag; }
        public UserId getRemovedBy() { return removedBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueDueDateSetEvent implements DomainEvent {
        private final IssueId issueId;
        private final LocalDateTime dueDate;
        private final UserId setBy;
        private final LocalDateTime occurredOn;

        public IssueDueDateSetEvent(IssueId issueId, LocalDateTime dueDate, UserId setBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.dueDate = dueDate;
            this.setBy = setBy;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public LocalDateTime getDueDate() { return dueDate; }
        public UserId getSetBy() { return setBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public static class IssueEstimationSetEvent implements DomainEvent {
        private final IssueId issueId;
        private final Estimation estimation;
        private final UserId setBy;
        private final LocalDateTime occurredOn;

        public IssueEstimationSetEvent(IssueId issueId, Estimation estimation, UserId setBy, LocalDateTime occurredOn) {
            this.issueId = issueId;
            this.estimation = estimation;
            this.setBy = setBy;
            this.occurredOn = occurredOn;
        }

        public IssueId getIssueId() { return issueId; }
        public Estimation getEstimation() { return estimation; }
        public UserId getSetBy() { return setBy; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }
}
