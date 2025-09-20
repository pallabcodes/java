package com.netflix.springframework.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * PaymentEntity - JPA Entity for Payment Management
 * 
 * This entity demonstrates Netflix production-grade payment entity implementation:
 * 1. Comprehensive payment data modeling
 * 2. Stripe integration with payment intent tracking
 * 3. Audit fields for payment lifecycle tracking
 * 4. Validation annotations for data integrity
 * 5. Business logic methods for payment operations
 * 6. Security considerations for sensitive payment data
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
@Table(name = "payments",
       indexes = {
           @Index(name = "idx_payment_stripe_id", columnList = "stripe_payment_intent_id"),
           @Index(name = "idx_payment_user_id", columnList = "user_id"),
           @Index(name = "idx_payment_status", columnList = "status"),
           @Index(name = "idx_payment_created_at", columnList = "created_at"),
           @Index(name = "idx_payment_amount", columnList = "amount")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_payment_stripe_id", columnNames = "stripe_payment_intent_id")
       })
public class PaymentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotBlank(message = "Stripe payment intent ID is required")
    @Size(max = 255, message = "Stripe payment intent ID must not exceed 255 characters")
    @Column(name = "stripe_payment_intent_id", nullable = false, unique = true, length = 255)
    private String stripePaymentIntentId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Amount must not exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Amount must have at most 6 integer digits and 2 decimal places")
    @Column(name = "amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase 3-letter code")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @Size(max = 1000, message = "Metadata must not exceed 1000 characters")
    @Column(name = "metadata", length = 1000)
    private String metadata;
    
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    @Email(message = "Customer email must be valid")
    @Column(name = "customer_email", length = 255)
    private String customerEmail;
    
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    @Column(name = "customer_name", length = 100)
    private String customerName;
    
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Size(max = 100, message = "Failure reason must not exceed 100 characters")
    @Column(name = "failure_reason", length = 100)
    private String failureReason;
    
    @Column(name = "refunded_amount", precision = 8, scale = 2)
    private BigDecimal refundedAmount;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Size(max = 255, message = "Refund reason must not exceed 255 characters")
    @Column(name = "refund_reason", length = 255)
    private String refundReason;
    
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
    public PaymentEntity() {
        this.status = PaymentStatus.PENDING;
        this.refundedAmount = BigDecimal.ZERO;
    }
    
    /**
     * Constructor with required fields
     * 
     * @param userId User ID
     * @param stripePaymentIntentId Stripe payment intent ID
     * @param amount Payment amount
     * @param currency Payment currency
     */
    public PaymentEntity(Long userId, String stripePaymentIntentId, BigDecimal amount, String currency) {
        this();
        this.userId = userId;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.amount = amount;
        this.currency = currency;
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
    
    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }
    
    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }
    
    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }
    
    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
    
    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
    
    public String getRefundReason() {
        return refundReason;
    }
    
    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
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
     * Business method to check if payment is successful
     * 
     * @return true if payment is successful
     */
    public boolean isSuccessful() {
        return PaymentStatus.SUCCEEDED.equals(this.status);
    }
    
    /**
     * Business method to check if payment is failed
     * 
     * @return true if payment is failed
     */
    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.status);
    }
    
    /**
     * Business method to check if payment is pending
     * 
     * @return true if payment is pending
     */
    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status);
    }
    
    /**
     * Business method to check if payment is refunded
     * 
     * @return true if payment is refunded
     */
    public boolean isRefunded() {
        return PaymentStatus.REFUNDED.equals(this.status);
    }
    
    /**
     * Business method to check if payment is partially refunded
     * 
     * @return true if payment is partially refunded
     */
    public boolean isPartiallyRefunded() {
        return this.refundedAmount != null && 
               this.refundedAmount.compareTo(BigDecimal.ZERO) > 0 && 
               this.refundedAmount.compareTo(this.amount) < 0;
    }
    
    /**
     * Business method to get remaining refundable amount
     * 
     * @return remaining refundable amount
     */
    public BigDecimal getRemainingRefundableAmount() {
        if (this.refundedAmount == null) {
            return this.amount;
        }
        return this.amount.subtract(this.refundedAmount);
    }
    
    /**
     * Business method to get formatted amount
     * 
     * @return formatted amount string
     */
    public String getFormattedAmount() {
        return String.format("%s %.2f", this.currency, this.amount);
    }
    
    /**
     * Business method to get display name
     * 
     * @return formatted display name
     */
    public String getDisplayName() {
        return String.format("Payment %s - %s", this.id, this.getFormattedAmount());
    }
    
    /**
     * Payment status enumeration
     */
    public enum PaymentStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        SUCCEEDED("Succeeded"),
        FAILED("Failed"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded"),
        PARTIALLY_REFUNDED("Partially Refunded");
        
        private final String displayName;
        
        PaymentStatus(String displayName) {
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
        
        PaymentEntity that = (PaymentEntity) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(stripePaymentIntentId, that.stripePaymentIntentId);
    }
    
    /**
     * hashCode method for entity hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, stripePaymentIntentId);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "PaymentEntity{" +
                "id=" + id +
                ", userId=" + userId +
                ", stripePaymentIntentId='" + stripePaymentIntentId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", failureReason='" + failureReason + '\'' +
                ", refundedAmount=" + refundedAmount +
                ", refundedAt=" + refundedAt +
                ", refundReason='" + refundReason + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
