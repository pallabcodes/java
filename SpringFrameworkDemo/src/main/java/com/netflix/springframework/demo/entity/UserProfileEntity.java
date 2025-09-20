package com.netflix.springframework.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UserProfileEntity - JPA Entity for 1-to-1 Relationship
 * 
 * This entity demonstrates Netflix production-grade JPA 1-to-1 relationship implementation:
 * 1. One-to-one mapping with UserEntity
 * 2. Shared primary key strategy with @MapsId
 * 3. Comprehensive validation and constraints
 * 4. Audit fields and lifecycle management
 * 5. Database-level constraints and indexes
 * 
 * For C/C++ engineers:
 * - 1-to-1 relationships are like composition in C++
 * - @OneToOne is like having a single related object
 * - @MapsId is like sharing the same primary key
 * - Cascade operations are like automatic cleanup in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "user_profiles",
       indexes = {
           @Index(name = "idx_user_profile_bio", columnList = "bio"),
           @Index(name = "idx_user_profile_created_at", columnList = "created_at")
       })
public class UserProfileEntity {
    
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;
    
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    @Column(name = "bio", length = 1000)
    private String bio;
    
    @Size(max = 500, message = "Location must not exceed 500 characters")
    @Column(name = "location", length = 500)
    private String location;
    
    @Size(max = 100, message = "Website must not exceed 100 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$", 
             message = "Website must be a valid URL")
    @Column(name = "website", length = 100)
    private String website;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Min(value = 0, message = "Experience years must be non-negative")
    @Max(value = 100, message = "Experience years must not exceed 100")
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    @Column(name = "job_title", length = 100)
    private String jobTitle;
    
    @Size(max = 200, message = "Company must not exceed 200 characters")
    @Column(name = "company", length = 200)
    private String company;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status", nullable = false, length = 20)
    private ProfileStatus profileStatus;
    
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
     */
    public UserProfileEntity() {
        this.profileStatus = ProfileStatus.ACTIVE;
    }
    
    /**
     * Constructor with user
     * 
     * @param user The user entity
     */
    public UserProfileEntity(UserEntity user) {
        this();
        this.user = user;
    }
    
    // Getters and Setters
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public UserEntity getUser() {
        return user;
    }
    
    public void setUser(UserEntity user) {
        this.user = user;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Integer getExperienceYears() {
        return experienceYears;
    }
    
    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public ProfileStatus getProfileStatus() {
        return profileStatus;
    }
    
    public void setProfileStatus(ProfileStatus profileStatus) {
        this.profileStatus = profileStatus;
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
     * Business method to check if profile is complete
     * 
     * @return true if profile is complete
     */
    public boolean isComplete() {
        return bio != null && !bio.trim().isEmpty() &&
               location != null && !location.trim().isEmpty() &&
               jobTitle != null && !jobTitle.trim().isEmpty();
    }
    
    /**
     * Business method to get display name
     * 
     * @return formatted display name
     */
    public String getDisplayName() {
        if (user != null) {
            return String.format("%s (%s)", user.getName(), jobTitle != null ? jobTitle : "No title");
        }
        return "Unknown User";
    }
    
    /**
     * Profile status enumeration
     */
    public enum ProfileStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        PENDING("Pending"),
        SUSPENDED("Suspended");
        
        private final String displayName;
        
        ProfileStatus(String displayName) {
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
        
        UserProfileEntity that = (UserProfileEntity) obj;
        return Objects.equals(userId, that.userId);
    }
    
    /**
     * hashCode method for entity hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "UserProfileEntity{" +
                "userId=" + userId +
                ", bio='" + bio + '\'' +
                ", location='" + location + '\'' +
                ", website='" + website + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", experienceYears=" + experienceYears +
                ", jobTitle='" + jobTitle + '\'' +
                ", company='" + company + '\'' +
                ", profileStatus=" + profileStatus +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
