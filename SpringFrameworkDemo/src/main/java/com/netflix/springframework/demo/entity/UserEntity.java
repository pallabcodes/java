package com.netflix.springframework.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * UserEntity - JPA Entity with Advanced Validation
 * 
 * This entity demonstrates Netflix production-grade JPA implementation with:
 * 1. Comprehensive Bean Validation annotations
 * 2. JPA entity mapping with proper constraints
 * 3. Audit fields for creation and update tracking
 * 4. Custom validation groups for different scenarios
 * 5. Database-level constraints and indexes
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
@Table(name = "users", 
       indexes = {
           @Index(name = "idx_user_email", columnList = "email"),
           @Index(name = "idx_user_name", columnList = "name"),
           @Index(name = "idx_user_created_at", columnList = "created_at"),
           @Index(name = "idx_user_deleted_at", columnList = "deleted_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_email", columnNames = "email")
       })
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @NotBlank(message = "Name is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters", 
          groups = {CreateValidation.class, UpdateValidation.class})
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must contain only letters and spaces",
             groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Email is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Email(message = "Email must be valid", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 255, message = "Email must not exceed 255 characters", 
          groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters", groups = {UpdateValidation.class})
    @Column(name = "bio", length = 500)
    private String bio;
    
    @Min(value = 18, message = "Age must be at least 18", groups = {CreateValidation.class, UpdateValidation.class})
    @Max(value = 120, message = "Age must not exceed 120", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "age")
    private Integer age;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid",
             groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @NotNull(message = "Status is required", groups = {CreateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;
    
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
    
    // 1-to-1 relationship with UserProfile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfileEntity profile;
    
    // Many-to-Many relationship with Roles
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        indexes = {
            @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
            @Index(name = "idx_user_roles_role_id", columnList = "role_id")
        }
    )
    private Set<RoleEntity> roles = new HashSet<>();
    
    /**
     * Default constructor
     * 
     * Required by JPA specification
     */
    public UserEntity() {
        this.status = UserStatus.ACTIVE;
    }
    
    /**
     * Constructor for creating new user
     * 
     * @param name User's name
     * @param email User's email
     * @param age User's age
     */
    public UserEntity(String name, String email, Integer age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
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
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public UserProfileEntity getProfile() {
        return profile;
    }
    
    public void setProfile(UserProfileEntity profile) {
        this.profile = profile;
    }
    
    public Set<RoleEntity> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }
    
    /**
     * Business method to check if user is active
     * 
     * @return true if user is active
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * Business method to check if user is adult
     * 
     * @return true if user is 18 or older
     */
    public boolean isAdult() {
        return this.age != null && this.age >= 18;
    }
    
    /**
     * Business method to get display name
     * 
     * @return formatted display name
     */
    public String getDisplayName() {
        return String.format("%s (%s)", this.name, this.email);
    }
    
    /**
     * Check if user is deleted (soft delete)
     * 
     * @return true if user is deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
    
    /**
     * Soft delete user
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * Restore user from soft delete
     */
    public void restore() {
        this.deletedAt = null;
    }
    
    /**
     * Add role to user
     * 
     * @param role Role to add
     */
    public void addRole(RoleEntity role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }
    
    /**
     * Remove role from user
     * 
     * @param role Role to remove
     */
    public void removeRole(RoleEntity role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }
    
    /**
     * Check if user has role
     * 
     * @param roleCode Role code to check
     * @return true if user has the role
     */
    public boolean hasRole(String roleCode) {
        return this.roles.stream()
                .anyMatch(role -> role.getCode().equals(roleCode));
    }
    
    /**
     * Check if user has any of the specified roles
     * 
     * @param roleCodes Role codes to check
     * @return true if user has any of the roles
     */
    public boolean hasAnyRole(String... roleCodes) {
        return this.roles.stream()
                .anyMatch(role -> java.util.Arrays.asList(roleCodes).contains(role.getCode()));
    }
    
    /**
     * Get user's role codes
     * 
     * @return Set of role codes
     */
    public Set<String> getRoleCodes() {
        return this.roles.stream()
                .map(RoleEntity::getCode)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Validation groups for different scenarios
     */
    public interface CreateValidation {}
    public interface UpdateValidation {}
    
    /**
     * User status enumeration
     */
    public enum UserStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        SUSPENDED("Suspended"),
        PENDING("Pending");
        
        private final String displayName;
        
        UserStatus(String displayName) {
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
        
        UserEntity that = (UserEntity) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(email, that.email);
    }
    
    /**
     * hashCode method for entity hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
