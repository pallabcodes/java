package com.netflix.microservices.customer.entity;

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
 * Netflix Production-Grade Customer Entity
 * 
 * This entity demonstrates Netflix production standards for JPA entities including:
 * 1. Comprehensive Bean Validation annotations
 * 2. JPA entity mapping with proper constraints
 * 3. Audit fields for creation and update tracking
 * 4. Custom validation groups for different scenarios
 * 5. Database-level constraints and indexes
 * 6. Soft Delete mechanism
 * 7. Optimistic locking with @Version
 * 8. Entity lifecycle callbacks
 * 
 * For C/C++ engineers:
 * - JPA entities are like database table mappings in C++ ORM libraries
 * - @Entity is like marking a class as a database table
 * - @Id is like primary key in database tables
 * - Validation annotations are like input validation in C++
 * - @CreationTimestamp is like automatic timestamp fields
 * - Soft delete is a common pattern to logically delete data instead of physically removing it
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "customers",
       indexes = {
           @Index(name = "idx_customer_email", columnList = "email"),
           @Index(name = "idx_customer_first_name", columnList = "firstName"),
           @Index(name = "idx_customer_last_name", columnList = "lastName"),
           @Index(name = "idx_customer_created_at", columnList = "createdAt"),
           @Index(name = "idx_customer_deleted_at", columnList = "deletedAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_customer_email", columnNames = "email")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(generator = "customer-uuid")
    @GenericGenerator(name = "customer-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @NotBlank(message = "First name is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Email(message = "Email must be valid", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 255, message = "Email must not exceed 255 characters",
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotNull(message = "Age is required", groups = {CreateValidation.class})
    @Positive(message = "Age must be positive", groups = {CreateValidation.class, UpdateValidation.class})
    @Min(value = 1, message = "Age must be at least 1", groups = {CreateValidation.class, UpdateValidation.class})
    @Max(value = 120, message = "Age must not exceed 120", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "age")
    private Integer age;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid",
             groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

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
     * Business method to check if customer is active
     * 
     * @return true if customer is active
     */
    public boolean isActive() {
        return this.status == CustomerStatus.ACTIVE && this.deletedAt == null;
    }

    /**
     * Business method to check if customer is an adult
     * 
     * @return true if customer is 18 or older
     */
    public boolean isAdult() {
        return this.age != null && this.age >= 18;
    }

    /**
     * Business method to get full name
     * 
     * @return formatted full name
     */
    public String getFullName() {
        return String.format("%s %s", this.firstName, this.lastName);
    }

    /**
     * Business method to get display name
     * 
     * @return formatted display name
     */
    public String getDisplayName() {
        return String.format("%s (%s)", getFullName(), this.email);
    }

    /**
     * Check if customer is deleted (soft delete)
     * 
     * @return true if customer is deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft delete customer
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = CustomerStatus.DELETED;
    }

    /**
     * Restore customer from soft delete
     */
    public void restore() {
        this.deletedAt = null;
        this.status = CustomerStatus.ACTIVE;
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
        // Example: Log entity load
        // logger.debug("Customer loaded: {}", this.id);
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
        Customer customer = (Customer) o;
        return Objects.equals(email, customer.email); // Business key for equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(email); // Hash based on business key
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                ", deletedAt=" + deletedAt +
                '}';
    }

    /**
     * Customer Status Enumeration
     */
    public enum CustomerStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        DELETED
    }
}
