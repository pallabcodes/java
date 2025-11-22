package com.amigoscode.customer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    /**
     * Check if customer exists by email
     * @param email the email to check
     * @return true if customer exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find customer by email
     * @param email the email to search for
     * @return Optional<Customer> if found
     */
    java.util.Optional<Customer> findByEmail(String email);
}
