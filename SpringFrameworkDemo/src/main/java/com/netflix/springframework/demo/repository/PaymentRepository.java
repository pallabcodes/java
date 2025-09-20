package com.netflix.springframework.demo.repository;

import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.PaymentEntity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PaymentRepository - JPA Repository for Payment Entity
 * 
 * This repository demonstrates Netflix production-grade JPA repository implementation:
 * 1. Basic CRUD operations with JpaRepository
 * 2. Custom query methods with proper naming conventions
 * 3. Query methods with parameters and return types
 * 4. Performance optimization with proper indexing
 * 5. Payment-specific business queries
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
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    
    /**
     * Find payment by Stripe payment intent ID
     * 
     * @param stripePaymentIntentId Stripe payment intent ID
     * @return Optional containing payment if found
     */
    Optional<PaymentEntity> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    /**
     * Find payments by user ID
     * 
     * @param userId User ID
     * @return List of payments for the user
     */
    List<PaymentEntity> findByUserId(Long userId);
    
    /**
     * Find payments by status
     * 
     * @param status Payment status
     * @return List of payments with the specified status
     */
    List<PaymentEntity> findByStatus(PaymentStatus status);
    
    /**
     * Find payments by user ID and status
     * 
     * @param userId User ID
     * @param status Payment status
     * @return List of payments for the user with the specified status
     */
    List<PaymentEntity> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    /**
     * Find payments by customer email
     * 
     * @param customerEmail Customer email
     * @return List of payments for the customer
     */
    List<PaymentEntity> findByCustomerEmail(String customerEmail);
    
    /**
     * Find payments by customer email and status
     * 
     * @param customerEmail Customer email
     * @param status Payment status
     * @return List of payments for the customer with the specified status
     */
    List<PaymentEntity> findByCustomerEmailAndStatus(String customerEmail, PaymentStatus status);
    
    /**
     * Find payments by amount range
     * 
     * @param minAmount Minimum amount
     * @param maxAmount Maximum amount
     * @return List of payments within the amount range
     */
    @Query("SELECT p FROM PaymentEntity p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
    List<PaymentEntity> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                           @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * Find payments by currency
     * 
     * @param currency Payment currency
     * @return List of payments with the specified currency
     */
    List<PaymentEntity> findByCurrency(String currency);
    
    /**
     * Find payments by currency and status
     * 
     * @param currency Payment currency
     * @param status Payment status
     * @return List of payments with the specified currency and status
     */
    List<PaymentEntity> findByCurrencyAndStatus(String currency, PaymentStatus status);
    
    /**
     * Find payments created after a specific date
     * 
     * @param createdAfter Date to filter by
     * @return List of payments created after the date
     */
    List<PaymentEntity> findByCreatedAtAfter(LocalDateTime createdAfter);
    
    /**
     * Find payments created between dates
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of payments created between the dates
     */
    List<PaymentEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find successful payments by user ID
     * 
     * @param userId User ID
     * @return List of successful payments for the user
     */
    @Query("SELECT p FROM PaymentEntity p WHERE p.userId = :userId AND p.status = 'SUCCEEDED'")
    List<PaymentEntity> findSuccessfulPaymentsByUserId(@Param("userId") Long userId);
    
    /**
     * Find failed payments by user ID
     * 
     * @param userId User ID
     * @return List of failed payments for the user
     */
    @Query("SELECT p FROM PaymentEntity p WHERE p.userId = :userId AND p.status = 'FAILED'")
    List<PaymentEntity> findFailedPaymentsByUserId(@Param("userId") Long userId);
    
    /**
     * Find refunded payments by user ID
     * 
     * @param userId User ID
     * @return List of refunded payments for the user
     */
    @Query("SELECT p FROM PaymentEntity p WHERE p.userId = :userId AND (p.status = 'REFUNDED' OR p.status = 'PARTIALLY_REFUNDED')")
    List<PaymentEntity> findRefundedPaymentsByUserId(@Param("userId") Long userId);
    
    /**
     * Find payments by multiple criteria
     * 
     * @param userId User ID (optional)
     * @param status Payment status (optional)
     * @param currency Payment currency (optional)
     * @param minAmount Minimum amount (optional)
     * @param maxAmount Maximum amount (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return List of payments matching the criteria
     */
    @Query("SELECT p FROM PaymentEntity p WHERE " +
           "(:userId IS NULL OR p.userId = :userId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:currency IS NULL OR p.currency = :currency) AND " +
           "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR p.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR p.createdAt <= :endDate)")
    List<PaymentEntity> findByCriteria(@Param("userId") Long userId,
                                      @Param("status") PaymentStatus status,
                                      @Param("currency") String currency,
                                      @Param("minAmount") BigDecimal minAmount,
                                      @Param("maxAmount") BigDecimal maxAmount,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count payments by status
     * 
     * @param status Payment status
     * @return Number of payments with the specified status
     */
    long countByStatus(PaymentStatus status);
    
    /**
     * Count payments by user ID
     * 
     * @param userId User ID
     * @return Number of payments for the user
     */
    long countByUserId(Long userId);
    
    /**
     * Count payments by user ID and status
     * 
     * @param userId User ID
     * @param status Payment status
     * @return Number of payments for the user with the specified status
     */
    long countByUserIdAndStatus(Long userId, PaymentStatus status);
    
    /**
     * Count payments by currency
     * 
     * @param currency Payment currency
     * @return Number of payments with the specified currency
     */
    long countByCurrency(String currency);
    
    /**
     * Count payments created after a specific date
     * 
     * @param createdAfter Date to filter by
     * @return Number of payments created after the date
     */
    long countByCreatedAtAfter(LocalDateTime createdAfter);
    
    /**
     * Sum total amount by user ID
     * 
     * @param userId User ID
     * @return Total amount for the user
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.userId = :userId AND p.status = 'SUCCEEDED'")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);
    
    /**
     * Sum total amount by user ID and currency
     * 
     * @param userId User ID
     * @param currency Payment currency
     * @return Total amount for the user in the specified currency
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.userId = :userId AND p.currency = :currency AND p.status = 'SUCCEEDED'")
    BigDecimal sumTotalAmountByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);
    
    /**
     * Sum total refunded amount by user ID
     * 
     * @param userId User ID
     * @return Total refunded amount for the user
     */
    @Query("SELECT COALESCE(SUM(p.refundedAmount), 0) FROM PaymentEntity p WHERE p.userId = :userId")
    BigDecimal sumTotalRefundedAmountByUserId(@Param("userId") Long userId);
    
    /**
     * Get payment statistics by status
     * 
     * @return List of payment status statistics
     */
    @Query("SELECT p.status, COUNT(p), COALESCE(SUM(p.amount), 0) FROM PaymentEntity p GROUP BY p.status")
    List<Object[]> getPaymentStatisticsByStatus();
    
    /**
     * Get payment statistics by currency
     * 
     * @return List of payment currency statistics
     */
    @Query("SELECT p.currency, COUNT(p), COALESCE(SUM(p.amount), 0) FROM PaymentEntity p GROUP BY p.currency")
    List<Object[]> getPaymentStatisticsByCurrency();
    
    /**
     * Get payment statistics by creation date
     * 
     * @return List of payment creation date statistics
     */
    @Query("SELECT DATE(p.createdAt) as creationDate, COUNT(p), COALESCE(SUM(p.amount), 0) FROM PaymentEntity p GROUP BY DATE(p.createdAt) ORDER BY creationDate")
    List<Object[]> getPaymentStatisticsByCreationDate();
    
    /**
     * Check if payment exists by Stripe payment intent ID
     * 
     * @param stripePaymentIntentId Stripe payment intent ID
     * @return true if payment exists
     */
    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);
    
    /**
     * Delete payments by status
     * 
     * @param status Payment status
     * @return Number of deleted payments
     */
    int deleteByStatus(PaymentStatus status);
    
    /**
     * Delete payments by user ID
     * 
     * @param userId User ID
     * @return Number of deleted payments
     */
    int deleteByUserId(Long userId);
}
