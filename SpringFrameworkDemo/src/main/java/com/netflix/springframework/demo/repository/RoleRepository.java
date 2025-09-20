package com.netflix.springframework.demo.repository;

import com.netflix.springframework.demo.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RoleRepository - JPA Repository for Role Entity
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
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    
    /**
     * Find role by code
     * 
     * @param code Role code
     * @return Optional containing role if found
     */
    Optional<RoleEntity> findByCode(String code);
    
    /**
     * Find role by name
     * 
     * @param name Role name
     * @return Optional containing role if found
     */
    Optional<RoleEntity> findByName(String name);
    
    /**
     * Find roles by type
     * 
     * @param roleType Role type
     * @return List of roles with the specified type
     */
    List<RoleEntity> findByRoleType(RoleEntity.RoleType roleType);
    
    /**
     * Find active roles
     * 
     * @return List of active roles
     */
    List<RoleEntity> findByIsActiveTrue();
    
    /**
     * Find inactive roles
     * 
     * @return List of inactive roles
     */
    List<RoleEntity> findByIsActiveFalse();
    
    /**
     * Find roles by priority
     * 
     * @param priority Priority level
     * @return List of roles with the specified priority
     */
    List<RoleEntity> findByPriority(Integer priority);
    
    /**
     * Find roles by priority range
     * 
     * @param minPriority Minimum priority
     * @param maxPriority Maximum priority
     * @return List of roles within the priority range
     */
    List<RoleEntity> findByPriorityBetween(Integer minPriority, Integer maxPriority);
    
    /**
     * Find roles by name containing (case-insensitive)
     * 
     * @param name Name to search for
     * @return List of roles matching the name
     */
    @Query("SELECT r FROM RoleEntity r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RoleEntity> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find roles by description containing (case-insensitive)
     * 
     * @param description Description to search for
     * @return List of roles matching the description
     */
    @Query("SELECT r FROM RoleEntity r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<RoleEntity> findByDescriptionContainingIgnoreCase(@Param("description") String description);
    
    /**
     * Find roles by multiple criteria
     * 
     * @param name Name to search for
     * @param roleType Role type
     * @param isActive Active status
     * @return List of roles matching all criteria
     */
    @Query("SELECT r FROM RoleEntity r WHERE " +
           "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:roleType IS NULL OR r.roleType = :roleType) AND " +
           "(:isActive IS NULL OR r.isActive = :isActive)")
    List<RoleEntity> findByCriteria(@Param("name") String name,
                                   @Param("roleType") RoleEntity.RoleType roleType,
                                   @Param("isActive") Boolean isActive);
    
    /**
     * Count roles by type
     * 
     * @param roleType Role type
     * @return Number of roles with the specified type
     */
    long countByRoleType(RoleEntity.RoleType roleType);
    
    /**
     * Count active roles
     * 
     * @return Number of active roles
     */
    long countByIsActiveTrue();
    
    /**
     * Check if role exists by code
     * 
     * @param code Role code
     * @return true if role exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if role exists by name
     * 
     * @param name Role name
     * @return true if role exists
     */
    boolean existsByName(String name);
    
    /**
     * Delete role by code
     * 
     * @param code Role code
     * @return Number of deleted roles
     */
    int deleteByCode(String code);
    
    /**
     * Delete roles by type
     * 
     * @param roleType Role type
     * @return Number of deleted roles
     */
    int deleteByRoleType(RoleEntity.RoleType roleType);
}
