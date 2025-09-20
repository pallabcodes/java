package com.netflix.springframework.demo.integration;

import com.netflix.springframework.demo.entity.*;
import com.netflix.springframework.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository Integration Tests
 * 
 * This test class demonstrates Netflix production-grade repository integration testing:
 * 1. JPA repository testing with real database
 * 2. Entity relationship testing
 * 3. Database constraint validation
 * 4. Transaction management testing
 * 5. Query performance testing
 * 
 * For C/C++ engineers:
 * - Repository tests are like database integration testing in C++
 * - JPA repositories are like ORM layer testing in C++
 * - Entity relationships are like foreign key testing in C++
 * - Transactions are like database transaction testing in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Repository Integration Tests")
class RepositoryIntegrationTest {
    
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
    
    @Autowired
    private UserAdvancedRepository userAdvancedRepository;
    
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
    @DisplayName("Should save and retrieve user entity")
    void shouldSaveAndRetrieveUserEntity() {
        // When
        UserEntity savedUser = userJpaRepository.save(userEntity);
        Optional<UserEntity> retrievedUser = userJpaRepository.findById(savedUser.getId());
        
        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getName()).isEqualTo("John Doe");
        assertThat(retrievedUser.get().getEmail()).isEqualTo("john.doe@netflix.com");
        assertThat(retrievedUser.get().getAge()).isEqualTo(30);
    }
    
    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        Optional<UserEntity> foundUser = userJpaRepository.findByEmail("john.doe@netflix.com");
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should find users by name containing")
    void shouldFindUsersByNameContaining() {
        // Given
        userJpaRepository.save(userEntity);
        
        UserEntity user2 = new UserEntity();
        user2.setName("Jane Doe");
        user2.setEmail("jane.doe@netflix.com");
        user2.setAge(25);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setVersion(0L);
        userJpaRepository.save(user2);
        
        // When
        List<UserEntity> users = userJpaRepository.findByNameContainingIgnoreCase("Doe");
        
        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserEntity::getName)
            .containsExactlyInAnyOrder("John Doe", "Jane Doe");
    }
    
    @Test
    @DisplayName("Should find users with pagination")
    void shouldFindUsersWithPagination() {
        // Given
        for (int i = 0; i < 25; i++) {
            UserEntity user = new UserEntity();
            user.setName("User " + i);
            user.setEmail("user" + i + "@netflix.com");
            user.setAge(20 + i);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setVersion(0L);
            userJpaRepository.save(user);
        }
        
        // When
        Page<UserEntity> firstPage = userJpaRepository.findAll(PageRequest.of(0, 10));
        Page<UserEntity> secondPage = userJpaRepository.findAll(PageRequest.of(1, 10));
        
        // Then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(secondPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(25);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Should find users with sorting")
    void shouldFindUsersWithSorting() {
        // Given
        UserEntity user1 = new UserEntity();
        user1.setName("Alice");
        user1.setEmail("alice@netflix.com");
        user1.setAge(25);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setVersion(0L);
        userJpaRepository.save(user1);
        
        UserEntity user2 = new UserEntity();
        user2.setName("Bob");
        user2.setEmail("bob@netflix.com");
        user2.setAge(30);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setVersion(0L);
        userJpaRepository.save(user2);
        
        // When
        List<UserEntity> usersAsc = userJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        List<UserEntity> usersDesc = userJpaRepository.findAll(Sort.by(Sort.Direction.DESC, "name"));
        
        // Then
        assertThat(usersAsc).extracting(UserEntity::getName)
            .containsExactly("Alice", "Bob");
        assertThat(usersDesc).extracting(UserEntity::getName)
            .containsExactly("Bob", "Alice");
    }
    
    @Test
    @DisplayName("Should save and retrieve payment entity")
    void shouldSaveAndRetrievePaymentEntity() {
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
    @DisplayName("Should find payment by Stripe ID")
    void shouldFindPaymentByStripeId() {
        // Given
        PaymentEntity savedPayment = paymentRepository.save(paymentEntity);
        
        // When
        Optional<PaymentEntity> foundPayment = paymentRepository.findByStripePaymentIntentId("pi_test_123");
        
        // Then
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getId()).isEqualTo(savedPayment.getId());
        assertThat(foundPayment.get().getStripePaymentIntentId()).isEqualTo("pi_test_123");
    }
    
    @Test
    @DisplayName("Should find payments by user ID")
    void shouldFindPaymentsByUserId() {
        // Given
        paymentRepository.save(paymentEntity);
        
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
    @DisplayName("Should save and retrieve subscription entity")
    void shouldSaveAndRetrieveSubscriptionEntity() {
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
    @DisplayName("Should find subscription by Stripe ID")
    void shouldFindSubscriptionByStripeId() {
        // Given
        SubscriptionEntity savedSubscription = subscriptionRepository.save(subscriptionEntity);
        
        // When
        Optional<SubscriptionEntity> foundSubscription = subscriptionRepository.findByStripeSubscriptionId("sub_test_123");
        
        // Then
        assertThat(foundSubscription).isPresent();
        assertThat(foundSubscription.get().getId()).isEqualTo(savedSubscription.getId());
        assertThat(foundSubscription.get().getStripeSubscriptionId()).isEqualTo("sub_test_123");
    }
    
    @Test
    @DisplayName("Should find subscriptions by user ID")
    void shouldFindSubscriptionsByUserId() {
        // Given
        subscriptionRepository.save(subscriptionEntity);
        
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
    @DisplayName("Should test user-profile 1-to-1 relationship")
    void shouldTestUserProfileOneToOneRelationship() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        userProfileEntity.setUser(savedUser);
        UserProfileEntity savedProfile = userProfileRepository.save(userProfileEntity);
        
        // When
        Optional<UserProfileEntity> retrievedProfile = userProfileRepository.findById(savedProfile.getId());
        
        // Then
        assertThat(retrievedProfile).isPresent();
        assertThat(retrievedProfile.get().getUser()).isNotNull();
        assertThat(retrievedProfile.get().getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedProfile.get().getBio()).isEqualTo("Software Engineer at Netflix");
    }
    
    @Test
    @DisplayName("Should test user-role many-to-many relationship")
    void shouldTestUserRoleManyToManyRelationship() {
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
    @DisplayName("Should test database constraints")
    void shouldTestDatabaseConstraints() {
        // Given
        userJpaRepository.save(userEntity);
        
        UserEntity duplicateUser = new UserEntity();
        duplicateUser.setName("Jane Doe");
        duplicateUser.setEmail("john.doe@netflix.com"); // Duplicate email
        duplicateUser.setAge(25);
        duplicateUser.setCreatedAt(LocalDateTime.now());
        duplicateUser.setUpdatedAt(LocalDateTime.now());
        duplicateUser.setVersion(0L);
        
        // When & Then
        assertThatThrownBy(() -> userJpaRepository.save(duplicateUser))
            .isInstanceOf(Exception.class); // Should fail due to unique constraint
    }
    
    @Test
    @DisplayName("Should test optimistic locking")
    void shouldTestOptimisticLocking() {
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
    @DisplayName("Should test soft delete functionality")
    void shouldTestSoftDeleteFunctionality() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        userJpaRepository.deleteById(savedUser.getId());
        
        // Then
        Optional<UserEntity> deletedUser = userJpaRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty(); // Should not be found due to soft delete
        
        // Verify soft delete timestamp is set
        // Note: This would require a custom query to check deleted_at field
    }
    
    @Test
    @DisplayName("Should test custom query methods")
    void shouldTestCustomQueryMethods() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        List<UserEntity> activeUsers = userJpaRepository.findActiveUsers();
        List<UserEntity> adultUsers = userJpaRepository.findAdultUsers();
        
        // Then
        assertThat(activeUsers).isNotEmpty();
        assertThat(adultUsers).isNotEmpty();
        assertThat(adultUsers).extracting(UserEntity::getAge)
            .allMatch(age -> age >= 18);
    }
    
    @Test
    @DisplayName("Should test advanced repository methods")
    void shouldTestAdvancedRepositoryMethods() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        List<UserEntity> usersBySpecification = userAdvancedRepository.findAll(
            (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("email"), "john.doe@netflix.com")
        );
        
        // Then
        assertThat(usersBySpecification).hasSize(1);
        assertThat(usersBySpecification.get(0).getEmail()).isEqualTo("john.doe@netflix.com");
    }
    
    @Test
    @DisplayName("Should test transaction rollback")
    void shouldTestTransactionRollback() {
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
    @DisplayName("Should test concurrent access")
    void shouldTestConcurrentAccess() throws InterruptedException {
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
}
