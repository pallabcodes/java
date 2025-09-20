package com.netflix.springframework.demo.integration;

import com.netflix.springframework.demo.entity.*;
import com.netflix.springframework.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testcontainers Integration Tests
 * 
 * This test class demonstrates Netflix production-grade Testcontainers integration testing:
 * 1. Real PostgreSQL database testing with Testcontainers
 * 2. Database schema validation and migration testing
 * 3. Data persistence and retrieval testing
 * 4. Transaction management with real database
 * 5. Performance testing with real database
 * 
 * For C/C++ engineers:
 * - Testcontainers are like Docker-based testing in C++
 * - Real database testing is like integration testing with actual databases in C++
 * - Container lifecycle management is like resource management in C++
 * - Database schema testing is like database structure validation in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("Testcontainers Integration Tests")
class TestcontainersIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("netflix_test_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("init-test-data.sql");
    
    @Autowired
    private MockMvc mockMvc;
    
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
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
    
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
    @DisplayName("Should verify PostgreSQL container is running")
    void shouldVerifyPostgreSQLContainerIsRunning() {
        // Then
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getDatabaseName()).isEqualTo("netflix_test_db");
        assertThat(postgres.getUsername()).isEqualTo("test_user");
        assertThat(postgres.getPassword()).isEqualTo("test_password");
    }
    
    @Test
    @DisplayName("Should test user entity persistence with real database")
    void shouldTestUserEntityPersistenceWithRealDatabase() {
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
    @DisplayName("Should test payment entity persistence with real database")
    void shouldTestPaymentEntityPersistenceWithRealDatabase() {
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
    @DisplayName("Should test subscription entity persistence with real database")
    void shouldTestSubscriptionEntityPersistenceWithRealDatabase() {
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
    @DisplayName("Should test user-profile 1-to-1 relationship with real database")
    void shouldTestUserProfileOneToOneRelationshipWithRealDatabase() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        userProfileEntity.setUser(savedUser);
        
        // When
        UserProfileEntity savedProfile = userProfileRepository.save(userProfileEntity);
        Optional<UserProfileEntity> retrievedProfile = userProfileRepository.findById(savedProfile.getId());
        
        // Then
        assertThat(savedProfile.getId()).isNotNull();
        assertThat(retrievedProfile).isPresent();
        assertThat(retrievedProfile.get().getUser()).isNotNull();
        assertThat(retrievedProfile.get().getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedProfile.get().getBio()).isEqualTo("Software Engineer at Netflix");
    }
    
    @Test
    @DisplayName("Should test user-role many-to-many relationship with real database")
    void shouldTestUserRoleManyToManyRelationshipWithRealDatabase() {
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
    @DisplayName("Should test database constraints with real database")
    void shouldTestDatabaseConstraintsWithRealDatabase() {
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
    @DisplayName("Should test optimistic locking with real database")
    void shouldTestOptimisticLockingWithRealDatabase() {
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
    @DisplayName("Should test custom query methods with real database")
    void shouldTestCustomQueryMethodsWithRealDatabase() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
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
    @DisplayName("Should test pagination with real database")
    void shouldTestPaginationWithRealDatabase() {
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
        List<UserEntity> firstPage = userJpaRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        List<UserEntity> secondPage = userJpaRepository.findAll(org.springframework.data.domain.PageRequest.of(1, 10)).getContent();
        
        // Then
        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(10);
    }
    
    @Test
    @DisplayName("Should test sorting with real database")
    void shouldTestSortingWithRealDatabase() {
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
        List<UserEntity> usersAsc = userJpaRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name"));
        List<UserEntity> usersDesc = userJpaRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "name"));
        
        // Then
        assertThat(usersAsc).extracting(UserEntity::getName)
            .containsExactly("Alice", "Bob");
        assertThat(usersDesc).extracting(UserEntity::getName)
            .containsExactly("Bob", "Alice");
    }
    
    @Test
    @DisplayName("Should test transaction rollback with real database")
    void shouldTestTransactionRollbackWithRealDatabase() {
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
    @DisplayName("Should test concurrent access with real database")
    void shouldTestConcurrentAccessWithRealDatabase() throws InterruptedException {
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
    @DisplayName("Should test performance with real database")
    void shouldTestPerformanceWithRealDatabase() {
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
    @DisplayName("Should test database schema validation")
    void shouldTestDatabaseSchemaValidation() {
        // Given
        UserEntity savedUser = userJpaRepository.save(userEntity);
        
        // When
        Optional<UserEntity> retrievedUser = userJpaRepository.findById(savedUser.getId());
        
        // Then
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getId()).isNotNull();
        assertThat(retrievedUser.get().getName()).isNotNull();
        assertThat(retrievedUser.get().getEmail()).isNotNull();
        assertThat(retrievedUser.get().getAge()).isNotNull();
        assertThat(retrievedUser.get().getCreatedAt()).isNotNull();
        assertThat(retrievedUser.get().getUpdatedAt()).isNotNull();
        assertThat(retrievedUser.get().getVersion()).isNotNull();
    }
    
    @Test
    @DisplayName("Should test database connection pooling")
    void shouldTestDatabaseConnectionPooling() {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When - Perform multiple database operations
        for (int i = 0; i < 50; i++) {
            UserEntity user = new UserEntity();
            user.setName("User " + i);
            user.setEmail("user" + i + "@netflix.com");
            user.setAge(20 + i);
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
        assertThat(duration).isLessThan(3000); // Should complete within 3 seconds
    }
    
    @Test
    @DisplayName("Should test database migration with Testcontainers")
    void shouldTestDatabaseMigrationWithTestcontainers() {
        // Given - Database should be created with proper schema
        
        // When - Create entities that depend on schema
        UserEntity savedUser = userJpaRepository.save(userEntity);
        PaymentEntity savedPayment = paymentRepository.save(paymentEntity);
        SubscriptionEntity savedSubscription = subscriptionRepository.save(subscriptionEntity);
        
        // Then - All entities should be created successfully
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedSubscription.getId()).isNotNull();
        
        // Verify schema constraints are working
        Optional<UserEntity> retrievedUser = userJpaRepository.findById(savedUser.getId());
        Optional<PaymentEntity> retrievedPayment = paymentRepository.findById(savedPayment.getId());
        Optional<SubscriptionEntity> retrievedSubscription = subscriptionRepository.findById(savedSubscription.getId());
        
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedPayment).isPresent();
        assertThat(retrievedSubscription).isPresent();
    }
}
