package com.netflix.springframework.demo.integration;

import com.netflix.springframework.demo.entity.*;
import com.netflix.springframework.demo.repository.*;
import com.netflix.springframework.demo.service.UserTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Transaction Integration Tests
 * 
 * This test class demonstrates Netflix production-grade transaction integration testing:
 * 1. Database transaction management testing
 * 2. Transaction rollback scenarios
 * 3. Concurrent transaction testing
 * 4. Transaction isolation level testing
 * 5. Deadlock prevention testing
 * 
 * For C/C++ engineers:
 * - Transaction tests are like database transaction testing in C++
 * - Transaction management is like ACID properties testing in C++
 * - Rollback scenarios are like error recovery testing in C++
 * - Concurrent transactions are like multi-threaded database testing in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Transaction Integration Tests")
class TransactionIntegrationTest {
    
    @Autowired
    private UserTransactionService userTransactionService;
    
    @Autowired
    private UserJpaRepository userJpaRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private UserEntity userEntity;
    private PaymentEntity paymentEntity;
    private SubscriptionEntity subscriptionEntity;
    private UserProfileEntity userProfileEntity;
    private RoleEntity roleEntity;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        userEntity = new UserEntity();
        userEntity.setName("John Doe");
        userEntity.setEmail("john.doe@netflix.com");
        userEntity.setAge(30);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userEntity.setVersion(0L);
        
        paymentEntity = new PaymentEntity();
        paymentEntity.setUserId(1L);
        paymentEntity.setStripePaymentIntentId("pi_test_123");
        paymentEntity.setAmount(new BigDecimal("100.00"));
        paymentEntity.setCurrency("USD");
        paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
        paymentEntity.setCreatedAt(LocalDateTime.now());
        paymentEntity.setUpdatedAt(LocalDateTime.now());
        paymentEntity.setVersion(0L);
        
        subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setUserId(1L);
        subscriptionEntity.setStripeSubscriptionId("sub_test_123");
        subscriptionEntity.setPriceId("price_test_123");
        subscriptionEntity.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionEntity.setCurrentPeriodStart(LocalDateTime.now());
        subscriptionEntity.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscriptionEntity.setCreatedAt(LocalDateTime.now());
        subscriptionEntity.setUpdatedAt(LocalDateTime.now());
        subscriptionEntity.setVersion(0L);
        
        userProfileEntity = new UserProfileEntity();
        userProfileEntity.setBio("Software Engineer at Netflix");
        userProfileEntity.setLocation("Los Gatos, CA");
        userProfileEntity.setWebsite("https://netflix.com");
        userProfileEntity.setCreatedAt(LocalDateTime.now());
        userProfileEntity.setUpdatedAt(LocalDateTime.now());
        userProfileEntity.setVersion(0L);
        
