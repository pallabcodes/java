package com.netflix.springframework.demo.repository;

import com.netflix.springframework.demo.entity.SubscriptionEntity;
import com.netflix.springframework.demo.entity.SubscriptionEntity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SubscriptionRepository - JPA Repository for Subscription Entity
 * 
 * This repository demonstrates Netflix production-grade JPA repository implementation:
 * 1. Basic CRUD operations with JpaRepository
 * 2. Custom query methods with proper naming conventions
 * 3. Query methods with parameters and return types
 * 4. Performance optimization with proper indexing
 * 5. Subscription-specific business queries
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
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    
    /**
     * Find subscription by Stripe subscription ID
     * 
     * @param stripeSubscriptionId Stripe subscription ID
     * @return Optional containing subscription if found
     */
    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    /**
     * Find subscriptions by user ID
     * 
     * @param userId User ID
     * @return List of subscriptions for the user
     */
    List<SubscriptionEntity> findByUserId(Long userId);
    
    /**
     * Find subscriptions by status
     * 
     * @param status Subscription status
     * @return List of subscriptions with the specified status
     */
    List<SubscriptionEntity> findByStatus(SubscriptionStatus status);
    
    /**
     * Find subscriptions by user ID and status
     * 
     * @param userId User ID
     * @param status Subscription status
     * @return List of subscriptions for the user with the specified status
     */
    List<SubscriptionEntity> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    /**
     * Find subscriptions by price ID
     * 
     * @param priceId Price ID
     * @return List of subscriptions with the specified price ID
     */
    List<SubscriptionEntity> findByPriceId(String priceId);
    
    /**
     * Find subscriptions by price ID and status
     * 
     * @param priceId Price ID
     * @param status Subscription status
     * @return List of subscriptions with the specified price ID and status
     */
    List<SubscriptionEntity> findByPriceIdAndStatus(String priceId, SubscriptionStatus status);
    
    /**
     * Find subscriptions by cancel at period end
     * 
     * @param cancelAtPeriodEnd Cancel at period end flag
     * @return List of subscriptions with the specified cancel at period end flag
     */
    List<SubscriptionEntity> findByCancelAtPeriodEnd(Boolean cancelAtPeriodEnd);
    
    /**
     * Find subscriptions by trial start date
     * 
     * @param trialStart Trial start date
     * @return List of subscriptions with the specified trial start date
     */
    List<SubscriptionEntity> findByTrialStart(LocalDateTime trialStart);
    
    /**
     * Find subscriptions by trial end date
     * 
     * @param trialEnd Trial end date
     * @return List of subscriptions with the specified trial end date
     */
    List<SubscriptionEntity> findByTrialEnd(LocalDateTime trialEnd);
    
    /**
     * Find subscriptions by current period start date
     * 
     * @param currentPeriodStart Current period start date
     * @return List of subscriptions with the specified current period start date
     */
    List<SubscriptionEntity> findByCurrentPeriodStart(LocalDateTime currentPeriodStart);
    
    /**
     * Find subscriptions by current period end date
     * 
     * @param currentPeriodEnd Current period end date
     * @return List of subscriptions with the specified current period end date
     */
    List<SubscriptionEntity> findByCurrentPeriodEnd(LocalDateTime currentPeriodEnd);
    
    /**
     * Find subscriptions by current period end date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of subscriptions with current period end within the date range
     */
    List<SubscriptionEntity> findByCurrentPeriodEndBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find subscriptions by creation date
     * 
     * @param createdAt Creation date
     * @return List of subscriptions created on the specified date
     */
    List<SubscriptionEntity> findByCreatedAt(LocalDateTime createdAt);
    
    /**
     * Find subscriptions by creation date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of subscriptions created within the date range
     */
    List<SubscriptionEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find active subscriptions by user ID
     * 
     * @param userId User ID
     * @return List of active subscriptions for the user
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    List<SubscriptionEntity> findActiveSubscriptionsByUserId(@Param("userId") Long userId);
    
    /**
     * Find trial subscriptions by user ID
     * 
     * @param userId User ID
     * @return List of trial subscriptions for the user
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.userId = :userId AND s.status = 'TRIALING'")
    List<SubscriptionEntity> findTrialSubscriptionsByUserId(@Param("userId") Long userId);
    
    /**
     * Find cancelled subscriptions by user ID
     * 
     * @param userId User ID
     * @return List of cancelled subscriptions for the user
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.userId = :userId AND s.status = 'CANCELLED'")
    List<SubscriptionEntity> findCancelledSubscriptionsByUserId(@Param("userId") Long userId);
    
    /**
     * Find subscriptions expiring soon
     * 
     * @param days Number of days
     * @return List of subscriptions expiring within the specified days
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.currentPeriodEnd <= :expiryDate AND s.status = 'ACTIVE'")
    List<SubscriptionEntity> findSubscriptionsExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find trial subscriptions ending soon
     * 
     * @param days Number of days
     * @return List of trial subscriptions ending within the specified days
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.trialEnd <= :endDate AND s.status = 'TRIALING'")
    List<SubscriptionEntity> findTrialSubscriptionsEndingSoon(@Param("endDate") LocalDateTime endDate);
    
    /**
     * Find subscriptions by multiple criteria
     * 
     * @param userId User ID (optional)
     * @param status Subscription status (optional)
     * @param priceId Price ID (optional)
     * @param cancelAtPeriodEnd Cancel at period end flag (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return List of subscriptions matching the criteria
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE " +
           "(:userId IS NULL OR s.userId = :userId) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:priceId IS NULL OR s.priceId = :priceId) AND " +
           "(:cancelAtPeriodEnd IS NULL OR s.cancelAtPeriodEnd = :cancelAtPeriodEnd) AND " +
           "(:startDate IS NULL OR s.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR s.createdAt <= :endDate)")
    List<SubscriptionEntity> findByCriteria(@Param("userId") Long userId,
                                           @Param("status") SubscriptionStatus status,
                                           @Param("priceId") String priceId,
                                           @Param("cancelAtPeriodEnd") Boolean cancelAtPeriodEnd,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count subscriptions by status
     * 
     * @param status Subscription status
     * @return Number of subscriptions with the specified status
     */
    long countByStatus(SubscriptionStatus status);
    
    /**
     * Count subscriptions by user ID
     * 
     * @param userId User ID
     * @return Number of subscriptions for the user
     */
    long countByUserId(Long userId);
    
    /**
     * Count subscriptions by user ID and status
     * 
     * @param userId User ID
     * @param status Subscription status
     * @return Number of subscriptions for the user with the specified status
     */
    long countByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    /**
     * Count subscriptions by price ID
     * 
     * @param priceId Price ID
     * @return Number of subscriptions with the specified price ID
     */
    long countByPriceId(String priceId);
    
    /**
     * Count active subscriptions by user ID
     * 
     * @param userId User ID
     * @return Number of active subscriptions for the user
     */
    @Query("SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    long countActiveSubscriptionsByUserId(@Param("userId") Long userId);
    
    /**
     * Count trial subscriptions by user ID
     * 
     * @param userId User ID
     * @return Number of trial subscriptions for the user
     */
    @Query("SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.userId = :userId AND s.status = 'TRIALING'")
    long countTrialSubscriptionsByUserId(@Param("userId") Long userId);
    
    /**
     * Count subscriptions expiring soon
     * 
     * @param expiryDate Expiry date
     * @return Number of subscriptions expiring before the specified date
     */
    @Query("SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.currentPeriodEnd <= :expiryDate AND s.status = 'ACTIVE'")
    long countSubscriptionsExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Count trial subscriptions ending soon
     * 
     * @param endDate End date
     * @return Number of trial subscriptions ending before the specified date
     */
    @Query("SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.trialEnd <= :endDate AND s.status = 'TRIALING'")
    long countTrialSubscriptionsEndingSoon(@Param("endDate") LocalDateTime endDate);
    
    /**
     * Get subscription statistics by status
     * 
     * @return List of subscription status statistics
     */
    @Query("SELECT s.status, COUNT(s) FROM SubscriptionEntity s GROUP BY s.status")
    List<Object[]> getSubscriptionStatisticsByStatus();
    
    /**
     * Get subscription statistics by price ID
     * 
     * @return List of subscription price ID statistics
     */
    @Query("SELECT s.priceId, COUNT(s) FROM SubscriptionEntity s GROUP BY s.priceId")
    List<Object[]> getSubscriptionStatisticsByPriceId();
    
    /**
     * Get subscription statistics by creation date
     * 
     * @return List of subscription creation date statistics
     */
    @Query("SELECT DATE(s.createdAt) as creationDate, COUNT(s) FROM SubscriptionEntity s GROUP BY DATE(s.createdAt) ORDER BY creationDate")
    List<Object[]> getSubscriptionStatisticsByCreationDate();
    
    /**
     * Get subscription statistics by current period end date
     * 
     * @return List of subscription current period end date statistics
     */
    @Query("SELECT DATE(s.currentPeriodEnd) as periodEndDate, COUNT(s) FROM SubscriptionEntity s GROUP BY DATE(s.currentPeriodEnd) ORDER BY periodEndDate")
    List<Object[]> getSubscriptionStatisticsByCurrentPeriodEnd();
    
    /**
     * Check if subscription exists by Stripe subscription ID
     * 
     * @param stripeSubscriptionId Stripe subscription ID
     * @return true if subscription exists
     */
    boolean existsByStripeSubscriptionId(String stripeSubscriptionId);
    
    /**
     * Check if user has active subscription
     * 
     * @param userId User ID
     * @return true if user has active subscription
     */
    @Query("SELECT COUNT(s) > 0 FROM SubscriptionEntity s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    boolean existsActiveSubscriptionByUserId(@Param("userId") Long userId);
    
    /**
     * Check if user has trial subscription
     * 
     * @param userId User ID
     * @return true if user has trial subscription
     */
    @Query("SELECT COUNT(s) > 0 FROM SubscriptionEntity s WHERE s.userId = :userId AND s.status = 'TRIALING'")
    boolean existsTrialSubscriptionByUserId(@Param("userId") Long userId);
    
    /**
     * Delete subscriptions by status
     * 
     * @param status Subscription status
     * @return Number of deleted subscriptions
     */
    int deleteByStatus(SubscriptionStatus status);
    
    /**
     * Delete subscriptions by user ID
     * 
     * @param userId User ID
     * @return Number of deleted subscriptions
     */
    int deleteByUserId(Long userId);
}
