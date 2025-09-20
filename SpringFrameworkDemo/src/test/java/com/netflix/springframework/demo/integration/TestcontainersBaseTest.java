package com.netflix.springframework.demo.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Testcontainers Base Test Class
 * 
 * This base class provides Netflix production-grade Testcontainers configuration:
 * 1. PostgreSQL container configuration
 * 2. Dynamic property configuration
 * 3. Container lifecycle management
 * 4. Database connection configuration
 * 5. Test environment setup
 * 
 * For C/C++ engineers:
 * - Base test classes are like common test setup in C++
 * - Container configuration is like Docker setup in C++
 * - Dynamic properties are like environment variable configuration in C++
 * - Lifecycle management is like resource management in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class TestcontainersBaseTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("netflix_test_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("init-test-data.sql")
            .withReuse(true); // Enable container reuse for better performance
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.use_sql_comments", () -> "true");
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> "20");
        registry.add("spring.jpa.properties.hibernate.order_inserts", () -> "true");
        registry.add("spring.jpa.properties.hibernate.order_updates", () -> "true");
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_versioned_data", () -> "true");
    }
    
    /**
     * Get the PostgreSQL container instance
     * 
     * @return PostgreSQL container
     */
    protected static PostgreSQLContainer<?> getPostgresContainer() {
        return postgres;
    }
    
    /**
     * Check if the container is running
     * 
     * @return true if container is running
     */
    protected static boolean isContainerRunning() {
        return postgres.isRunning();
    }
    
    /**
     * Get the database URL
     * 
     * @return database URL
     */
    protected static String getDatabaseUrl() {
        return postgres.getJdbcUrl();
    }
    
    /**
     * Get the database username
     * 
     * @return database username
     */
    protected static String getDatabaseUsername() {
        return postgres.getUsername();
    }
    
    /**
     * Get the database password
     * 
     * @return database password
     */
    protected static String getDatabasePassword() {
        return postgres.getPassword();
    }
    
    /**
     * Get the database name
     * 
     * @return database name
     */
    protected static String getDatabaseName() {
        return postgres.getDatabaseName();
    }
}