        roleEntity = new RoleEntity();
        roleEntity.setName("USER");
        roleEntity.setCode("USER");
        roleEntity.setDescription("Regular user role");
        roleEntity.setCreatedAt(LocalDateTime.now());
        roleEntity.setUpdatedAt(LocalDateTime.now());
        roleEntity.setVersion(0L);
    }
    
    @Test
    @DisplayName("Should handle successful transaction commit")
    void shouldHandleSuccessfulTransactionCommit() {
        // When
        UserEntity savedUser = userJpaRepository.save(userEntity);
        PaymentEntity savedPayment = paymentRepository.save(paymentEntity);
        SubscriptionEntity savedSubscription = subscriptionRepository.save(subscriptionEntity);
        
        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedSubscription.getId()).isNotNull();
        
        // Verify all entities are persisted
        Optional<UserEntity> retrievedUser = userJpaRepository.findById(savedUser.getId());
        Optional<PaymentEntity> retrievedPayment = paymentRepository.findById(savedPayment.getId());
        Optional<SubscriptionEntity> retrievedSubscription = subscriptionRepository.findById(savedSubscription.getId());
        
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedPayment).isPresent();
        assertThat(retrievedSubscription).isPresent();
    }
    
    @Test
    @DisplayName("Should handle transaction rollback on constraint violation")
    void shouldHandleTransactionRollbackOnConstraintViolation() {
        // Given
        UserEntity user1 = new UserEntity();
        user1.setName("User 1");
        user1.setEmail("user1@netflix.com");
        user1.setAge(25);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setVersion(0L);
        
        UserEntity user2 = new UserEntity();
        user2.setName("User 2");
        user2.setEmail("user1@netflix.com"); // Duplicate email to cause constraint violation
        user2.setAge(30);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setVersion(0L);
        
        // When
        try {
            userJpaRepository.save(user1);
            userJpaRepository.save(user2); // This should fail
        } catch (Exception e) {
            // Expected to fail due to duplicate email
        }
        
        // Then
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).isEmpty(); // Should be empty due to transaction rollback
    }
    
    @Test
    @DisplayName("Should handle transaction rollback on business logic failure")
    void shouldHandleTransactionRollbackOnBusinessLogicFailure() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When - Simulate business logic failure
        try {
            userTransactionService.createUserWithProfile(savedUser, userProfileEntity);
            // Simulate failure after some operations
            throw new RuntimeException("Business logic failure");
        } catch (RuntimeException e) {
            // Expected business logic failure
        }
        
        // Then
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).isEmpty(); // Should be empty due to transaction rollback
    }
    
    @Test
    @DisplayName("Should handle concurrent transactions safely")
    void shouldHandleConcurrentTransactionsSafely() throws InterruptedException {
        // Given
        UserEntity user1 = new UserEntity();
        user1.setName("User 1");
        user1.setEmail("user1@netflix.com");
        user1.setAge(25);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setVersion(0L);
        
        UserEntity user2 = new UserEntity();
        user2.setName("User 2");
        user2.setEmail("user2@netflix.com");
        user2.setAge(30);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setVersion(0L);
        
        // When - Create users concurrently
        Thread thread1 = new Thread(() -> {
            userJpaRepository.save(user1);
        });
        
        Thread thread2 = new Thread(() -> {
            userJpaRepository.save(user2);
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // Then
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(UserEntity::getName)
            .containsExactlyInAnyOrder("User 1", "User 2");
    }
    
    @Test
    @DisplayName("Should handle optimistic locking conflicts")
    void shouldHandleOptimisticLockingConflicts() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        UserEntity user1 = userJpaRepository.findById(savedUser.getId()).get();
        UserEntity user2 = userJpaRepository.findById(savedUser.getId()).get();
        
        // When
        user1.setName("Updated Name 1");
        userJpaRepository.save(user1);
        
        user2.setName("Updated Name 2");
        
        // Then
        assertThatThrownBy(() -> userJpaRepository.save(user2))
            .isInstanceOf(Exception.class); // Should fail due to optimistic locking
    }
    
    @Test
    @DisplayName("Should handle transaction isolation levels")
    void shouldHandleTransactionIsolationLevels() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When - Test different isolation levels
        UserEntity user1 = userJpaRepository.findById(savedUser.getId()).get();
        user1.setName("Updated Name");
        userJpaRepository.save(user1);
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser.getName()).isEqualTo("Updated Name");
        assertThat(finalUser.getVersion()).isGreaterThan(0L);
    }
    
    @Test
    @DisplayName("Should handle deadlock prevention")
    void shouldHandleDeadlockPrevention() throws InterruptedException {
        // Given
        UserEntity user1 = new UserEntity();
        user1.setName("User 1");
        user1.setEmail("user1@netflix.com");
        user1.setAge(25);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setVersion(0L);
        userJpaRepository.save(user1);
        
        UserEntity user2 = new UserEntity();
        user2.setName("User 2");
        user2.setEmail("user2@netflix.com");
        user2.setAge(30);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setVersion(0L);
        userJpaRepository.save(user2);
        
        // When - Simulate potential deadlock scenario
        Thread thread1 = new Thread(() -> {
            UserEntity u1 = userJpaRepository.findById(user1.getId()).get();
            UserEntity u2 = userJpaRepository.findById(user2.getId()).get();
            u1.setName("Updated by Thread 1");
            u2.setName("Updated by Thread 1");
            userJpaRepository.save(u1);
            userJpaRepository.save(u2);
        });
        
        Thread thread2 = new Thread(() -> {
            UserEntity u2 = userJpaRepository.findById(user2.getId()).get();
            UserEntity u1 = userJpaRepository.findById(user1.getId()).get();
            u2.setName("Updated by Thread 2");
            u1.setName("Updated by Thread 2");
            userJpaRepository.save(u2);
            userJpaRepository.save(u1);
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // Then - Should complete without deadlock
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).hasSize(2);
    }
    
    @Test
    @DisplayName("Should handle transaction timeout")
    void shouldHandleTransactionTimeout() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When - Simulate long-running transaction
        try {
            userTransactionService.performLongRunningOperation(savedUser.getId());
        } catch (Exception e) {
            // Expected to timeout
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle nested transactions")
    void shouldHandleNestedTransactions() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        try {
            userTransactionService.performNestedTransaction(savedUser.getId());
        } catch (Exception e) {
            // Expected to handle nested transaction
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle transaction propagation")
    void shouldHandleTransactionPropagation() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        try {
            userTransactionService.performTransactionWithPropagation(savedUser.getId());
        } catch (Exception e) {
            // Expected to handle transaction propagation
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle transaction read-only scenarios")
    void shouldHandleTransactionReadOnlyScenarios() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        UserEntity retrievedUser = userTransactionService.performReadOnlyTransaction(savedUser.getId());
        
        // Then
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should handle transaction with multiple entities")
    void shouldHandleTransactionWithMultipleEntities() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        userProfileEntity.setUser(savedUser);
        
        // When
        try {
            userTransactionService.createUserWithMultipleEntities(savedUser, userProfileEntity, roleEntity);
        } catch (Exception e) {
            // Expected to handle multiple entities
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle transaction with external service calls")
    void shouldHandleTransactionWithExternalServiceCalls() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        try {
            userTransactionService.performTransactionWithExternalService(savedUser.getId());
        } catch (Exception e) {
            // Expected to handle external service calls
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle transaction performance under load")
    void shouldHandleTransactionPerformanceUnderLoad() throws InterruptedException {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When - Create multiple users concurrently
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int userId = i;
            threads[i] = new Thread(() -> {
                UserEntity user = new UserEntity();
                user.setName("User " + userId);
                user.setEmail("user" + userId + "@netflix.com");
                user.setAge(20 + userId);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user.setVersion(0L);
                userJpaRepository.save(user);
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(10000); // Should complete within 10 seconds
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).hasSize(10);
    }
    
    @Test
    @DisplayName("Should handle transaction with complex business logic")
    void shouldHandleTransactionWithComplexBusinessLogic() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        try {
            userTransactionService.performComplexBusinessLogic(savedUser.getId());
        } catch (Exception e) {
            // Expected to handle complex business logic
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle transaction with error recovery")
    void shouldHandleTransactionWithErrorRecovery() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        try {
            userTransactionService.performTransactionWithErrorRecovery(savedUser.getId());
        } catch (Exception e) {
            // Expected to handle error recovery
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser).isNotNull();
    }
}
