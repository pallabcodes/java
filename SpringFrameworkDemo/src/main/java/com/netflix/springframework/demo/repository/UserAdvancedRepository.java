package com.netflix.springframework.demo.repository;

import com.netflix.springframework.demo.entity.UserEntity;
import com.netflix.springframework.demo.entity.UserEntity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserAdvancedRepository - Advanced JPA Repository with Comprehensive Features
 * 
 * This repository demonstrates Netflix production-grade JPA implementation with:
 * 1. Advanced query methods with custom JPQL and native queries
 * 2. Sorting and pagination with Pageable and Sort
 * 3. Soft delete queries with @Where annotation
 * 4. Entity lifecycle management and auditing
 * 5. Performance optimization with projections and locking
 * 6. Database transaction management
 * 7. Complex relationship queries
 * 
 * For C/C++ engineers:
 * - JPA repositories are like data access objects (DAOs) in C++
 * - Query methods are like SQL query builders
 * - @Query is like writing custom SQL queries
 * - Pagination is like LIMIT/OFFSET in SQL
 * - Sorting is like ORDER BY in SQL
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface UserAdvancedRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    
    // ==================== BASIC QUERIES ====================
    
    /**
     * Find user by email (case-insensitive)
     * 
     * @param email User's email
     * @return Optional containing user if found
     */
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UserEntity> findByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * Find users by name containing (case-insensitive)
     * 
     * @param name Name to search for
     * @return List of users matching the name
     */
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserEntity> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find users by status with pagination
     * 
     * @param status User status
     * @param pageable Pagination information
     * @return Page of users with the specified status
     */
    Page<UserEntity> findByStatus(UserStatus status, Pageable pageable);
    
    /**
     * Find users by status with sorting
     * 
     * @param status User status
     * @param sort Sorting information
     * @return List of users with the specified status
     */
    List<UserEntity> findByStatus(UserStatus status, Sort sort);
    
    // ==================== ADVANCED QUERIES ====================
    
    /**
     * Find users by age range with pagination
     * 
     * @param minAge Minimum age
     * @param maxAge Maximum age
     * @param pageable Pagination information
     * @return Page of users within the age range
     */
    @Query("SELECT u FROM UserEntity u WHERE u.age BETWEEN :minAge AND :maxAge")
    Page<UserEntity> findByAgeBetween(@Param("minAge") Integer minAge, 
                                     @Param("maxAge") Integer maxAge, 
                                     Pageable pageable);
    
    /**
     * Find active users created after a specific date
     * 
     * @param createdAfter Date to filter by
     * @param pageable Pagination information
     * @return Page of active users created after the date
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' AND u.createdAt > :createdAfter")
    Page<UserEntity> findActiveUsersCreatedAfter(@Param("createdAfter") LocalDateTime createdAfter, 
                                                Pageable pageable);
    
    /**
     * Find users by email domain with sorting
     * 
     * @param domain Email domain
     * @param sort Sorting information
     * @return List of users with the specified email domain
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<UserEntity> findByEmailDomain(@Param("domain") String domain, Sort sort);
    
    /**
     * Find users with complex search criteria
     * 
     * @param name Name to search for
     * @param email Email to search for
     * @param status User status
     * @param minAge Minimum age
     * @param maxAge Maximum age
     * @param pageable Pagination information
     * @return Page of users matching the search criteria
     */
    @Query("SELECT u FROM UserEntity u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:minAge IS NULL OR u.age >= :minAge) AND " +
           "(:maxAge IS NULL OR u.age <= :maxAge)")
    Page<UserEntity> findUsersWithComplexSearch(@Param("name") String name,
                                               @Param("email") String email,
                                               @Param("status") UserStatus status,
                                               @Param("minAge") Integer minAge,
                                               @Param("maxAge") Integer maxAge,
                                               Pageable pageable);
    
    // ==================== RELATIONSHIP QUERIES ====================
    
    /**
     * Find users by role code
     * 
     * @param roleCode Role code
     * @param pageable Pagination information
     * @return Page of users with the specified role
     */
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.code = :roleCode")
    Page<UserEntity> findByRoleCode(@Param("roleCode") String roleCode, Pageable pageable);
    
    /**
     * Find users by multiple role codes
     * 
     * @param roleCodes List of role codes
     * @param pageable Pagination information
     * @return Page of users with any of the specified roles
     */
    @Query("SELECT DISTINCT u FROM UserEntity u JOIN u.roles r WHERE r.code IN :roleCodes")
    Page<UserEntity> findByRoleCodes(@Param("roleCodes") List<String> roleCodes, Pageable pageable);
    
    /**
     * Find users with profile information
     * 
     * @param pageable Pagination information
     * @return Page of users with their profiles
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.profile WHERE u.profile IS NOT NULL")
    Page<UserEntity> findUsersWithProfiles(Pageable pageable);
    
    /**
     * Find users without profiles
     * 
     * @param pageable Pagination information
     * @return Page of users without profiles
     */
    @Query("SELECT u FROM UserEntity u WHERE u.profile IS NULL")
    Page<UserEntity> findUsersWithoutProfiles(Pageable pageable);
    
    // ==================== SOFT DELETE QUERIES ====================
    
    /**
     * Find all users including soft deleted
     * 
     * @param pageable Pagination information
     * @return Page of all users including deleted
     */
    @Query(value = "SELECT * FROM users", nativeQuery = true)
    Page<UserEntity> findAllIncludingDeleted(Pageable pageable);
    
    /**
     * Find soft deleted users
     * 
     * @param pageable Pagination information
     * @return Page of soft deleted users
     */
    @Query(value = "SELECT * FROM users WHERE deleted_at IS NOT NULL", nativeQuery = true)
    Page<UserEntity> findDeletedUsers(Pageable pageable);
    
    /**
     * Count active users (excluding soft deleted)
     * 
     * @return Number of active users
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.deletedAt IS NULL")
    long countActiveUsers();
    
    /**
     * Count deleted users
     * 
     * @return Number of deleted users
     */
    @Query(value = "SELECT COUNT(*) FROM users WHERE deleted_at IS NOT NULL", nativeQuery = true)
    long countDeletedUsers();
    
    // ==================== PERFORMANCE QUERIES ====================
    
    /**
     * Find users with projections (only id, name, email)
     * 
     * @param pageable Pagination information
     * @return Page of user projections
     */
    @Query("SELECT new com.netflix.springframework.demo.dto.UserProjection(u.id, u.name, u.email) FROM UserEntity u")
    Page<UserProjection> findUserProjections(Pageable pageable);
    
    /**
     * Find user by ID with pessimistic locking
     * 
     * @param id User ID
     * @return Optional containing user if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    Optional<UserEntity> findByIdWithLock(@Param("id") Long id);
    
    /**
     * Find users with eager loading of relationships
     * 
     * @param pageable Pagination information
     * @return Page of users with relationships loaded
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.profile")
    Page<UserEntity> findUsersWithRelationships(Pageable pageable);
    
    // ==================== BULK OPERATIONS ====================
    
    /**
     * Update user status by ID
     * 
     * @param id User ID
     * @param status New status
     * @return Number of affected rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.status = :status, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updateUserStatus(@Param("id") Long id, @Param("status") UserStatus status);
    
    /**
     * Soft delete users by status
     * 
     * @param status User status
     * @return Number of affected rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.status = :status")
    int softDeleteByStatus(@Param("status") UserStatus status);
    
    /**
     * Restore soft deleted users
     * 
     * @param deletedBefore Date before which users were deleted
     * @return Number of affected rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.deletedAt = NULL WHERE u.deletedAt < :deletedBefore")
    int restoreDeletedUsers(@Param("deletedBefore") LocalDateTime deletedBefore);
    
    /**
     * Permanently delete soft deleted users
     * 
     * @param deletedBefore Date before which users were deleted
     * @return Number of affected rows
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE deleted_at < :deletedBefore", nativeQuery = true)
    int permanentlyDeleteUsers(@Param("deletedBefore") LocalDateTime deletedBefore);
    
    // ==================== STATISTICS QUERIES ====================
    
    /**
     * Get user statistics by status
     * 
     * @return List of user status statistics
     */
    @Query("SELECT u.status, COUNT(u) FROM UserEntity u GROUP BY u.status")
    List<Object[]> getUserStatisticsByStatus();
    
    /**
     * Get user statistics by age group
     * 
     * @return List of user age group statistics
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN u.age < 18 THEN 'Under 18' " +
           "WHEN u.age BETWEEN 18 AND 25 THEN '18-25' " +
           "WHEN u.age BETWEEN 26 AND 35 THEN '26-35' " +
           "WHEN u.age BETWEEN 36 AND 50 THEN '36-50' " +
           "WHEN u.age > 50 THEN 'Over 50' " +
           "ELSE 'Unknown' " +
           "END as ageGroup, COUNT(u) " +
           "FROM UserEntity u " +
           "GROUP BY ageGroup")
    List<Object[]> getUserStatisticsByAgeGroup();
    
    /**
     * Get user statistics by creation date
     * 
     * @return List of user creation date statistics
     */
    @Query("SELECT DATE(u.createdAt) as creationDate, COUNT(u) FROM UserEntity u GROUP BY DATE(u.createdAt) ORDER BY creationDate")
    List<Object[]> getUserStatisticsByCreationDate();
    
    // ==================== PROJECTION INTERFACE ====================
    
    /**
     * User projection interface for performance optimization
     */
    interface UserProjection {
        Long getId();
        String getName();
        String getEmail();
    }
}
