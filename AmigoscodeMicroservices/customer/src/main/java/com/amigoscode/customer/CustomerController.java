package com.amigoscode.customer;

import com.amigoscode.customer.security.RequiresPermission;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
    @RequiresPermission(value = {"customer.create", "ADMIN"}, resource = "#request.email")
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
     * Get customer by ID (authenticated endpoint with ABAC)
     */
    @GetMapping("/{id}")
    @RequiresPermission(value = {"customer.read", "ADMIN"},
                       resource = "#id",
                       action = "READ",
                       checkTenantOwnership = true)
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Integer id, Authentication authentication) {
        log.info("Fetching customer with ID: {} for user: {}", id, authentication.getName());

        try {
            Customer customer = customerService.getCustomerById(id);

            // ABAC: Check if user owns this customer record or has ADMIN role
            String userEmail = getUserEmailFromAuthentication(authentication);
            boolean isOwner = customer.getEmail().equals(userEmail);
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isOwner && !isAdmin) {
                log.warn("Access denied: User {} attempted to access customer record {} belonging to {}",
                        userEmail, id, customer.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            CustomerResponse response = new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail()
            );

            log.info("Successfully retrieved customer {} for user {}", id, userEmail);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to fetch customer with ID: {} for user: {}", id, authentication.getName(), e);
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

    /**
     * Helper method to extract user email from OAuth2 authentication
     */
    private String getUserEmailFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }
        return authentication.getName();
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
