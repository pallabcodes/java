package com.netflix.springframework.demo.integration;

import com.netflix.springframework.demo.entity.*;
import com.netflix.springframework.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Testcontainers Performance Tests
 * 
 * This test class demonstrates Netflix production-grade Testcontainers performance testing:
 * 1. Real database performance testing with Testcontainers
 * 2. Concurrent database operations testing
 * 3. Database connection pooling testing
 * 4. Transaction performance testing
 * 5. Memory and resource usage testing
 * 
 * For C/C++ engineers:
 * - Performance tests are like benchmarking in C++
 * - Concurrent testing is like multi-threaded testing in C++
 * - Connection pooling is like resource pooling in C++
 * - Memory testing is like memory usage monitoring in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("Testcontainers Performance Tests")
class TestcontainersPerformanceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("netflix_perf_test_db")
            .withUsername("perf_test_user")
            .withPassword("perf_test_password")
            .withInitScript("init-performance-data.sql")
            .withReuse(true);
    
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
    }
    
    @Test
    @DisplayName("Should handle high-volume user creation with real database")
    void shouldHandleHighVolumeUserCreationWithRealDatabase() {
        // Given
        int userCount = 1000;
        long startTime = System.currentTimeMillis();
        
        // When
        List<UserEntity> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            UserEntity user = new UserEntity();
            user.setName("User " + i);
            user.setEmail("user" + i + "@netflix.com");
            user.setAge(20 + (i % 50));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setVersion(0L);
            users.add(user);
        }
        
        userJpaRepository.saveAll(users);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(10000); // Should complete within 10 seconds
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).hasSize(userCount);
        
        // Performance metrics
        double throughput = (double) userCount / (duration / 1000.0);
        assertThat(throughput).isGreaterThan(100); // Should handle at least 100 users per second
    }
    
    @Test
    @DisplayName("Should handle concurrent user creation with real database")
    void shouldHandleConcurrentUserCreationWithRealDatabase() throws InterruptedException {
        // Given
        int threadCount = 10;
        int usersPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        // When
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < usersPerThread; j++) {
                    UserEntity user = new UserEntity();
                    user.setName("User " + threadId + "-" + j);
                    user.setEmail("user" + threadId + "-" + j + "@netflix.com");
                    user.setAge(20 + (j % 50));
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    user.setVersion(0L);
                    userJpaRepository.save(user);
                }
            }, executor);
        }
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(15000); // Should complete within 15 seconds
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).hasSize(threadCount * usersPerThread);
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle high-volume payment creation with real database")
    void shouldHandleHighVolumePaymentCreationWithRealDatabase() {
        // Given
        int paymentCount = 500;
        long startTime = System.currentTimeMillis();
        
        // When
        List<PaymentEntity> payments = new ArrayList<>();
        for (int i = 0; i < paymentCount; i++) {
            PaymentEntity payment = new PaymentEntity();
            payment.setUserId((long) (i % 100) + 1);
            payment.setStripePaymentIntentId("pi_test_" + i);
            payment.setAmount(new BigDecimal("100.00"));
            payment.setCurrency("USD");
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setVersion(0L);
            payments.add(payment);
        }
        
        paymentRepository.saveAll(payments);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(8000); // Should complete within 8 seconds
        List<PaymentEntity> allPayments = paymentRepository.findAll();
        assertThat(allPayments).hasSize(paymentCount);
    }
    
    @Test
    @DisplayName("Should handle concurrent payment creation with real database")
    void shouldHandleConcurrentPaymentCreationWithRealDatabase() throws InterruptedException {
        // Given
        int threadCount = 5;
        int paymentsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        // When
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < paymentsPerThread; j++) {
                    PaymentEntity payment = new PaymentEntity();
                    payment.setUserId((long) (threadId * 10 + j));
                    payment.setStripePaymentIntentId("pi_test_" + threadId + "-" + j);
                    payment.setAmount(new BigDecimal("100.00"));
                    payment.setCurrency("USD");
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    payment.setCreatedAt(LocalDateTime.now());
                    payment.setUpdatedAt(LocalDateTime.now());
                    payment.setVersion(0L);
                    paymentRepository.save(payment);
                }
            }, executor);
        }
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(10000); // Should complete within 10 seconds
        List<PaymentEntity> allPayments = paymentRepository.findAll();
        assertThat(allPayments).hasSize(threadCount * paymentsPerThread);
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle high-volume subscription creation with real database")
    void shouldHandleHighVolumeSubscriptionCreationWithRealDatabase() {
        // Given
        int subscriptionCount = 300;
        long startTime = System.currentTimeMillis();
        
        // When
        List<SubscriptionEntity> subscriptions = new ArrayList<>();
        for (int i = 0; i < subscriptionCount; i++) {
            SubscriptionEntity subscription = new SubscriptionEntity();
            subscription.setUserId((long) (i % 100) + 1);
            subscription.setStripeSubscriptionId("sub_test_" + i);
            subscription.setPriceId("price_test_" + (i % 10));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setCurrentPeriodStart(LocalDateTime.now());
            subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
            subscription.setCreatedAt(LocalDateTime.now());
            subscription.setUpdatedAt(LocalDateTime.now());
            subscription.setVersion(0L);
            subscriptions.add(subscription);
        }
        
        subscriptionRepository.saveAll(subscriptions);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(6000); // Should complete within 6 seconds
        List<SubscriptionEntity> allSubscriptions = subscriptionRepository.findAll();
        assertThat(allSubscriptions).hasSize(subscriptionCount);
    }
    
    @Test
    @DisplayName("Should handle concurrent subscription creation with real database")
    void shouldHandleConcurrentSubscriptionCreationWithRealDatabase() throws InterruptedException {
        // Given
        int threadCount = 5;
        int subscriptionsPerThread = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        // When
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < subscriptionsPerThread; j++) {
                    SubscriptionEntity subscription = new SubscriptionEntity();
                    subscription.setUserId((long) (threadId * 10 + j));
                    subscription.setStripeSubscriptionId("sub_test_" + threadId + "-" + j);
                    subscription.setPriceId("price_test_" + (j % 5));
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                    subscription.setCurrentPeriodStart(LocalDateTime.now());
                    subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
                    subscription.setCreatedAt(LocalDateTime.now());
                    subscription.setUpdatedAt(LocalDateTime.now());
                    subscription.setVersion(0L);
                    subscriptionRepository.save(subscription);
                }
            }, executor);
        }
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(8000); // Should complete within 8 seconds
        List<SubscriptionEntity> allSubscriptions = subscriptionRepository.findAll();
        assertThat(allSubscriptions).hasSize(threadCount * subscriptionsPerThread);
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle mixed operations performance with real database")
    void shouldHandleMixedOperationsPerformanceWithRealDatabase() throws InterruptedException {
        // Given
        int operationCount = 200;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        long startTime = System.currentTimeMillis();
        
        // When - Mix of create, read, update operations
        CompletableFuture<Void>[] futures = new CompletableFuture[operationCount];
        for (int i = 0; i < operationCount; i++) {
            final int operationId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                int operationType = operationId % 3;
                switch (operationType) {
                    case 0: // Create user
                        UserEntity user = new UserEntity();
                        user.setName("User " + operationId);
                        user.setEmail("user" + operationId + "@netflix.com");
                        user.setAge(20 + (operationId % 50));
                        user.setCreatedAt(LocalDateTime.now());
                        user.setUpdatedAt(LocalDateTime.now());
                        user.setVersion(0L);
                        userJpaRepository.save(user);
                        break;
                    case 1: // Create payment
                        PaymentEntity payment = new PaymentEntity();
                        payment.setUserId((long) (operationId % 50) + 1);
                        payment.setStripePaymentIntentId("pi_test_" + operationId);
                        payment.setAmount(new BigDecimal("100.00"));
                        payment.setCurrency("USD");
                        payment.setStatus(PaymentStatus.SUCCEEDED);
                        payment.setCreatedAt(LocalDateTime.now());
                        payment.setUpdatedAt(LocalDateTime.now());
                        payment.setVersion(0L);
                        paymentRepository.save(payment);
                        break;
                    case 2: // Create subscription
                        SubscriptionEntity subscription = new SubscriptionEntity();
                        subscription.setUserId((long) (operationId % 50) + 1);
                        subscription.setStripeSubscriptionId("sub_test_" + operationId);
                        subscription.setPriceId("price_test_" + (operationId % 5));
                        subscription.setStatus(SubscriptionStatus.ACTIVE);
                        subscription.setCurrentPeriodStart(LocalDateTime.now());
                        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
                        subscription.setCreatedAt(LocalDateTime.now());
                        subscription.setUpdatedAt(LocalDateTime.now());
                        subscription.setVersion(0L);
                        subscriptionRepository.save(subscription);
                        break;
                }
            }, executor);
        }
        
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(12000); // Should complete within 12 seconds
        
        // Verify all operations completed
        List<UserEntity> allUsers = userJpaRepository.findAll();
        List<PaymentEntity> allPayments = paymentRepository.findAll();
        List<SubscriptionEntity> allSubscriptions = subscriptionRepository.findAll();
        
        assertThat(allUsers.size() + allPayments.size() + allSubscriptions.size()).isEqualTo(operationCount);
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle database connection pooling performance")
    void shouldHandleDatabaseConnectionPoolingPerformance() {
        // Given
        int operationCount = 100;
        long startTime = System.currentTimeMillis();
        
        // When - Perform multiple database operations to test connection pooling
        for (int i = 0; i < operationCount; i++) {
            UserEntity user = new UserEntity();
            user.setName("User " + i);
            user.setEmail("user" + i + "@netflix.com");
            user.setAge(20 + (i % 50));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setVersion(0L);
            userJpaRepository.save(user);
            
            Optional<UserEntity> retrievedUser = userJpaRepository.findById(user.getId());
            assertThat(retrievedUser).isPresent();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        
        // Performance metrics
        double throughput = (double) operationCount / (duration / 1000.0);
        assertThat(throughput).isGreaterThan(20); // Should handle at least 20 operations per second
    }
    
    @Test
    @DisplayName("Should handle memory usage under load with real database")
    void shouldHandleMemoryUsageUnderLoadWithRealDatabase() {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When - Create many entities to test memory usage
        for (int i = 0; i < 500; i++) {
            UserEntity user = new UserEntity();
            user.setName("User " + i);
            user.setEmail("user" + i + "@netflix.com");
            user.setAge(20 + (i % 50));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setVersion(0L);
            userJpaRepository.save(user);
        }
        
        // Force garbage collection
        System.gc();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        // Then
        assertThat(memoryUsed).isLessThan(100 * 1024 * 1024); // Should use less than 100MB additional memory
        
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).hasSize(500);
    }
    
    @Test
    @DisplayName("Should handle database transaction performance")
    void shouldHandleDatabaseTransactionPerformance() {
        // Given
        int transactionCount = 50;
        long startTime = System.currentTimeMillis();
        
        // When - Perform multiple transactions
        for (int i = 0; i < transactionCount; i++) {
            UserEntity user = new UserEntity();
            user.setName("User " + i);
            user.setEmail("user" + i + "@netflix.com");
            user.setAge(20 + (i % 50));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setVersion(0L);
            userJpaRepository.save(user);
            
            PaymentEntity payment = new PaymentEntity();
            payment.setUserId(user.getId());
            payment.setStripePaymentIntentId("pi_test_" + i);
            payment.setAmount(new BigDecimal("100.00"));
            payment.setCurrency("USD");
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setVersion(0L);
            paymentRepository.save(payment);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(8000); // Should complete within 8 seconds
        
        // Verify all transactions completed
        List<UserEntity> allUsers = userJpaRepository.findAll();
        List<PaymentEntity> allPayments = paymentRepository.findAll();
        assertThat(allUsers).hasSize(transactionCount);
        assertThat(allPayments).hasSize(transactionCount);
    }
}
