package com.amigoscode.customer;

import com.amigoscode.amqp.RabbitMQMessageProducer;
import com.amigoscode.clients.fraud.FraudCheckResponse;
import com.amigoscode.clients.fraud.FraudClient;
import com.amigoscode.clients.notification.NotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service - Security Tests")
class CustomerServiceSecurityTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FraudClient fraudClient;

    @Mock
    private RabbitMQMessageProducer rabbitMQMessageProducer;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, fraudClient, rabbitMQMessageProducer);
    }

    @Test
    @DisplayName("Should reject registration with XSS in firstName")
    void shouldRejectXssInFirstName() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "<script>alert('xss')</script>",
            "Doe",
            "john.doe@example.com"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.registerCustomer(request);
        });

        assertTrue(exception.getMessage().contains("potentially malicious scripts"));
    }

    @Test
    @DisplayName("Should reject registration with XSS in lastName")
    void shouldRejectXssInLastName() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John",
            "<img src=x onerror=alert(1)>",
            "john.doe@example.com"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.registerCustomer(request);
        });

        assertTrue(exception.getMessage().contains("potentially malicious scripts"));
    }

    @Test
    @DisplayName("Should reject registration with SQL injection in email")
    void shouldRejectSqlInjectionInEmail() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John",
            "Doe",
            "john.doe@example.com' UNION SELECT * FROM users--"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.registerCustomer(request);
        });

        assertTrue(exception.getMessage().contains("potentially malicious content"));
    }

    @Test
    @DisplayName("Should sanitize input to prevent XSS")
    void shouldSanitizeInput() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John<script>",
            "Doe<img>",
            "john.doe@example.com"
        );

        Customer savedCustomer = Customer.builder()
            .id(1)
            .firstName("John&lt;script&gt;")
            .lastName("Doe&lt;img&gt;")
            .email("john.doe@example.com")
            .build();

        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudClient.isFraudster(any(Integer.class))).thenReturn(new FraudCheckResponse(false));
        doNothing().when(rabbitMQMessageProducer).publish(any(), anyString(), anyString());

        // When
        Customer result = customerService.registerCustomer(request);

        // Then
        assertEquals("John&lt;script&gt;", result.getFirstName());
        assertEquals("Doe&lt;img&gt;", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should reject duplicate email registration")
    void shouldRejectDuplicateEmail() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John",
            "Doe",
            "john.doe@example.com"
        );

        when(customerRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            customerService.registerCustomer(request);
        });

        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(customerRepository, never()).saveAndFlush(any(Customer.class));
        verify(fraudClient, never()).isFraudster(any(Integer.class));
    }

    @Test
    @DisplayName("Should detect and block fraudulent registrations")
    void shouldBlockFraudulentRegistrations() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "Fraud",
            "User",
            "fraud@example.com"
        );

        Customer savedCustomer = Customer.builder()
            .id(1)
            .firstName("Fraud")
            .lastName("User")
            .email("fraud@example.com")
            .build();

        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudClient.isFraudster(1)).thenReturn(new FraudCheckResponse(true));

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            customerService.registerCustomer(request);
        });

        assertTrue(exception.getMessage().contains("Fraudulent registration attempt"));
        verify(rabbitMQMessageProducer, never()).publish(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should send welcome notification for valid registration")
    void shouldSendWelcomeNotification() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "John",
            "Doe",
            "john.doe@example.com"
        );

        Customer savedCustomer = Customer.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.saveAndFlush(any(Customer.class))).thenReturn(savedCustomer);
        when(fraudClient.isFraudster(1)).thenReturn(new FraudCheckResponse(false));
        doNothing().when(rabbitMQMessageProducer).publish(any(), anyString(), anyString());

        // When
        customerService.registerCustomer(request);

        // Then
        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(rabbitMQMessageProducer).publish(notificationCaptor.capture(), eq("internal.exchange"), eq("internal.notification.routing-key"));

        NotificationRequest notification = notificationCaptor.getValue();
        assertEquals(1, notification.customerId());
        assertEquals("john.doe@example.com", notification.customerEmail());
        assertTrue(notification.message().contains("Hi John"));
    }
}
