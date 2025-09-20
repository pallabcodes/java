package com.netflix.springframework.demo.service;

import com.netflix.springframework.demo.entity.UserEntity;
import com.netflix.springframework.demo.entity.RoleEntity;
import com.netflix.springframework.demo.entity.UserProfileEntity;
import com.netflix.springframework.demo.repository.UserAdvancedRepository;
import com.netflix.springframework.demo.repository.RoleRepository;
import com.netflix.springframework.demo.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserTransactionService - Comprehensive Database Transaction Management
 * 
 * This service demonstrates Netflix production-grade transaction management:
 * 1. @Transactional annotations with different propagation levels
 * 2. Transaction isolation levels for data consistency
 * 3. Rollback strategies and error handling
 * 4. Nested transactions and transaction boundaries
 * 5. Performance optimization with transaction scope
 * 6. Deadlock prevention and concurrency control
 * 
 * For C/C++ engineers:
 * - @Transactional is like database transaction management in C++
 * - Propagation levels are like transaction scope in C++
 * - Isolation levels are like locking mechanisms in C++
 * - Rollback is like transaction rollback in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Service
@Transactional
public class UserTransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserTransactionService.class);
    private static final String SERVICE_NAME = "UserTransactionService";
    
    private final UserAdvancedRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    
    /**
     * Constructor with dependency injection
     * 
     * @param userRepository User repository
     * @param roleRepository Role repository
     * @param userProfileRepository User profile repository
     */
    @Autowired
    public UserTransactionService(UserAdvancedRepository userRepository,
                                 RoleRepository roleRepository,
                                 UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userProfileRepository = userProfileRepository;
        logger.info("{} initialized", SERVICE_NAME);
    }
    
    /**
     * Create user with profile and roles in a single transaction
     * 
     * This method demonstrates REQUIRED propagation (default)
     * 
     * @param user User to create
     * @param profile Profile to create
     * @param roleCodes Role codes to assign
     * @return Created user with profile and roles
     */
    @Transactional(propagation = Propagation.REQUIRED, 
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public UserEntity createUserWithProfileAndRoles(UserEntity user, 
                                                   UserProfileEntity profile, 
                                                   List<String> roleCodes) {
        logger.info("{} - Creating user with profile and roles", SERVICE_NAME);
        
        try {
            // Save user first
            UserEntity savedUser = userRepository.save(user);
            logger.info("User created with ID: {}", savedUser.getId());
            
            // Create and save profile
            profile.setUser(savedUser);
            UserProfileEntity savedProfile = userProfileRepository.save(profile);
            logger.info("Profile created for user ID: {}", savedUser.getId());
            
            // Assign roles
            for (String roleCode : roleCodes) {
                Optional<RoleEntity> roleOpt = roleRepository.findByCode(roleCode);
                if (roleOpt.isPresent()) {
                    savedUser.addRole(roleOpt.get());
                    logger.info("Role {} assigned to user ID: {}", roleCode, savedUser.getId());
                } else {
                    logger.warn("Role {} not found, skipping assignment", roleCode);
                }
            }
            
            // Save user with roles
            UserEntity finalUser = userRepository.save(savedUser);
            logger.info("User creation completed successfully for ID: {}", finalUser.getId());
            
            return finalUser;
            
        } catch (Exception e) {
            logger.error("Error creating user with profile and roles", e);
            throw new RuntimeException("Failed to create user with profile and roles", e);
        }
    }
    
    /**
     * Update user with profile in a single transaction
     * 
     * This method demonstrates REQUIRED propagation with rollback
     * 
     * @param userId User ID to update
     * @param user User data to update
     * @param profile Profile data to update
     * @return Updated user with profile
     */
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public UserEntity updateUserWithProfile(Long userId, UserEntity user, UserProfileEntity profile) {
        logger.info("{} - Updating user with profile for ID: {}", SERVICE_NAME, userId);
        
        try {
            // Find existing user
            Optional<UserEntity> existingUserOpt = userRepository.findById(userId);
            if (existingUserOpt.isEmpty()) {
                throw new RuntimeException("User not found with ID: " + userId);
            }
            
            UserEntity existingUser = existingUserOpt.get();
            
            // Update user data
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setAge(user.getAge());
            existingUser.setPhoneNumber(user.getPhoneNumber());
            existingUser.setStatus(user.getStatus());
            
            // Update profile data
            Optional<UserProfileEntity> existingProfileOpt = userProfileRepository.findByUserId(userId);
            if (existingProfileOpt.isPresent()) {
                UserProfileEntity existingProfile = existingProfileOpt.get();
                existingProfile.setBio(profile.getBio());
                existingProfile.setLocation(profile.getLocation());
                existingProfile.setWebsite(profile.getWebsite());
                existingProfile.setJobTitle(profile.getJobTitle());
                existingProfile.setCompany(profile.getCompany());
                userProfileRepository.save(existingProfile);
                logger.info("Profile updated for user ID: {}", userId);
            } else {
                profile.setUser(existingUser);
                userProfileRepository.save(profile);
                logger.info("Profile created for user ID: {}", userId);
            }
            
            // Save user
            UserEntity updatedUser = userRepository.save(existingUser);
            logger.info("User update completed successfully for ID: {}", userId);
            
            return updatedUser;
            
        } catch (Exception e) {
            logger.error("Error updating user with profile for ID: {}", userId, e);
            throw new RuntimeException("Failed to update user with profile", e);
        }
    }
    
    /**
     * Soft delete user with all related data
     * 
     * This method demonstrates REQUIRED propagation with custom rollback
     * 
     * @param userId User ID to delete
     * @return true if deletion was successful
     */
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = {Exception.class, RuntimeException.class})
    public boolean softDeleteUserWithRelatedData(Long userId) {
        logger.info("{} - Soft deleting user with related data for ID: {}", SERVICE_NAME, userId);
        
        try {
            // Find existing user
            Optional<UserEntity> existingUserOpt = userRepository.findById(userId);
            if (existingUserOpt.isEmpty()) {
                throw new RuntimeException("User not found with ID: " + userId);
            }
            
            UserEntity existingUser = existingUserOpt.get();
            
            // Soft delete user
            existingUser.softDelete();
            userRepository.save(existingUser);
            logger.info("User soft deleted for ID: {}", userId);
            
            // Soft delete profile if exists
            Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
            if (profileOpt.isPresent()) {
                UserProfileEntity profile = profileOpt.get();
                profile.setProfileStatus(UserProfileEntity.ProfileStatus.INACTIVE);
                userProfileRepository.save(profile);
                logger.info("Profile deactivated for user ID: {}", userId);
            }
            
            // Remove all roles
            existingUser.getRoles().clear();
            userRepository.save(existingUser);
            logger.info("Roles removed for user ID: {}", userId);
            
            logger.info("User soft deletion completed successfully for ID: {}", userId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error soft deleting user with related data for ID: {}", userId, e);
            throw new RuntimeException("Failed to soft delete user with related data", e);
        }
    }
    
    /**
     * Batch update user statuses
     * 
     * This method demonstrates REQUIRES_NEW propagation
     * 
     * @param userIds List of user IDs to update
     * @param status New status to set
     * @return Number of users updated
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public int batchUpdateUserStatuses(List<Long> userIds, UserEntity.UserStatus status) {
        logger.info("{} - Batch updating user statuses for {} users", SERVICE_NAME, userIds.size());
        
        try {
            int updatedCount = 0;
            
            for (Long userId : userIds) {
                int result = userRepository.updateUserStatus(userId, status);
                updatedCount += result;
                logger.debug("Updated user ID: {} with status: {}", userId, status);
            }
            
            logger.info("Batch update completed successfully. Updated {} users", updatedCount);
            return updatedCount;
            
        } catch (Exception e) {
            logger.error("Error batch updating user statuses", e);
            throw new RuntimeException("Failed to batch update user statuses", e);
        }
    }
    
    /**
     * Transfer user roles between users
     * 
     * This method demonstrates MANDATORY propagation
     * 
     * @param fromUserId Source user ID
     * @param toUserId Target user ID
     * @param roleCodes Role codes to transfer
     * @return true if transfer was successful
     */
    @Transactional(propagation = Propagation.MANDATORY,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public boolean transferUserRoles(Long fromUserId, Long toUserId, List<String> roleCodes) {
        logger.info("{} - Transferring roles from user {} to user {}", SERVICE_NAME, fromUserId, toUserId);
        
        try {
            // Find both users
            Optional<UserEntity> fromUserOpt = userRepository.findById(fromUserId);
            Optional<UserEntity> toUserOpt = userRepository.findById(toUserId);
            
            if (fromUserOpt.isEmpty() || toUserOpt.isEmpty()) {
                throw new RuntimeException("One or both users not found");
            }
            
            UserEntity fromUser = fromUserOpt.get();
            UserEntity toUser = toUserOpt.get();
            
            // Transfer roles
            for (String roleCode : roleCodes) {
                Optional<RoleEntity> roleOpt = roleRepository.findByCode(roleCode);
                if (roleOpt.isPresent()) {
                    RoleEntity role = roleOpt.get();
                    
                    // Remove from source user
                    fromUser.removeRole(role);
                    
                    // Add to target user
                    toUser.addRole(role);
                    
                    logger.debug("Transferred role {} from user {} to user {}", roleCode, fromUserId, toUserId);
                }
            }
            
            // Save both users
            userRepository.save(fromUser);
            userRepository.save(toUser);
            
            logger.info("Role transfer completed successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Error transferring user roles", e);
            throw new RuntimeException("Failed to transfer user roles", e);
        }
    }
    
    /**
     * Clean up orphaned data
     * 
     * This method demonstrates NOT_SUPPORTED propagation
     * 
     * @return Number of orphaned records cleaned up
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int cleanupOrphanedData() {
        logger.info("{} - Cleaning up orphaned data", SERVICE_NAME);
        
        try {
            // This method runs without a transaction
            // Useful for read-only operations or when you want to avoid transaction overhead
            
            int cleanedCount = 0;
            
            // Clean up orphaned profiles
            List<UserProfileEntity> orphanedProfiles = userProfileRepository.findOrphanedProfiles();
            for (UserProfileEntity profile : orphanedProfiles) {
                userProfileRepository.delete(profile);
                cleanedCount++;
                logger.debug("Cleaned up orphaned profile: {}", profile.getId());
            }
            
            logger.info("Orphaned data cleanup completed. Cleaned {} records", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            logger.error("Error cleaning up orphaned data", e);
            throw new RuntimeException("Failed to cleanup orphaned data", e);
        }
    }
    
    /**
     * Get user statistics
     * 
     * This method demonstrates SUPPORTS propagation
     * 
     * @return User statistics
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public UserStatistics getUserStatistics() {
        logger.info("{} - Getting user statistics", SERVICE_NAME);
        
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countActiveUsers();
            long deletedUsers = userRepository.countDeletedUsers();
            
            UserStatistics stats = new UserStatistics(totalUsers, activeUsers, deletedUsers);
            logger.info("User statistics retrieved: {}", stats);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting user statistics", e);
            throw new RuntimeException("Failed to get user statistics", e);
        }
    }
    
    /**
     * User statistics class
     */
    public static class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long deletedUsers;
        
        public UserStatistics(long totalUsers, long activeUsers, long deletedUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.deletedUsers = deletedUsers;
        }
        
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getDeletedUsers() { return deletedUsers; }
        
        @Override
        public String toString() {
            return "UserStatistics{" +
                    "totalUsers=" + totalUsers +
                    ", activeUsers=" + activeUsers +
                    ", deletedUsers=" + deletedUsers +
                    '}';
        }
    }
}
