package com.netflix.springframework.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * SubscriptionEntity - JPA Entity for Subscription Management
 * 
 * This entity demonstrates Netflix production-grade subscription entity implementation:
 * 1. Comprehensive subscription data modeling
 * 2. Stripe integration with subscription tracking
 * 3. Audit fields for subscription lifecycle tracking
 * 4. Validation annotations for data integrity
 * 5. Business logic methods for subscription operations
 * 6. Billing cycle and payment management
 * 
 * For C/C++ engineers:
 * - JPA entities are like database table mappings in C++ ORM libraries
 * - @Entity is like marking a class as a database table
 * - @Id is like primary key in database tables
 * - Validation annotations are like input validation in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "subscriptions",
       indexes = {
           @Index(name = "idx_subscription_stripe_id", columnList = "stripe_subscription_id"),
           @Index(name = "idx_subscription_user_id", columnList = "user_id"),
           @Index(name = "idx_subscription_status", columnList = "status"),
           @Index(name = "idx_subscription_created_at", columnList = "created_at"),
           @Index(name = "idx_subscription_current_period_end", columnList = "current_period_end")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_subscription_stripe_id", columnNames = "stripe_subscription_id")
       })
public class SubscriptionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotBlank(message = "Stripe subscription ID is required")
    @Size(max = 255, message = "Stripe subscription ID must not exceed 255 characters")
    @Column(name = "stripe_subscription_id", nullable = false, unique = true, length = 255)
    private String stripeSubscriptionId;
    
    @NotBlank(message = "Price ID is required")
    @Size(max = 255, message = "Price ID must not exceed 255 characters")
    @Column(name = "price_id", nullable = false, length = 255)
    private String priceId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;
    
    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;
    
    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;
    
    @Column(name = "cancel_at_period_end")
    private Boolean cancelAtPeriodEnd = false;
    
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    
    @Column(name = "trial_start")
    private LocalDateTime trialStart;
    
    @Column(name = "trial_end")
    private LocalDateTime trialEnd;
    
    @Size(max = 1000, message = "Metadata must not exceed 1000 characters")
    @Column(name = "metadata", length = 1000)
    private String metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    /**
     * Default constructor
     * 
     * Required by JPA specification
     */
    public SubscriptionEntity() {
        this.status = SubscriptionStatus.INCOMPLETE;
    }
    
    /**
     * Constructor with required fields
     * 
     * @param userId User ID
     * @param stripeSubscriptionId Stripe subscription ID
     * @param priceId Price ID
     */
    public SubscriptionEntity(Long userId, String stripeSubscriptionId, String priceId) {
        this();
        this.userId = userId;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.priceId = priceId;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }
    
    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }
    
    public String getPriceId() {
        return priceId;
    }
    
    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }
    
    public SubscriptionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCurrentPeriodStart() {
        return currentPeriodStart;
    }
    
    public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }
    
    public LocalDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }
    
    public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }
    
    public Boolean getCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }
    
    public void setCancelAtPeriodEnd(Boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }
    
    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }
    
    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }
    
    public LocalDateTime getTrialStart() {
        return trialStart;
    }
    
    public void setTrialStart(LocalDateTime trialStart) {
        this.trialStart = trialStart;
    }
    
    public LocalDateTime getTrialEnd() {
        return trialEnd;
    }
    
    public void setTrialEnd(LocalDateTime trialEnd) {
        this.trialEnd = trialEnd;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    /**
     * Business method to check if subscription is active
     * 
     * @return true if subscription is active
     */
    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * Business method to check if subscription is cancelled
     * 
     * @return true if subscription is cancelled
     */
    public boolean isCancelled() {
        return SubscriptionStatus.CANCELLED.equals(this.status);
    }
    
    /**
     * Business method to check if subscription is in trial
     * 
     * @return true if subscription is in trial
     */
    public boolean isInTrial() {
        return SubscriptionStatus.TRIALING.equals(this.status);
    }
    
    /**
     * Business method to check if subscription is past due
     * 
     * @return true if subscription is past due
     */
    public boolean isPastDue() {
        return SubscriptionStatus.PAST_DUE.equals(this.status);
    }
    
    /**
     * Business method to check if subscription is incomplete
     * 
     * @return true if subscription is incomplete
     */
    public boolean isIncomplete() {
        return SubscriptionStatus.INCOMPLETE.equals(this.status);
    }
    
    /**
     * Business method to check if subscription is unpaid
     * 
     * @return true if subscription is unpaid
     */
    public boolean isUnpaid() {
        return SubscriptionStatus.UNPAID.equals(this.status);
    }
    
    /**
     * Business method to check if subscription is expired
     * 
     * @return true if subscription is expired
     */
    public boolean isExpired() {
        return SubscriptionStatus.INCOMPLETE_EXPIRED.equals(this.status);
    }
    
    /**
     * Business method to check if subscription is in trial period
     * 
     * @return true if subscription is in trial period
     */
    public boolean isInTrialPeriod() {
        if (this.trialStart == null || this.trialEnd == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.trialStart) && now.isBefore(this.trialEnd);
    }
    
    /**
     * Business method to check if subscription is in current period
     * 
     * @return true if subscription is in current period
     */
    public boolean isInCurrentPeriod() {
        if (this.currentPeriodStart == null || this.currentPeriodEnd == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.currentPeriodStart) && now.isBefore(this.currentPeriodEnd);
    }
    
    /**
     * Business method to check if subscription will be cancelled at period end
     * 
     * @return true if subscription will be cancelled at period end
     */
    public boolean willBeCancelledAtPeriodEnd() {
        return Boolean.TRUE.equals(this.cancelAtPeriodEnd);
    }
    
    /**
     * Business method to get days until period end
     * 
     * @return days until period end
     */
    public long getDaysUntilPeriodEnd() {
        if (this.currentPeriodEnd == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.currentPeriodEnd)) {
            return 0;
        }
        
        return java.time.Duration.between(now, this.currentPeriodEnd).toDays();
    }
    
    /**
     * Business method to get days until trial end
     * 
     * @return days until trial end
     */
    public long getDaysUntilTrialEnd() {
        if (this.trialEnd == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.trialEnd)) {
            return 0;
        }
        
        return java.time.Duration.between(now, this.trialEnd).toDays();
    }
    
    /**
     * Business method to get subscription display name
     * 
     * @return formatted display name
     */
    public String getDisplayName() {
        return String.format("Subscription %s - %s", this.id, this.status.getDisplayName());
    }
    
    /**
     * Business method to cancel subscription
     */
    public void cancel() {
        this.cancelAtPeriodEnd = true;
        this.canceledAt = LocalDateTime.now();
        this.status = SubscriptionStatus.CANCELLED;
    }
    
    /**
     * Business method to reactivate subscription
     */
    public void reactivate() {
        this.cancelAtPeriodEnd = false;
        this.canceledAt = null;
        this.status = SubscriptionStatus.ACTIVE;
    }
    
    /**
     * Subscription status enumeration
     */
    public enum SubscriptionStatus {
        INCOMPLETE("Incomplete"),
        INCOMPLETE_EXPIRED("Incomplete Expired"),
        TRIALING("Trialing"),
        ACTIVE("Active"),
        PAST_DUE("Past Due"),
        CANCELED("Canceled"),
        UNPAID("Unpaid");
        
        private final String displayName;
        
        SubscriptionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * equals method for entity comparison
     * 
     * @param obj Object to compare
     * @return true if objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SubscriptionEntity that = (SubscriptionEntity) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(stripeSubscriptionId, that.stripeSubscriptionId);
    }
    
    /**
     * hashCode method for entity hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, stripeSubscriptionId);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "id=" + id +
                ", userId=" + userId +
                ", stripeSubscriptionId='" + stripeSubscriptionId + '\'' +
                ", priceId='" + priceId + '\'' +
                ", status=" + status +
                ", currentPeriodStart=" + currentPeriodStart +
                ", currentPeriodEnd=" + currentPeriodEnd +
                ", cancelAtPeriodEnd=" + cancelAtPeriodEnd +
                ", canceledAt=" + canceledAt +
                ", trialStart=" + trialStart +
                ", trialEnd=" + trialEnd +
                ", metadata='" + metadata + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
