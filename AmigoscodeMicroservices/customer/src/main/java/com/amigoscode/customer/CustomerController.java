package com.amigoscode.customer;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/customers")
@AllArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Register a new customer with security validation
     */
    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponse> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request) {

        log.info("Processing customer registration for email: {}", request.email());

        try {
            Customer customer = customerService.registerCustomer(request);

            CustomerRegistrationResponse response = new CustomerRegistrationResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                "Customer registered successfully"
            );

            log.info("Customer registration successful for ID: {}", customer.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Customer registration failed for email: {}", request.email(), e);

            // Security: Don't leak internal error details
            ErrorResponse errorResponse = new ErrorResponse(
                "REGISTRATION_FAILED",
                "Customer registration failed. Please try again."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get customer by ID (authenticated endpoint)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Integer id) {
        log.info("Fetching customer with ID: {}", id);

        try {
            Customer customer = customerService.getCustomerById(id);

            CustomerResponse response = new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to fetch customer with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check endpoint (public)
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "Customer service is healthy"));
    }
}

// Response DTOs
record CustomerRegistrationResponse(
    Integer id,
    String firstName,
    String lastName,
    String email,
    String message
) {}

record CustomerResponse(
    Integer id,
    String firstName,
    String lastName,
    String email
) {}

record ErrorResponse(
    String errorCode,
    String message
) {}

record HealthResponse(
    String status,
    String message
) {}
