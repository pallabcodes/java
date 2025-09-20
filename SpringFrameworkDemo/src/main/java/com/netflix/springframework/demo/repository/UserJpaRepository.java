package com.netflix.springframework.demo.repository;

import com.netflix.springframework.demo.entity.UserEntity;
import com.netflix.springframework.demo.entity.UserEntity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
 * UserJpaRepository - Advanced JPA Repository
 * 
 * This repository demonstrates Netflix production-grade JPA implementation with:
 * 1. Spring Data JPA repository with custom queries
 * 2. Query methods with proper naming conventions
 * 3. Custom JPQL and native queries
 * 4. Transaction management and locking
 * 5. Performance optimization with projections
 * 
 * For C/C++ engineers:
 * - JPA repositories are like data access objects (DAOs) in C++
 * - Query methods are like SQL query builders
 * - @Query is like writing custom SQL queries
 * - @Transactional is like database transaction management
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    
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
     * Find users by status
     * 
     * @param status User status
     * @return List of users with the specified status
     */
    List<UserEntity> findByStatus(UserStatus status);
    
    /**
     * Find users by age range
     * 
     * @param minAge Minimum age
     * @param maxAge Maximum age
     * @return List of users within the age range
     */
    @Query("SELECT u FROM UserEntity u WHERE u.age BETWEEN :minAge AND :maxAge")
    List<UserEntity> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
    
    /**
     * Find active users created after a specific date
     * 
     * @param createdAfter Date to filter by
     * @return List of active users created after the date
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' AND u.createdAt > :createdAfter")
    List<UserEntity> findActiveUsersCreatedAfter(@Param("createdAfter") LocalDateTime createdAfter);
    
    /**
     * Count users by status
     * 
     * @param status User status
     * @return Number of users with the specified status
     */
    long countByStatus(UserStatus status);
    
    /**
     * Find users by email domain
     * 
     * @param domain Email domain
     * @return List of users with the specified email domain
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<UserEntity> findByEmailDomain(@Param("domain") String domain);
    
    /**
     * Find users with pagination and sorting
     * 
     * @param status User status
     * @param offset Offset for pagination
     * @param limit Limit for pagination
     * @return List of users with pagination
     */
    @Query(value = "SELECT * FROM users WHERE status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<UserEntity> findUsersByStatusWithPagination(@Param("status") String status, 
                                                    @Param("offset") int offset, 
                                                    @Param("limit") int limit);
    
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
     * Delete users by status
     * 
     * @param status User status
     * @return Number of deleted users
     */
    @Modifying
    @Transactional
    int deleteByStatus(UserStatus status);
    
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
     * Find users with custom projection (only name and email)
     * 
     * @return List of user projections
     */
    @Query("SELECT new com.netflix.springframework.demo.dto.UserProjection(u.id, u.name, u.email) FROM UserEntity u")
    List<UserProjection> findUserProjections();
    
    /**
     * Find users by multiple criteria
     * 
     * @param name Name to search for
     * @param email Email to search for
     * @param status User status
     * @return List of users matching all criteria
     */
    @Query("SELECT u FROM UserEntity u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:status IS NULL OR u.status = :status)")
    List<UserEntity> findUsersByCriteria(@Param("name") String name, 
                                        @Param("email") String email, 
                                        @Param("status") UserStatus status);
    
    /**
     * Find users with complex search
     * 
     * @param searchTerm Search term for name or email
     * @param minAge Minimum age
     * @param maxAge Maximum age
     * @param status User status
     * @return List of users matching the search criteria
     */
    @Query("SELECT u FROM UserEntity u WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:minAge IS NULL OR u.age >= :minAge) AND " +
           "(:maxAge IS NULL OR u.age <= :maxAge) AND " +
           "(:status IS NULL OR u.status = :status)")
    List<UserEntity> findUsersWithComplexSearch(@Param("searchTerm") String searchTerm,
                                               @Param("minAge") Integer minAge,
                                               @Param("maxAge") Integer maxAge,
                                               @Param("status") UserStatus status);
    
    /**
     * User projection interface for performance optimization
     */
    interface UserProjection {
        Long getId();
        String getName();
        String getEmail();
    }
}
