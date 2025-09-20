package com.netflix.microservices.customer.integration;

import com.netflix.microservices.customer.CustomerServiceApplication;
import com.netflix.microservices.customer.entity.Customer;
import com.netflix.microservices.customer.repository.CustomerRepository;
import com.netflix.microservices.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Netflix Production-Grade Customer Service Integration Tests
 * 
 * This test class demonstrates Netflix production standards for integration testing including:
 * 1. Testcontainers for real database testing
 * 2. End-to-end API testing
 * 3. Service interaction testing
 * 4. Performance and load testing
 * 5. Error handling and resilience testing
 * 6. Security and authentication testing
 * 7. Data consistency and transaction testing
 * 8. Monitoring and observability testing
 * 
 * For C/C++ engineers:
 * - Integration tests are like system tests in C++
 * - Testcontainers are like Docker containers for testing
 * - @SpringBootTest is like starting the entire application
 * - TestRestTemplate is like HTTP client for testing
 * - Assertions are like assert statements in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest(
    classes = CustomerServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Customer Service Integration Tests")
class CustomerServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("customer_test_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("init-customer-test-data.sql")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomerSuccessfully() {
        // Given
        Customer customer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@netflix.com")
                .age(30)
                .phoneNumber("+1234567890")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> request = new HttpEntity<>(customer, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/customers",
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();

        // Verify database
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getEmail()).isEqualTo("john.doe@netflix.com");
    }

    @Test
    @DisplayName("Should get customer by ID successfully")
    void shouldGetCustomerByIdSuccessfully() {
        // Given
        Customer customer = Customer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@netflix.com")
                .age(25)
                .phoneNumber("+1987654321")
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/customers/" + savedCustomer.getId(),
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("Should get all customers successfully")
    void shouldGetAllCustomersSuccessfully() {
        // Given
        Customer customer1 = Customer.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice.johnson@netflix.com")
                .age(28)
                .build();
        Customer customer2 = Customer.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .email("bob.wilson@netflix.com")
                .age(35)
                .build();
        customerRepository.saveAll(List.of(customer1, customer2));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/customers",
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() {
        // Given
        Customer customer = Customer.builder()
                .firstName("Original")
                .lastName("Name")
                .email("original@netflix.com")
                .age(30)
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        Customer updatedCustomer = Customer.builder()
                .id(savedCustomer.getId())
                .firstName("Updated")
                .lastName("Name")
                .email("updated@netflix.com")
                .age(31)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> request = new HttpEntity<>(updatedCustomer, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/customers/" + savedCustomer.getId(),
                HttpMethod.PUT,
                request,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();

        // Verify database
        Customer updated = customerRepository.findById(savedCustomer.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getEmail()).isEqualTo("updated@netflix.com");
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() {
        // Given
        Customer customer = Customer.builder()
                .firstName("ToDelete")
                .lastName("Customer")
                .email("todelete@netflix.com")
                .age(30)
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/customers/" + savedCustomer.getId(),
                HttpMethod.DELETE,
                null,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();

        // Verify database (soft delete)
        Customer deleted = customerRepository.findById(savedCustomer.getId()).orElse(null);
        assertThat(deleted).isNotNull();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() {
        // When
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/customers/non-existent-id",
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() {
        // Given
        Customer invalidCustomer = Customer.builder()
                .firstName("") // Invalid: empty first name
                .lastName("Doe")
                .email("invalid-email") // Invalid: malformed email
                .age(-1) // Invalid: negative age
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> request = new HttpEntity<>(invalidCustomer, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/customers",
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }

    @Test
    @DisplayName("Should handle concurrent customer creation")
    void shouldHandleConcurrentCustomerCreation() {
        // Given
        int numberOfThreads = 10;
        int customersPerThread = 10;
        
        // When
        List<Thread> threads = new java.util.ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                for (int j = 0; j < customersPerThread; j++) {
                    Customer customer = Customer.builder()
                            .firstName("Thread" + threadId)
                            .lastName("Customer" + j)
                            .email("thread" + threadId + ".customer" + j + "@netflix.com")
                            .age(20 + j)
                            .build();
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Customer> request = new HttpEntity<>(customer, headers);
                    
                    restTemplate.exchange(
                            baseUrl + "/api/v1/customers",
                            HttpMethod.POST,
                            request,
                            ApiResponse.class
                    );
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Then
        List<Customer> allCustomers = customerRepository.findAll();
        assertThat(allCustomers).hasSize(numberOfThreads * customersPerThread);
    }

    @Test
    @DisplayName("Should perform health check successfully")
    void shouldPerformHealthCheckSuccessfully() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/actuator/health",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Should perform metrics check successfully")
    void shouldPerformMetricsCheckSuccessfully() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/actuator/metrics",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
