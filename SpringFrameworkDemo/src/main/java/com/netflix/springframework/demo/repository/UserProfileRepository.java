package com.netflix.springframework.demo.repository;

import com.netflix.springframework.demo.entity.UserProfileEntity;
import com.netflix.springframework.demo.entity.UserProfileEntity.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserProfileRepository - JPA Repository for UserProfile Entity
 * 
 * This repository demonstrates Netflix production-grade JPA repository implementation:
 * 1. Basic CRUD operations with JpaRepository
 * 2. Custom query methods with proper naming conventions
 * 3. Query methods with parameters and return types
 * 4. Performance optimization with proper indexing
 * 5. Relationship management and cascading operations
 * 
 * For C/C++ engineers:
 * - JPA repositories are like data access objects (DAOs) in C++
 * - Query methods are like SQL query builders
 * - @Query is like writing custom SQL queries
 * - Repository interfaces are like abstract base classes in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
    
    /**
     * Find profile by user ID
     * 
     * @param userId User ID
     * @return Optional containing profile if found
     */
    Optional<UserProfileEntity> findByUserId(Long userId);
    
    /**
     * Find profiles by status
     * 
     * @param status Profile status
     * @return List of profiles with the specified status
     */
    List<UserProfileEntity> findByProfileStatus(ProfileStatus status);
    
    /**
     * Find profiles by location
     * 
     * @param location Location to search for
     * @return List of profiles in the specified location
     */
    List<UserProfileEntity> findByLocation(String location);
    
    /**
     * Find profiles by location containing (case-insensitive)
     * 
     * @param location Location to search for
     * @return List of profiles matching the location
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<UserProfileEntity> findByLocationContainingIgnoreCase(@Param("location") String location);
    
    /**
     * Find profiles by job title
     * 
     * @param jobTitle Job title to search for
     * @return List of profiles with the specified job title
     */
    List<UserProfileEntity> findByJobTitle(String jobTitle);
    
    /**
     * Find profiles by job title containing (case-insensitive)
     * 
     * @param jobTitle Job title to search for
     * @return List of profiles matching the job title
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE LOWER(p.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))")
    List<UserProfileEntity> findByJobTitleContainingIgnoreCase(@Param("jobTitle") String jobTitle);
    
    /**
     * Find profiles by company
     * 
     * @param company Company to search for
     * @return List of profiles with the specified company
     */
    List<UserProfileEntity> findByCompany(String company);
    
    /**
     * Find profiles by company containing (case-insensitive)
     * 
     * @param company Company to search for
     * @return List of profiles matching the company
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE LOWER(p.company) LIKE LOWER(CONCAT('%', :company, '%'))")
    List<UserProfileEntity> findByCompanyContainingIgnoreCase(@Param("company") String company);
    
    /**
     * Find profiles by experience years range
     * 
     * @param minExperience Minimum experience years
     * @param maxExperience Maximum experience years
     * @return List of profiles within the experience range
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE p.experienceYears BETWEEN :minExperience AND :maxExperience")
    List<UserProfileEntity> findByExperienceYearsBetween(@Param("minExperience") Integer minExperience,
                                                        @Param("maxExperience") Integer maxExperience);
    
    /**
     * Find profiles by multiple criteria
     * 
     * @param location Location to search for
     * @param jobTitle Job title to search for
     * @param company Company to search for
     * @param status Profile status
     * @return List of profiles matching all criteria
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE " +
           "(:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:jobTitle IS NULL OR LOWER(p.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
           "(:company IS NULL OR LOWER(p.company) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
           "(:status IS NULL OR p.profileStatus = :status)")
    List<UserProfileEntity> findByCriteria(@Param("location") String location,
                                          @Param("jobTitle") String jobTitle,
                                          @Param("company") String company,
                                          @Param("status") ProfileStatus status);
    
    /**
     * Find complete profiles (with all required fields)
     * 
     * @return List of complete profiles
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE " +
           "p.bio IS NOT NULL AND p.bio != '' AND " +
           "p.location IS NOT NULL AND p.location != '' AND " +
           "p.jobTitle IS NOT NULL AND p.jobTitle != ''")
    List<UserProfileEntity> findCompleteProfiles();
    
    /**
     * Find incomplete profiles (missing required fields)
     * 
     * @return List of incomplete profiles
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE " +
           "p.bio IS NULL OR p.bio = '' OR " +
           "p.location IS NULL OR p.location = '' OR " +
           "p.jobTitle IS NULL OR p.jobTitle = ''")
    List<UserProfileEntity> findIncompleteProfiles();
    
    /**
     * Find orphaned profiles (profiles without users)
     * 
     * @return List of orphaned profiles
     */
    @Query("SELECT p FROM UserProfileEntity p WHERE p.user IS NULL")
    List<UserProfileEntity> findOrphanedProfiles();
    
    /**
     * Count profiles by status
     * 
     * @param status Profile status
     * @return Number of profiles with the specified status
     */
    long countByProfileStatus(ProfileStatus status);
    
    /**
     * Count complete profiles
     * 
     * @return Number of complete profiles
     */
    @Query("SELECT COUNT(p) FROM UserProfileEntity p WHERE " +
           "p.bio IS NOT NULL AND p.bio != '' AND " +
           "p.location IS NOT NULL AND p.location != '' AND " +
           "p.jobTitle IS NOT NULL AND p.jobTitle != ''")
    long countCompleteProfiles();
    
    /**
     * Count profiles by location
     * 
     * @param location Location
     * @return Number of profiles in the specified location
     */
    long countByLocation(String location);
    
    /**
     * Count profiles by company
     * 
     * @param company Company
     * @return Number of profiles with the specified company
     */
    long countByCompany(String company);
    
    /**
     * Check if profile exists by user ID
     * 
     * @param userId User ID
     * @return true if profile exists
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Delete profile by user ID
     * 
     * @param userId User ID
     * @return Number of deleted profiles
     */
    int deleteByUserId(Long userId);
    
    /**
     * Delete profiles by status
     * 
     * @param status Profile status
     * @return Number of deleted profiles
     */
    int deleteByProfileStatus(ProfileStatus status);
}
