package com.amigoscode.customer;

import com.amigoscode.amqp.RabbitMQMessageProducer;
import com.amigoscode.clients.fraud.FraudClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("Customer Controller - Security Integration Tests")
class CustomerControllerSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private FraudClient fraudClient;

    @MockBean
    private RabbitMQMessageProducer rabbitMQMessageProducer;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should allow registration without authentication")
    void shouldAllowRegistrationWithoutAuth() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John", "Doe", "john.doe@example.com"
        );

        Customer savedCustomer = Customer.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        when(customerRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudClient.isFraudster(anyInt())).thenReturn(new com.amigoscode.clients.fraud.FraudCheckResponse(false));

        // When & Then
        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.message").value("Customer registered successfully"));
    }

    @Test
    @DisplayName("Should reject registration with invalid input")
    void shouldRejectInvalidRegistration() throws Exception {
        // Given - missing required fields
        String invalidRequest = "{\"firstName\":\"\",\"lastName\":\"\",\"email\":\"invalid-email\"}";

        // When & Then
        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with XSS attack")
    void shouldRejectXssAttack() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "<script>alert('xss')</script>", "Doe", "test@example.com"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("REGISTRATION_FAILED"));
    }

    @Test
    @DisplayName("Should require authentication for customer retrieval")
    void shouldRequireAuthForCustomerRetrieval() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Should allow customer retrieval with authentication")
    void shouldAllowCustomerRetrievalWithAuth() throws Exception {
        // Given
        Customer customer = Customer.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        when(customerRepository.findById(1)).thenReturn(java.util.Optional.of(customer));

        // When & Then
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent customer")
    @WithMockUser
    void shouldReturn404ForNonExistentCustomer() throws Exception {
        // Given
        when(customerRepository.findById(999)).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle duplicate email registration")
    void shouldHandleDuplicateEmailRegistration() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "Jane", "Smith", "john.doe@example.com"
        );

        when(customerRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("REGISTRATION_FAILED"));
    }

    @Test
    @DisplayName("Should handle fraudulent registration attempt")
    void shouldHandleFraudulentRegistration() throws Exception {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "Fraud", "User", "fraud@example.com"
        );

        Customer savedCustomer = Customer.builder()
            .id(1)
            .firstName("Fraud")
            .lastName("User")
            .email("fraud@example.com")
            .build();

        when(customerRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudClient.isFraudster(1)).thenReturn(new com.amigoscode.clients.fraud.FraudCheckResponse(true));

        // When & Then
        mockMvc.perform(post("/api/v1/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("REGISTRATION_FAILED"));
    }

    @Test
    @DisplayName("Should provide health check endpoint")
    void shouldProvideHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/customers/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Customer service is healthy"));
    }
}
