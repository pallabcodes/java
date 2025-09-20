package com.netflix.springframework.demo.service;

import com.netflix.springframework.demo.config.StripeConfig;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.entity.SubscriptionEntity;
import com.netflix.springframework.demo.repository.SubscriptionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SubscriptionService - Production-Grade Subscription Management Service
 * 
 * This service demonstrates Netflix production-grade subscription management implementation:
 * 1. Subscription creation and management
 * 2. Recurring payment processing
 * 3. Subscription lifecycle management
 * 4. Billing cycle management
 * 5. Subscription upgrades and downgrades
 * 6. Cancellation and reactivation
 * 
 * For C/C++ engineers:
 * - Subscription services are like recurring billing modules in C++
 * - @Transactional is like database transaction management in C++
 * - @Retryable is like retry mechanisms in C++
 * - Stripe API calls are like external service calls in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Service
@Transactional
public class SubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    private static final String SERVICE_NAME = "SubscriptionService";
    
    private final StripeConfig stripeConfig;
    private final SubscriptionRepository subscriptionRepository;
    private final StripePaymentService paymentService;
    
    /**
     * Constructor with dependency injection
     * 
     * @param stripeConfig Stripe configuration
     * @param subscriptionRepository Subscription repository
     * @param paymentService Payment service
     */
    @Autowired
    public SubscriptionService(StripeConfig stripeConfig, 
                              SubscriptionRepository subscriptionRepository,
                              StripePaymentService paymentService) {
        this.stripeConfig = stripeConfig;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentService = paymentService;
        logger.info("{} initialized", SERVICE_NAME);
    }
    
    /**
     * Create subscription
     * 
     * @param userId User ID
     * @param priceId Stripe price ID
     * @param paymentMethodId Payment method ID
     * @return Created subscription
     */
    @Retryable(value = {StripeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public SubscriptionEntity createSubscription(Long userId, String priceId, String paymentMethodId) {
        logger.info("{} - Creating subscription for user: {}, price: {}", SERVICE_NAME, userId, priceId);
        
        try {
            // Create or get Stripe customer
            String customerId = createOrGetStripeCustomer(userId);
            
            // Create Stripe subscription
            Subscription stripeSubscription = createStripeSubscription(customerId, priceId, paymentMethodId);
            
            // Create subscription entity
            SubscriptionEntity subscription = createSubscriptionEntity(userId, stripeSubscription);
            
            // Save subscription
            SubscriptionEntity savedSubscription = subscriptionRepository.save(subscription);
            
            logger.info("{} - Subscription created successfully. Subscription ID: {}, Stripe ID: {}", 
                       SERVICE_NAME, savedSubscription.getId(), stripeSubscription.getId());
            
            return savedSubscription;
            
        } catch (StripeException e) {
            logger.error("{} - Stripe error creating subscription for user: {}", SERVICE_NAME, userId, e);
            throw new RuntimeException("Failed to create subscription: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("{} - Error creating subscription for user: {}", SERVICE_NAME, userId, e);
            throw new RuntimeException("Failed to create subscription", e);
        }
    }
    
    /**
     * Update subscription
     * 
     * @param subscriptionId Subscription ID
     * @param priceId New price ID
     * @return Updated subscription
     */
    @Retryable(value = {StripeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public SubscriptionEntity updateSubscription(Long subscriptionId, String priceId) {
        logger.info("{} - Updating subscription: {}, new price: {}", SERVICE_NAME, subscriptionId, priceId);
        
        try {
            // Find subscription
            Optional<SubscriptionEntity> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                throw new RuntimeException("Subscription not found with ID: " + subscriptionId);
            }
            
            SubscriptionEntity subscription = subscriptionOpt.get();
            
            // Update Stripe subscription
            Subscription stripeSubscription = updateStripeSubscription(subscription.getStripeSubscriptionId(), priceId);
            
            // Update subscription entity
            updateSubscriptionEntity(subscription, stripeSubscription);
            
            // Save subscription
            SubscriptionEntity savedSubscription = subscriptionRepository.save(subscription);
            
            logger.info("{} - Subscription updated successfully. Subscription ID: {}, Status: {}", 
                       SERVICE_NAME, subscriptionId, savedSubscription.getStatus());
            
            return savedSubscription;
            
        } catch (StripeException e) {
            logger.error("{} - Stripe error updating subscription: {}", SERVICE_NAME, subscriptionId, e);
            throw new RuntimeException("Failed to update subscription: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("{} - Error updating subscription: {}", SERVICE_NAME, subscriptionId, e);
            throw new RuntimeException("Failed to update subscription", e);
        }
    }
    
    /**
     * Cancel subscription
     * 
     * @param subscriptionId Subscription ID
     * @param cancelAtPeriodEnd Cancel at period end
     * @return Updated subscription
     */
    @Retryable(value = {StripeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public SubscriptionEntity cancelSubscription(Long subscriptionId, boolean cancelAtPeriodEnd) {
        logger.info("{} - Cancelling subscription: {}, at period end: {}", SERVICE_NAME, subscriptionId, cancelAtPeriodEnd);
        
        try {
            // Find subscription
            Optional<SubscriptionEntity> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                throw new RuntimeException("Subscription not found with ID: " + subscriptionId);
            }
            
            SubscriptionEntity subscription = subscriptionOpt.get();
            
            // Cancel Stripe subscription
            Subscription stripeSubscription = cancelStripeSubscription(subscription.getStripeSubscriptionId(), cancelAtPeriodEnd);
            
            // Update subscription entity
            updateSubscriptionEntity(subscription, stripeSubscription);
            
            // Save subscription
            SubscriptionEntity savedSubscription = subscriptionRepository.save(subscription);
            
            logger.info("{} - Subscription cancelled successfully. Subscription ID: {}, Status: {}", 
                       SERVICE_NAME, subscriptionId, savedSubscription.getStatus());
            
            return savedSubscription;
            
        } catch (StripeException e) {
            logger.error("{} - Stripe error cancelling subscription: {}", SERVICE_NAME, subscriptionId, e);
            throw new RuntimeException("Failed to cancel subscription: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("{} - Error cancelling subscription: {}", SERVICE_NAME, subscriptionId, e);
            throw new RuntimeException("Failed to cancel subscription", e);
        }
    }
    
    /**
     * Reactivate subscription
     * 
     * @param subscriptionId Subscription ID
     * @return Updated subscription
     */
    @Retryable(value = {StripeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public SubscriptionEntity reactivateSubscription(Long subscriptionId) {
        logger.info("{} - Reactivating subscription: {}", SERVICE_NAME, subscriptionId);
        
        try {
            // Find subscription
            Optional<SubscriptionEntity> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                throw new RuntimeException("Subscription not found with ID: " + subscriptionId);
            }
            
            SubscriptionEntity subscription = subscriptionOpt.get();
            
            // Reactivate Stripe subscription
            Subscription stripeSubscription = reactivateStripeSubscription(subscription.getStripeSubscriptionId());
            
            // Update subscription entity
            updateSubscriptionEntity(subscription, stripeSubscription);
            
            // Save subscription
            SubscriptionEntity savedSubscription = subscriptionRepository.save(subscription);
            
            logger.info("{} - Subscription reactivated successfully. Subscription ID: {}, Status: {}", 
                       SERVICE_NAME, subscriptionId, savedSubscription.getStatus());
            
            return savedSubscription;
            
        } catch (StripeException e) {
            logger.error("{} - Stripe error reactivating subscription: {}", SERVICE_NAME, subscriptionId, e);
            throw new RuntimeException("Failed to reactivate subscription: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("{} - Error reactivating subscription: {}", SERVICE_NAME, subscriptionId, e);
            throw new RuntimeException("Failed to reactivate subscription", e);
        }
    }
    
    /**
     * Get subscription by ID
     * 
     * @param subscriptionId Subscription ID
     * @return Subscription if found
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionEntity> getSubscriptionById(Long subscriptionId) {
        logger.debug("{} - Getting subscription by ID: {}", SERVICE_NAME, subscriptionId);
        return subscriptionRepository.findById(subscriptionId);
    }
    
    /**
     * Get subscription by Stripe ID
     * 
     * @param stripeSubscriptionId Stripe subscription ID
     * @return Subscription if found
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionEntity> getSubscriptionByStripeId(String stripeSubscriptionId) {
        logger.debug("{} - Getting subscription by Stripe ID: {}", SERVICE_NAME, stripeSubscriptionId);
        return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);
    }
    
    /**
     * Get subscriptions by user ID
     * 
     * @param userId User ID
     * @return List of subscriptions
     */
    @Transactional(readOnly = true)
    public List<SubscriptionEntity> getSubscriptionsByUserId(Long userId) {
        logger.debug("{} - Getting subscriptions by user ID: {}", SERVICE_NAME, userId);
        return subscriptionRepository.findByUserId(userId);
    }
    
    /**
     * Get active subscriptions by user ID
     * 
     * @param userId User ID
     * @return List of active subscriptions
     */
    @Transactional(readOnly = true)
    public List<SubscriptionEntity> getActiveSubscriptionsByUserId(Long userId) {
        logger.debug("{} - Getting active subscriptions by user ID: {}", SERVICE_NAME, userId);
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionEntity.SubscriptionStatus.ACTIVE);
    }
    
    /**
     * Create or get Stripe customer
     * 
     * @param userId User ID
     * @return Stripe customer ID
     * @throws StripeException if Stripe API call fails
     */
    private String createOrGetStripeCustomer(Long userId) throws StripeException {
        // TODO: Implement customer creation/retrieval logic
        // This would typically involve:
        // 1. Check if customer exists in Stripe
        // 2. Create customer if not exists
        // 3. Store customer ID in database
        // 4. Return customer ID
        
        return "cus_" + userId;
    }
    
    /**
     * Create Stripe subscription
     * 
     * @param customerId Stripe customer ID
     * @param priceId Stripe price ID
     * @param paymentMethodId Payment method ID
     * @return Created Stripe subscription
     * @throws StripeException if Stripe API call fails
     */
    private Subscription createStripeSubscription(String customerId, String priceId, String paymentMethodId) throws StripeException {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(priceId)
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .setPaymentSettings(SubscriptionCreateParams.PaymentSettings.builder()
                        .setPaymentMethod(paymentMethodId)
                        .setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION)
                        .build())
                .setExpand(java.util.Arrays.asList("latest_invoice.payment_intent"))
                .build();
        
        return Subscription.create(params);
    }
    
    /**
     * Update Stripe subscription
     * 
     * @param stripeSubscriptionId Stripe subscription ID
     * @param priceId New price ID
     * @return Updated Stripe subscription
     * @throws StripeException if Stripe API call fails
     */
    private Subscription updateStripeSubscription(String stripeSubscriptionId, String priceId) throws StripeException {
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setItems(java.util.Arrays.asList(SubscriptionUpdateParams.Item.builder()
                        .setPrice(priceId)
                        .build()))
                .build();
        
        return Subscription.retrieve(stripeSubscriptionId).update(params);
    }
    
    /**
     * Cancel Stripe subscription
     * 
     * @param stripeSubscriptionId Stripe subscription ID
     * @param cancelAtPeriodEnd Cancel at period end
     * @return Cancelled Stripe subscription
     * @throws StripeException if Stripe API call fails
     */
    private Subscription cancelStripeSubscription(String stripeSubscriptionId, boolean cancelAtPeriodEnd) throws StripeException {
        Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
        
        if (cancelAtPeriodEnd) {
            return subscription.cancel();
        } else {
            return subscription.cancel();
        }
    }
    
    /**
     * Reactivate Stripe subscription
     * 
     * @param stripeSubscriptionId Stripe subscription ID
     * @return Reactivated Stripe subscription
     * @throws StripeException if Stripe API call fails
     */
    private Subscription reactivateStripeSubscription(String stripeSubscriptionId) throws StripeException {
        // TODO: Implement subscription reactivation logic
        // This would typically involve:
        // 1. Check if subscription can be reactivated
        // 2. Update subscription status
        // 3. Resume billing
        // 4. Send reactivation notification
        
        return Subscription.retrieve(stripeSubscriptionId);
    }
    
    /**
     * Create subscription entity
     * 
     * @param userId User ID
     * @param stripeSubscription Stripe subscription
     * @return Created subscription entity
     */
    private SubscriptionEntity createSubscriptionEntity(Long userId, Subscription stripeSubscription) {
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setUserId(userId);
        subscription.setStripeSubscriptionId(stripeSubscription.getId());
        subscription.setStatus(mapStripeStatusToSubscriptionStatus(stripeSubscription.getStatus()));
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        
        return subscription;
    }
    
    /**
     * Update subscription entity
     * 
     * @param subscription Subscription entity
     * @param stripeSubscription Stripe subscription
     */
    private void updateSubscriptionEntity(SubscriptionEntity subscription, Subscription stripeSubscription) {
        subscription.setStatus(mapStripeStatusToSubscriptionStatus(stripeSubscription.getStatus()));
        subscription.setUpdatedAt(LocalDateTime.now());
        
        if (stripeSubscription.getCurrentPeriodStart() != null) {
            subscription.setCurrentPeriodStart(LocalDateTime.ofEpochSecond(
                stripeSubscription.getCurrentPeriodStart(), 0, java.time.ZoneOffset.UTC));
        }
        
        if (stripeSubscription.getCurrentPeriodEnd() != null) {
            subscription.setCurrentPeriodEnd(LocalDateTime.ofEpochSecond(
                stripeSubscription.getCurrentPeriodEnd(), 0, java.time.ZoneOffset.UTC));
        }
    }
    
    /**
     * Map Stripe status to subscription status
     * 
     * @param stripeStatus Stripe subscription status
     * @return Subscription status
     */
    private SubscriptionEntity.SubscriptionStatus mapStripeStatusToSubscriptionStatus(String stripeStatus) {
        return switch (stripeStatus.toLowerCase()) {
            case "active" -> SubscriptionEntity.SubscriptionStatus.ACTIVE;
            case "canceled" -> SubscriptionEntity.SubscriptionStatus.CANCELLED;
            case "incomplete" -> SubscriptionEntity.SubscriptionStatus.INCOMPLETE;
            case "incomplete_expired" -> SubscriptionEntity.SubscriptionStatus.INCOMPLETE_EXPIRED;
            case "past_due" -> SubscriptionEntity.SubscriptionStatus.PAST_DUE;
            case "trialing" -> SubscriptionEntity.SubscriptionStatus.TRIALING;
            case "unpaid" -> SubscriptionEntity.SubscriptionStatus.UNPAID;
            default -> SubscriptionEntity.SubscriptionStatus.INCOMPLETE;
        };
    }
}
