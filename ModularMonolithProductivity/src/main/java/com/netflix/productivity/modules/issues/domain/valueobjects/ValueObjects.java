package com.netflix.productivity.modules.issues.domain.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Objects for Issue Domain - Domain-Driven Design
 *
 * Immutable value objects that represent domain concepts without identity.
 * Value objects should:
 * - Be immutable
 * - Have no side effects
 * - Be equal based on their properties
 * - Be self-validating
 * - Represent concepts from the domain
 */

/**
 * Issue ID - Unique identifier for issues
 */
public final class IssueId {
    private final String value;

    private IssueId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static IssueId of(String value) {
        return new IssueId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueId issueId = (IssueId) o;
        return Objects.equals(value, issueId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/**
 * Title - Issue title with validation
 */
public final class Title {
    private static final int MAX_LENGTH = 200;
    private final String value;

    private Title(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Title cannot exceed " + MAX_LENGTH + " characters");
        }
        this.value = value.trim();
    }

    public static Title of(String value) {
        return new Title(value);
    }

    public String getValue() {
        return value;
    }

    public int length() {
        return value.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Title title = (Title) o;
        return Objects.equals(value, title.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/**
 * Description - Issue description with optional formatting
 */
public final class Description {
    private static final int MAX_LENGTH = 5000;
    private final String value;
    private final boolean isHtml;

    private Description(String value, boolean isHtml) {
        if (value == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_LENGTH + " characters");
        }
        this.value = value;
        this.isHtml = isHtml;

        // Basic HTML validation if HTML is enabled
        if (isHtml && containsPotentialXss(value)) {
            throw new IllegalArgumentException("Description contains potentially unsafe HTML");
        }
    }

    public static Description plainText(String value) {
        return new Description(value, false);
    }

    public static Description html(String value) {
        return new Description(value, true);
    }

    public String getValue() {
        return value;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public boolean isEmpty() {
        return value.trim().isEmpty();
    }

    public int length() {
        return value.length();
    }

    private boolean containsPotentialXss(String content) {
        // Simple XSS detection - in production, use a proper HTML sanitizer
        String[] dangerousTags = {"<script", "<iframe", "<object", "<embed", "javascript:", "onload=", "onerror="};
        String lowerContent = content.toLowerCase();
        return dangerousTags.any { lowerContent.contains(it) };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Description that = (Description) o;
        return isHtml == that.isHtml && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isHtml);
    }

    @Override
    public String toString() {
        return value.length() > 50 ? value.substring(0, 47) + "..." : value;
    }
}

/**
 * Status - Issue workflow status
 */
public enum Status {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REOPENED;

    public boolean isActive() {
        return this == OPEN || this == IN_PROGRESS || this == REOPENED;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isResolved() {
        return this == RESOLVED;
    }
}

/**
 * Priority - Issue priority levels
 */
public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }

    public boolean isLowerThan(Priority other) {
        return this.level < other.level;
    }
}

/**
 * Assignee - Person assigned to work on the issue
 */
public final class Assignee {
    private UserId userId;

    private Assignee(UserId userId) {
        this.userId = Objects.requireNonNull(userId, "Assignee user ID cannot be null");
    }

    public static Assignee of(UserId userId) {
        return new Assignee(userId);
    }

    public UserId getUserId() {
        return userId;
    }

    // Note: This setter is for aggregate internal use only
    // In a real DDD implementation, this might be handled differently
    void setUserId(UserId userId) {
        this.userId = Objects.requireNonNull(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignee assignee = (Assignee) o;
        return Objects.equals(userId, assignee.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "Assignee{" + userId + '}';
    }
}

/**
 * Reporter - Person who reported the issue
 */
public final class Reporter {
    private final UserId userId;

    private Reporter(UserId userId) {
        this.userId = Objects.requireNonNull(userId, "Reporter user ID cannot be null");
    }

    public static Reporter of(UserId userId) {
        return new Reporter(userId);
    }

    public UserId getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reporter reporter = (Reporter) o;
        return Objects.equals(userId, reporter.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "Reporter{" + userId + '}';
    }
}

/**
 * Project ID - Reference to the project this issue belongs to
 */
public final class ProjectId {
    private final String value;

    private ProjectId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static ProjectId of(String value) {
        return new ProjectId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectId projectId = (ProjectId) o;
        return Objects.equals(value, projectId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/**
 * User ID - Unique identifier for users
 */
public final class UserId {
    private final String value;

    private UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/**
 * Comment ID - Unique identifier for comments
 */
public final class CommentId {
    private final String value;

    private CommentId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static CommentId of(String value) {
        return new CommentId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentId commentId = (CommentId) o;
        return Objects.equals(value, commentId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/**
 * Tag - Label for categorizing issues
 */
public final class Tag {
    private static final int MAX_LENGTH = 50;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final String value;

    private Tag(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Tag cannot exceed " + MAX_LENGTH + " characters");
        }
        if (!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Tag can only contain letters, numbers, hyphens, and underscores");
        }
        this.value = trimmed.toLowerCase();
    }

    public static Tag of(String value) {
        return new Tag(value);
    }

    public String getValue() {
        return value;
    }

    public int length() {
        return value.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(value, tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/**
 * Comment - User comment on an issue
 */
public final class Comment {
    private static final int MAX_LENGTH = 2000;
    private final CommentId id;
    private final UserId authorId;
    private final String content;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Comment(CommentId id, UserId authorId, String content, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "Comment ID cannot be null");
        this.authorId = Objects.requireNonNull(authorId, "Author ID cannot be null");

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be null or empty");
        }
        if (content.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Comment cannot exceed " + MAX_LENGTH + " characters");
        }
        this.content = content.trim();

        this.createdAt = Objects.requireNonNull(createdAt, "Creation time cannot be null");
        this.updatedAt = createdAt;
    }

    public static Comment create(CommentId id, UserId authorId, String content) {
        return new Comment(id, authorId, content, LocalDateTime.now());
    }

    public static Comment reconstruct(CommentId id, UserId authorId, String content,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        Comment comment = new Comment(id, authorId, content, createdAt);
        comment.updatedAt = updatedAt != null ? updatedAt : createdAt;
        return comment;
    }

    public CommentId getId() {
        return id;
    }

    public UserId getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean canEdit(UserId userId) {
        return authorId.equals(userId);
    }

    public Comment updateContent(String newContent, UserId updatedBy) {
        if (!canEdit(updatedBy)) {
            throw new IllegalStateException("User not authorized to edit this comment");
        }
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be null or empty");
        }
        if (newContent.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Comment cannot exceed " + MAX_LENGTH + " characters");
        }

        Comment updated = new Comment(id, authorId, newContent.trim(), createdAt);
        updated.updatedAt = LocalDateTime.now();
        return updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", authorId=" + authorId +
                ", createdAt=" + createdAt +
                ", content='" + content.substring(0, Math.min(content.length(), 50)) + "...'" +
                '}';
    }
}

/**
 * Estimation - Time estimation for issue completion
 */
public final class Estimation {
    private static final int MAX_HOURS = 1000;
    private static final int MAX_MINUTES = 59;

    private final int hours;
    private final int minutes;

    private Estimation(int hours, int minutes) {
        if (hours < 0 || hours > MAX_HOURS) {
            throw new IllegalArgumentException("Hours must be between 0 and " + MAX_HOURS);
        }
        if (minutes < 0 || minutes > MAX_MINUTES) {
            throw new IllegalArgumentException("Minutes must be between 0 and " + MAX_MINUTES);
        }
        this.hours = hours;
        this.minutes = minutes;
    }

    public static Estimation of(int hours, int minutes) {
        return new Estimation(hours, minutes);
    }

    public static Estimation ofHours(int hours) {
        return new Estimation(hours, 0);
    }

    public static Estimation ofMinutes(int minutes) {
        return new Estimation(0, minutes);
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getTotalMinutes() {
        return hours * 60 + minutes;
    }

    public Estimation add(Estimation other) {
        int totalMinutes = this.getTotalMinutes() + other.getTotalMinutes();
        return of(totalMinutes / 60, totalMinutes % 60);
    }

    public Estimation subtract(Estimation other) {
        int totalMinutes = Math.max(0, this.getTotalMinutes() - other.getTotalMinutes());
        return of(totalMinutes / 60, totalMinutes % 60);
    }

    public boolean isGreaterThan(Estimation other) {
        return this.getTotalMinutes() > other.getTotalMinutes();
    }

    public boolean isLessThan(Estimation other) {
        return this.getTotalMinutes() < other.getTotalMinutes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estimation that = (Estimation) o;
        return hours == that.hours && minutes == that.minutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hours, minutes);
    }

    @Override
    public String toString() {
        if (hours == 0) {
            return minutes + "m";
        } else if (minutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + minutes + "m";
        }
    }
}
