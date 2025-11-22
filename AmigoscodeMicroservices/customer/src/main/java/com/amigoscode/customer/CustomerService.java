package com.amigoscode.customer;

import com.amigoscode.amqp.RabbitMQMessageProducer;
import com.amigoscode.clients.fraud.FraudCheckResponse;
import com.amigoscode.clients.fraud.FraudClient;
import com.amigoscode.clients.notification.NotificationClient;
import com.amigoscode.clients.notification.NotificationRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;
    private final MeterRegistry meterRegistry;

    // Monitoring: Performance timers
    private final Timer registrationTimer = Timer.builder("customer.registration.duration")
        .description("Time taken to register a customer")
        .tags("service", "customer", "operation", "registration")
        .register(meterRegistry);

    // Monitoring: Business metrics counters
    private final Counter registrationSuccessCounter = Counter.builder("customer.registration.success")
        .description("Successful customer registrations")
        .tags("service", "customer", "result", "success")
        .register(meterRegistry);

    private final Counter registrationFailureCounter = Counter.builder("customer.registration.failure")
        .description("Failed customer registrations")
        .tags("service", "customer", "result", "failure")
        .register(meterRegistry);

    private final Counter fraudDetectionCounter = Counter.builder("customer.fraud.detected")
        .description("Fraudulent registration attempts detected")
        .tags("service", "customer", "type", "fraud")
        .register(meterRegistry);

    private final Counter duplicateEmailCounter = Counter.builder("customer.duplicate.email")
        .description("Duplicate email registration attempts")
        .tags("service", "customer", "type", "duplicate")
        .register(meterRegistry);

    public Customer registerCustomer(CustomerRegistrationRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString();

        log.info("Processing customer registration [requestId={}, email={}]", requestId, request.email());

        try {
            return registrationTimer.recordCallable(() -> {
                // Security: Check if email is already taken
                if (customerRepository.existsByEmail(request.email())) {
                    duplicateEmailCounter.increment();
                    log.warn("Duplicate email registration attempt [requestId={}, email={}]", requestId, request.email());
                    registrationFailureCounter.increment();
                    throw new IllegalStateException("Email already exists: " + request.email());
                }

                Customer customer = Customer.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .email(request.email())
                        .build();

                customer = customerRepository.saveAndFlush(customer);
                log.debug("Customer saved to database [requestId={}, customerId={}]", requestId, customer.getId());

                // Security: Fraud check with monitoring
                FraudCheckResponse fraudCheckResponse = fraudClient.isFraudster(customer.getId());

                if (fraudCheckResponse.isFraudster()) {
                    fraudDetectionCounter.increment();
                    log.warn("Fraudulent registration attempt blocked [requestId={}, customerId={}]", requestId, customer.getId());
                    registrationFailureCounter.increment();
                    throw new SecurityException("Fraudulent registration attempt detected for: " + request.email());
                }

                // Send welcome notification
                NotificationRequest notificationRequest = new NotificationRequest(
                        customer.getId(),
                        customer.getEmail(),
                        String.format("Hi %s, welcome to Amigoscode...",
                                customer.getFirstName())
                );

                try {
                    rabbitMQMessageProducer.publish(
                            notificationRequest,
                            "internal.exchange",
                            "internal.notification.routing-key"
                    );
                    log.debug("Welcome notification sent [requestId={}, customerId={}]", requestId, customer.getId());
                } catch (Exception e) {
                    log.error("Failed to send welcome notification [requestId={}, customerId={}]", requestId, customer.getId(), e);
                    // Don't fail registration for notification issues
                }

                registrationSuccessCounter.increment();
                long duration = System.currentTimeMillis() - startTime;
                log.info("Customer registration completed successfully [requestId={}, customerId={}, duration={}ms]",
                        requestId, customer.getId(), duration);

                // Record business metrics
                meterRegistry.gauge("customer.total.count", customerRepository.count());

                return customer;
            });

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Customer registration failed [requestId={}, duration={}ms]", requestId, duration, e);

            // Re-throw the exception after logging
            throw e;
        }
    }

    public Customer getCustomerById(Integer id) {
        String requestId = java.util.UUID.randomUUID().toString();
        log.info("Retrieving customer by ID [requestId={}, customerId={}]", requestId, id);

        try {
            Timer.Sample sample = Timer.start(meterRegistry);
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Customer not found [requestId={}, customerId={}]", requestId, id);
                        Counter.builder("customer.not.found")
                            .description("Customer lookup failures")
                            .tags("service", "customer", "reason", "not_found")
                            .register(meterRegistry)
                            .increment();
                        return new IllegalArgumentException("Customer not found with ID: " + id);
                    });

            sample.stop(Timer.builder("customer.lookup.duration")
                .description("Time taken to lookup customer")
                .tags("service", "customer", "operation", "lookup")
                .register(meterRegistry));

            log.info("Customer retrieved successfully [requestId={}, customerId={}]", requestId, id);
            return customer;

        } catch (IllegalArgumentException e) {
            // Re-throw not found exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving customer [requestId={}, customerId={}]", requestId, id, e);
            Counter.builder("customer.lookup.error")
                .description("Customer lookup errors")
                .tags("service", "customer", "error_type", "database")
                .register(meterRegistry)
                .increment();
            throw e;
        }
    }
}
