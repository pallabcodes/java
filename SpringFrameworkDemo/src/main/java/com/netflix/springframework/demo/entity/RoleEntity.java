package com.netflix.springframework.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * RoleEntity - JPA Entity for Many-to-Many Relationship
 * 
 * This entity demonstrates Netflix production-grade JPA many-to-many relationship implementation:
 * 1. Many-to-many mapping with UserEntity
 * 2. Join table configuration with custom columns
 * 3. Bidirectional relationship management
 * 4. Cascade operations and orphan removal
 * 5. Database-level constraints and indexes
 * 
 * For C/C++ engineers:
 * - Many-to-many relationships are like many-to-many associations in C++
 * - @ManyToMany is like having multiple related objects
 * - @JoinTable is like defining the junction table
 * - Cascade operations are like automatic cleanup in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "roles",
       indexes = {
           @Index(name = "idx_role_name", columnList = "name"),
           @Index(name = "idx_role_code", columnList = "code"),
           @Index(name = "idx_role_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_role_name", columnNames = "name"),
           @UniqueConstraint(name = "uk_role_code", columnNames = "code")
       })
public class RoleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Role name must contain only letters and spaces")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    @NotBlank(message = "Role code is required")
    @Size(min = 2, max = 50, message = "Role code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role code must contain only uppercase letters and underscores")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType roleType;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "priority", nullable = false)
    private Integer priority;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    @ManyToMany(mappedBy = "roles", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<UserEntity> users = new HashSet<>();
    
    /**
     * Default constructor
     */
    public RoleEntity() {
        this.isActive = true;
        this.priority = 0;
        this.roleType = RoleType.USER;
    }
    
    /**
     * Constructor with name and code
     * 
     * @param name Role name
     * @param code Role code
     */
    public RoleEntity(String name, String code) {
        this();
        this.name = name;
        this.code = code;
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public RoleType getRoleType() {
        return roleType;
    }
    
    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
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
    
    public Set<UserEntity> getUsers() {
        return users;
    }
    
    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }
    
    /**
     * Add user to role
     * 
     * @param user User to add
     */
    public void addUser(UserEntity user) {
        this.users.add(user);
        user.getRoles().add(this);
    }
    
    /**
     * Remove user from role
     * 
     * @param user User to remove
     */
    public void removeUser(UserEntity user) {
        this.users.remove(user);
        user.getRoles().remove(this);
    }
    
    /**
     * Check if role is active
     * 
     * @return true if role is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }
    
    /**
     * Check if role is system role
     * 
     * @return true if role is system role
     */
    public boolean isSystemRole() {
        return RoleType.SYSTEM.equals(this.roleType);
    }
    
    /**
     * Get display name
     * 
     * @return formatted display name
     */
    public String getDisplayName() {
        return String.format("%s (%s)", this.name, this.code);
    }
    
    /**
     * Role type enumeration
     */
    public enum RoleType {
        SYSTEM("System"),
        USER("User"),
        ADMIN("Admin"),
        CUSTOM("Custom");
        
        private final String displayName;
        
        RoleType(String displayName) {
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
        
        RoleEntity that = (RoleEntity) obj;
        return Objects.equals(id, that.id) &&
               Objects.equals(code, that.code);
    }
    
    /**
     * hashCode method for entity hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "RoleEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", roleType=" + roleType +
                ", isActive=" + isActive +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
