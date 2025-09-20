package com.netflix.springframework.demo.config;

import com.stripe.Stripe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

/**
 * Stripe Configuration - Production-Grade Payment Gateway Setup
 * 
 * This configuration class demonstrates Netflix production-grade Stripe integration:
 * 1. Environment-specific API key management
 * 2. Secure configuration with externalized secrets
 * 3. Comprehensive error handling and validation
 * 4. Production-ready security measures
 * 5. Monitoring and logging configuration
 * 
 * For C/C++ engineers:
 * - Configuration classes are like initialization modules in C++
 * - @Value is like reading from configuration files
 * - @PostConstruct is like constructor initialization
 * - @Profile is like conditional compilation in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class StripeConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(StripeConfig.class);
    private static final String CONFIG_CLASS = "StripeConfig";
    
    @Value("${stripe.api.key}")
    private String stripeSecretKey;
    
    @Value("${stripe.api.publishable-key}")
    private String stripePublishableKey;
    
    @Value("${stripe.api.webhook-secret}")
    private String stripeWebhookSecret;
    
    @Value("${stripe.payment.currency:usd}")
    private String defaultCurrency;
    
    @Value("${stripe.payment.description:Netflix Spring Framework Demo Payment}")
    private String defaultDescription;
    
    @Value("${stripe.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${stripe.security.jwt.expiration:3600000}")
    private long jwtExpiration;
    
    @Value("${stripe.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${stripe.retry.delay:1000}")
    private long retryDelay;
    
    @Value("${stripe.retry.backoff-multiplier:2.0}")
    private double backoffMultiplier;
    
    /**
     * Initialize Stripe SDK with API key
     * 
     * This method is called after dependency injection is complete
     * and ensures Stripe SDK is properly configured
     */
    @PostConstruct
    public void initializeStripe() {
        try {
            logger.info("{} - Initializing Stripe SDK", CONFIG_CLASS);
            
            // Validate configuration
            validateConfiguration();
            
            // Set Stripe API key
            Stripe.apiKey = stripeSecretKey;
            
            // Configure Stripe client
            Stripe.setAppInfo("Netflix Spring Framework Demo", "1.0.0", "https://netflix.com");
            
            logger.info("{} - Stripe SDK initialized successfully", CONFIG_CLASS);
            logger.debug("{} - Using Stripe API key: {}...", CONFIG_CLASS, 
                        stripeSecretKey.substring(0, Math.min(8, stripeSecretKey.length())));
            
        } catch (Exception e) {
            logger.error("{} - Failed to initialize Stripe SDK", CONFIG_CLASS, e);
            throw new RuntimeException("Stripe initialization failed", e);
        }
    }
    
    /**
     * Validate Stripe configuration
     * 
     * @throws IllegalArgumentException if configuration is invalid
     */
    private void validateConfiguration() {
        if (stripeSecretKey == null || stripeSecretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Stripe secret key is required");
        }
        
        if (stripePublishableKey == null || stripePublishableKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Stripe publishable key is required");
        }
        
        if (stripeWebhookSecret == null || stripeWebhookSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("Stripe webhook secret is required");
        }
        
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret is required");
        }
        
        if (jwtExpiration <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        
        if (maxRetryAttempts <= 0) {
            throw new IllegalArgumentException("Max retry attempts must be positive");
        }
        
        if (retryDelay < 0) {
            throw new IllegalArgumentException("Retry delay must be non-negative");
        }
        
        if (backoffMultiplier <= 0) {
            throw new IllegalArgumentException("Backoff multiplier must be positive");
        }
        
        logger.debug("{} - Configuration validation completed successfully", CONFIG_CLASS);
    }
    
    /**
     * Get Stripe secret key
     * 
     * @return Stripe secret key
     */
    public String getStripeSecretKey() {
        return stripeSecretKey;
    }
    
    /**
     * Get Stripe publishable key
     * 
     * @return Stripe publishable key
     */
    public String getStripePublishableKey() {
        return stripePublishableKey;
    }
    
    /**
     * Get Stripe webhook secret
     * 
     * @return Stripe webhook secret
     */
    public String getStripeWebhookSecret() {
        return stripeWebhookSecret;
    }
    
    /**
     * Get default currency
     * 
     * @return Default currency
     */
    public String getDefaultCurrency() {
        return defaultCurrency;
    }
    
    /**
     * Get default description
     * 
     * @return Default description
     */
    public String getDefaultDescription() {
        return defaultDescription;
    }
    
    /**
     * Get JWT secret
     * 
     * @return JWT secret
     */
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    /**
     * Get JWT expiration time
     * 
     * @return JWT expiration time in milliseconds
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }
    
    /**
     * Get maximum retry attempts
     * 
     * @return Maximum retry attempts
     */
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    /**
     * Get retry delay
     * 
     * @return Retry delay in milliseconds
     */
    public long getRetryDelay() {
        return retryDelay;
    }
    
    /**
     * Get backoff multiplier
     * 
     * @return Backoff multiplier
     */
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }
    
    /**
     * Check if running in test mode
     * 
     * @return true if test mode
     */
    public boolean isTestMode() {
        return stripeSecretKey.startsWith("sk_test_");
    }
    
    /**
     * Check if running in live mode
     * 
     * @return true if live mode
     */
    public boolean isLiveMode() {
        return stripeSecretKey.startsWith("sk_live_");
    }
    
    /**
     * Get Stripe configuration summary
     * 
     * @return Configuration summary
     */
    public String getConfigurationSummary() {
        return String.format(
            "StripeConfig{currency='%s', description='%s', testMode=%s, maxRetries=%d, retryDelay=%dms}",
            defaultCurrency, defaultDescription, isTestMode(), maxRetryAttempts, retryDelay
        );
    }
}
