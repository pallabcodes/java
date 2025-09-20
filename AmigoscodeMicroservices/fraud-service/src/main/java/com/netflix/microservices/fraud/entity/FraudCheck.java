package com.netflix.microservices.fraud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Netflix Production-Grade Fraud Check Entity
 * 
 * This entity demonstrates Netflix production standards for fraud detection including:
 * 1. Comprehensive fraud check tracking
 * 2. Risk scoring and assessment
 * 3. Fraud pattern recognition
 * 4. Machine learning model results
 * 5. Audit trail for fraud investigations
 * 6. Performance optimization for high-throughput
 * 7. Caching for fast fraud checks
 * 8. Monitoring and alerting for fraud patterns
 * 
 * For C/C++ engineers:
 * - JPA entities are like database table mappings in C++ ORM libraries
 * - @Entity is like marking a class as a database table
 * - @Id is like primary key in database tables
 * - Validation annotations are like input validation in C++
 * - @CreationTimestamp is like automatic timestamp fields
 * - Fraud detection is like pattern matching algorithms in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "fraud_checks",
       indexes = {
           @Index(name = "idx_fraud_customer_id", columnList = "customerId"),
           @Index(name = "idx_fraud_transaction_id", columnList = "transactionId"),
           @Index(name = "idx_fraud_risk_score", columnList = "riskScore"),
           @Index(name = "idx_fraud_status", columnList = "status"),
           @Index(name = "idx_fraud_created_at", columnList = "createdAt"),
           @Index(name = "idx_fraud_deleted_at", columnList = "deletedAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_fraud_transaction_id", columnNames = "transactionId")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheck {

    @Id
    @GeneratedValue(generator = "fraud-check-uuid")
    @GenericGenerator(name = "fraud-check-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @NotBlank(message = "Customer ID is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 36, message = "Customer ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @NotBlank(message = "Transaction ID is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 36, message = "Transaction ID must not exceed 36 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "transaction_id", nullable = false, unique = true, length = 36)
    private String transactionId;

    @NotNull(message = "Risk score is required", groups = {CreateValidation.class})
    @DecimalMin(value = "0.0", message = "Risk score must be at least 0.0", 
                groups = {CreateValidation.class, UpdateValidation.class})
    @DecimalMax(value = "1.0", message = "Risk score must not exceed 1.0", 
                groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "risk_score", nullable = false, precision = 3, scale = 2)
    private Double riskScore;

    @NotNull(message = "Status is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FraudCheckStatus status;

    @Size(max = 1000, message = "Reason must not exceed 1000 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "reason", length = 1000)
    private String reason;

    @Size(max = 500, message = "Details must not exceed 500 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "details", length = 500)
    private String details;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_fingerprint", length = 100)
    private String deviceFingerprint;

    @Column(name = "location_country", length = 2)
    private String locationCountry;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "amount", precision = 15, scale = 2)
    private Double amount;

    @Column(name = "currency", length = 3)
    private String currency;

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
     * Business method to check if fraud check is high risk
     * 
     * @return true if risk score is above 0.7
     */
    public boolean isHighRisk() {
        return this.riskScore != null && this.riskScore > 0.7;
    }

    /**
     * Business method to check if fraud check is medium risk
     * 
     * @return true if risk score is between 0.3 and 0.7
     */
    public boolean isMediumRisk() {
        return this.riskScore != null && this.riskScore >= 0.3 && this.riskScore <= 0.7;
    }

    /**
     * Business method to check if fraud check is low risk
     * 
     * @return true if risk score is below 0.3
     */
    public boolean isLowRisk() {
        return this.riskScore != null && this.riskScore < 0.3;
    }

    /**
     * Business method to check if fraud check is approved
     * 
     * @return true if status is APPROVED
     */
    public boolean isApproved() {
        return this.status == FraudCheckStatus.APPROVED;
    }

    /**
     * Business method to check if fraud check is rejected
     * 
     * @return true if status is REJECTED
     */
    public boolean isRejected() {
        return this.status == FraudCheckStatus.REJECTED;
    }

    /**
     * Business method to check if fraud check requires manual review
     * 
     * @return true if status is MANUAL_REVIEW
     */
    public boolean requiresManualReview() {
        return this.status == FraudCheckStatus.MANUAL_REVIEW;
    }

    /**
     * Business method to get risk level description
     * 
     * @return risk level description
     */
    public String getRiskLevel() {
        if (isHighRisk()) {
            return "HIGH";
        } else if (isMediumRisk()) {
            return "MEDIUM";
        } else if (isLowRisk()) {
            return "LOW";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Business method to get formatted risk score
     * 
     * @return formatted risk score percentage
     */
    public String getFormattedRiskScore() {
        if (this.riskScore != null) {
            return String.format("%.1f%%", this.riskScore * 100);
        }
        return "N/A";
    }

    /**
     * Check if fraud check is deleted (soft delete)
     * 
     * @return true if fraud check is deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft delete fraud check
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = FraudCheckStatus.DELETED;
    }

    /**
     * Restore fraud check from soft delete
     */
    public void restore() {
        this.deletedAt = null;
        this.status = FraudCheckStatus.APPROVED;
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
        // Example: Log fraud check load
        // logger.debug("Fraud check loaded: {}", this.id);
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
        FraudCheck that = (FraudCheck) o;
        return Objects.equals(transactionId, that.transactionId); // Business key for equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId); // Hash based on business key
    }

    @Override
    public String toString() {
        return "FraudCheck{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", riskScore=" + riskScore +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                ", details='" + details + '\'' +
                ", modelVersion='" + modelVersion + '\'' +
                ", processingTimeMs=" + processingTimeMs +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", deviceFingerprint='" + deviceFingerprint + '\'' +
                ", locationCountry='" + locationCountry + '\'' +
                ", locationCity='" + locationCity + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                ", deletedAt=" + deletedAt +
                '}';
    }

    /**
     * Fraud Check Status Enumeration
     */
    public enum FraudCheckStatus {
        APPROVED,
        REJECTED,
        MANUAL_REVIEW,
        PENDING,
        ERROR,
        DELETED
    }
}
