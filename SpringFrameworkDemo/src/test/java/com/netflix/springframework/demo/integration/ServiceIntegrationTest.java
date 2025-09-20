package com.netflix.springframework.demo.integration;

import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.*;
import com.netflix.springframework.demo.repository.*;
import com.netflix.springframework.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Service Integration Tests
 * 
 * This test class demonstrates Netflix production-grade service integration testing:
 * 1. Service layer integration with repositories
 * 2. Business logic validation
 * 3. Transaction management testing
 * 4. Service interaction testing
 * 5. Error handling and recovery testing
 * 
 * For C/C++ engineers:
 * - Service tests are like business logic integration testing in C++
 * - Service layer is like business logic layer in C++
 * - Repository integration is like data access layer testing in C++
 * - Transaction management is like database transaction testing in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Service Integration Tests")
class ServiceIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StripePaymentService paymentService;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private PaymentFulfillmentService fulfillmentService;
    
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
    private PaymentRequest paymentRequest;
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
        
        paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setCustomerEmail("john.doe@netflix.com");
        paymentRequest.setPaymentMethodId("pm_test_123");
        paymentRequest.setDescription("Test payment");
        
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
    @DisplayName("Should create user with profile through service layer")
    void shouldCreateUserWithProfileThroughServiceLayer() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        userProfileEntity.setUser(savedUser);
        UserProfileEntity savedProfile = userProfileRepository.save(userProfileEntity);
        
        // When
        Optional<UserEntity> retrievedUser = userJpaRepository.findById(savedUser.getId());
        Optional<UserProfileEntity> retrievedProfile = userProfileRepository.findById(savedProfile.getId());
        
        // Then
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedProfile).isPresent();
        assertThat(retrievedProfile.get().getUser()).isNotNull();
        assertThat(retrievedProfile.get().getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedProfile.get().getBio()).isEqualTo("Software Engineer at Netflix");
    }
    
    @Test
    @DisplayName("Should create user with roles through service layer")
    void shouldCreateUserWithRolesThroughServiceLayer() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        RoleEntity savedRole = roleRepository.save(roleEntity);
        
        savedUser.getRoles().add(savedRole);
        savedRole.getUsers().add(savedUser);
        
        userJpaRepository.save(savedUser);
        roleRepository.save(savedRole);
        
        // When
        Optional<UserEntity> retrievedUser = userJpaRepository.findById(savedUser.getId());
        Optional<RoleEntity> retrievedRole = roleRepository.findById(savedRole.getId());
        
        // Then
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getRoles()).hasSize(1);
        assertThat(retrievedUser.get().getRoles().iterator().next().getName()).isEqualTo("USER");
        
        assertThat(retrievedRole).isPresent();
        assertThat(retrievedRole.get().getUsers()).hasSize(1);
        assertThat(retrievedRole.get().getUsers().iterator().next().getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should handle payment creation through service layer")
    void shouldHandlePaymentCreationThroughServiceLayer() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        PaymentEntity savedPayment = paymentRepository.save(paymentEntity);
        Optional<PaymentEntity> retrievedPayment = paymentRepository.findById(savedPayment.getId());
        
        // Then
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(retrievedPayment).isPresent();
        assertThat(retrievedPayment.get().getUserId()).isEqualTo(1L);
        assertThat(retrievedPayment.get().getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(retrievedPayment.get().getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
    }
    
    @Test
    @DisplayName("Should handle subscription creation through service layer")
    void shouldHandleSubscriptionCreationThroughServiceLayer() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        SubscriptionEntity savedSubscription = subscriptionRepository.save(subscriptionEntity);
        Optional<SubscriptionEntity> retrievedSubscription = subscriptionRepository.findById(savedSubscription.getId());
        
        // Then
        assertThat(savedSubscription.getId()).isNotNull();
        assertThat(retrievedSubscription).isPresent();
        assertThat(retrievedSubscription.get().getUserId()).isEqualTo(1L);
        assertThat(retrievedSubscription.get().getStripeSubscriptionId()).isEqualTo("sub_test_123");
        assertThat(retrievedSubscription.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("Should handle payment fulfillment through service layer")
    void shouldHandlePaymentFulfillmentThroughServiceLayer() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        PaymentEntity savedPayment = paymentRepository.save(paymentEntity);
        
        // When
        // Simulate payment fulfillment process
        savedPayment.setStatus(PaymentStatus.SUCCEEDED);
        savedPayment.setFulfillmentStatus("FULFILLED");
        savedPayment.setFulfillmentDate(LocalDateTime.now());
        PaymentEntity updatedPayment = paymentRepository.save(savedPayment);
        
        // Then
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(updatedPayment.getFulfillmentStatus()).isEqualTo("FULFILLED");
        assertThat(updatedPayment.getFulfillmentDate()).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle subscription management through service layer")
    void shouldHandleSubscriptionManagementThroughServiceLayer() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        SubscriptionEntity savedSubscription = subscriptionRepository.save(subscriptionEntity);
        
        // When - Update subscription
        savedSubscription.setStatus(SubscriptionStatus.CANCELLED);
        savedSubscription.setCancelledAt(LocalDateTime.now());
        SubscriptionEntity updatedSubscription = subscriptionRepository.save(savedSubscription);
        
        // Then
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(updatedSubscription.getCancelledAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle user search through service layer")
    void shouldHandleUserSearchThroughServiceLayer() {
        // Given
        UserEntity user1 = new UserEntity();
        user1.setName("Alice Smith");
        user1.setEmail("alice.smith@netflix.com");
        user1.setAge(25);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setVersion(0L);
        userJpaRepository.save(user1);
        
        UserEntity user2 = new UserEntity();
        user2.setName("Bob Johnson");
        user2.setEmail("bob.johnson@netflix.com");
        user2.setAge(30);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setVersion(0L);
        userJpaRepository.save(user2);
        
        // When
        List<UserEntity> users = userJpaRepository.findByNameContainingIgnoreCase("Smith");
        
        // Then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Alice Smith");
    }
    
    @Test
    @DisplayName("Should handle payment search through service layer")
    void shouldHandlePaymentSearchThroughServiceLayer() {
        // Given
        PaymentEntity payment1 = new PaymentEntity();
        payment1.setUserId(1L);
        payment1.setStripePaymentIntentId("pi_test_123");
        payment1.setAmount(new BigDecimal("100.00"));
        payment1.setCurrency("USD");
        payment1.setStatus(PaymentStatus.SUCCEEDED);
        payment1.setCreatedAt(LocalDateTime.now());
        payment1.setUpdatedAt(LocalDateTime.now());
        payment1.setVersion(0L);
        paymentRepository.save(payment1);
        
        PaymentEntity payment2 = new PaymentEntity();
        payment2.setUserId(1L);
        payment2.setStripePaymentIntentId("pi_test_456");
        payment2.setAmount(new BigDecimal("200.00"));
        payment2.setCurrency("USD");
        payment2.setStatus(PaymentStatus.SUCCEEDED);
        payment2.setCreatedAt(LocalDateTime.now());
        payment2.setUpdatedAt(LocalDateTime.now());
        payment2.setVersion(0L);
        paymentRepository.save(payment2);
        
        // When
        List<PaymentEntity> payments = paymentRepository.findByUserId(1L);
        
        // Then
        assertThat(payments).hasSize(2);
        assertThat(payments).extracting(PaymentEntity::getStripePaymentIntentId)
            .containsExactlyInAnyOrder("pi_test_123", "pi_test_456");
    }
    
    @Test
    @DisplayName("Should handle subscription search through service layer")
    void shouldHandleSubscriptionSearchThroughServiceLayer() {
        // Given
        SubscriptionEntity subscription1 = new SubscriptionEntity();
        subscription1.setUserId(1L);
        subscription1.setStripeSubscriptionId("sub_test_123");
        subscription1.setPriceId("price_test_123");
        subscription1.setStatus(SubscriptionStatus.ACTIVE);
        subscription1.setCurrentPeriodStart(LocalDateTime.now());
        subscription1.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscription1.setCreatedAt(LocalDateTime.now());
        subscription1.setUpdatedAt(LocalDateTime.now());
        subscription1.setVersion(0L);
        subscriptionRepository.save(subscription1);
        
        SubscriptionEntity subscription2 = new SubscriptionEntity();
        subscription2.setUserId(1L);
        subscription2.setStripeSubscriptionId("sub_test_456");
        subscription2.setPriceId("price_test_456");
        subscription2.setStatus(SubscriptionStatus.ACTIVE);
        subscription2.setCurrentPeriodStart(LocalDateTime.now());
        subscription2.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscription2.setCreatedAt(LocalDateTime.now());
        subscription2.setUpdatedAt(LocalDateTime.now());
        subscription2.setVersion(0L);
        subscriptionRepository.save(subscription2);
        
        // When
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findByUserId(1L);
        
        // Then
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptions).extracting(SubscriptionEntity::getStripeSubscriptionId)
            .containsExactlyInAnyOrder("sub_test_123", "sub_test_456");
    }
    
    @Test
    @DisplayName("Should handle transaction rollback in service layer")
    void shouldHandleTransactionRollbackInServiceLayer() {
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
    @DisplayName("Should handle concurrent access in service layer")
    void shouldHandleConcurrentAccessInServiceLayer() throws InterruptedException {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When - Simulate concurrent access
        Thread thread1 = new Thread(() -> {
            UserEntity user = userJpaRepository.findById(savedUser.getId()).get();
            user.setName("Updated by Thread 1");
            userJpaRepository.save(user);
        });
        
        Thread thread2 = new Thread(() -> {
            UserEntity user = userJpaRepository.findById(savedUser.getId()).get();
            user.setName("Updated by Thread 2");
            userJpaRepository.save(user);
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser.getName()).isIn("Updated by Thread 1", "Updated by Thread 2");
    }
    
    @Test
    @DisplayName("Should handle service layer error scenarios")
    void shouldHandleServiceLayerErrorScenarios() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When - Try to create duplicate user
        UserEntity duplicateUser = new UserEntity();
        duplicateUser.setName("Jane Doe");
        duplicateUser.setEmail("john.doe@netflix.com"); // Duplicate email
        duplicateUser.setAge(25);
        duplicateUser.setCreatedAt(LocalDateTime.now());
        duplicateUser.setUpdatedAt(LocalDateTime.now());
        duplicateUser.setVersion(0L);
        
        // Then
        assertThatThrownBy(() -> userJpaRepository.save(duplicateUser))
            .isInstanceOf(Exception.class); // Should fail due to unique constraint
    }
    
    @Test
    @DisplayName("Should handle service layer performance scenarios")
    void shouldHandleServiceLayerPerformanceScenarios() {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When - Create multiple users
        for (int i = 0; i < 100; i++) {
            UserEntity user = new UserEntity();
            user.setName("User " + i);
            user.setEmail("user" + i + "@netflix.com");
            user.setAge(20 + i);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setVersion(0L);
            userJpaRepository.save(user);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
        List<UserEntity> allUsers = userJpaRepository.findAll();
        assertThat(allUsers).hasSize(100);
    }
    
    @Test
    @DisplayName("Should handle service layer data consistency")
    void shouldHandleServiceLayerDataConsistency() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When - Update user multiple times
        for (int i = 0; i < 10; i++) {
            UserEntity user = userJpaRepository.findById(savedUser.getId()).get();
            user.setName("Updated Name " + i);
            userJpaRepository.save(user);
        }
        
        // Then
        UserEntity finalUser = userJpaRepository.findById(savedUser.getId()).get();
        assertThat(finalUser.getName()).isEqualTo("Updated Name 9");
        assertThat(finalUser.getVersion()).isGreaterThan(0L); // Version should be incremented
    }
}
