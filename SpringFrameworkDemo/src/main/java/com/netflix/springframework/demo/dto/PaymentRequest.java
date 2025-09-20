package com.netflix.springframework.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * PaymentRequest - DTO for Payment Creation
 * 
 * This DTO demonstrates Netflix production-grade payment request implementation:
 * 1. Comprehensive validation for payment data
 * 2. JSON property mapping with @JsonProperty
 * 3. Bean Validation annotations for input validation
 * 4. Security considerations for sensitive data
 * 5. Business logic validation for payment amounts
 * 
 * For C/C++ engineers:
 * - DTOs are like data structures in C++
 * - Validation annotations are like input checks in C++
 * - JSON mapping is like serialization in C++
 * - BigDecimal is like decimal arithmetic in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
public class PaymentRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Amount must not exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Amount must have at most 6 integer digits and 2 decimal places")
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase 3-letter code")
    @JsonProperty("currency")
    private String currency;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @JsonProperty("description")
    private String description;
    
    @Size(max = 1000, message = "Metadata must not exceed 1000 characters")
    @JsonProperty("metadata")
    private String metadata;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    @JsonProperty("customer_email")
    private String customerEmail;
    
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    @JsonProperty("customer_name")
    private String customerName;
    
    @NotBlank(message = "Payment method ID is required")
    @Size(max = 255, message = "Payment method ID must not exceed 255 characters")
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    @JsonProperty("customer_id")
    private String customerId;
    
    @JsonProperty("save_payment_method")
    private Boolean savePaymentMethod = false;
    
    @JsonProperty("confirmation_method")
    private String confirmationMethod = "automatic";
    
    @JsonProperty("capture_method")
    private String captureMethod = "automatic";
    
    /**
     * Default constructor
     * 
     * Required for JSON deserialization
     */
    public PaymentRequest() {
    }
    
    /**
     * Constructor with required fields
     * 
     * @param amount Payment amount
     * @param currency Payment currency
     * @param customerEmail Customer email
     * @param paymentMethodId Payment method ID
     */
    public PaymentRequest(BigDecimal amount, String currency, String customerEmail, String paymentMethodId) {
        this.amount = amount;
        this.currency = currency;
        this.customerEmail = customerEmail;
        this.paymentMethodId = paymentMethodId;
    }
    
    // Getters and Setters
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getPaymentMethodId() {
        return paymentMethodId;
    }
    
    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public Boolean getSavePaymentMethod() {
        return savePaymentMethod;
    }
    
    public void setSavePaymentMethod(Boolean savePaymentMethod) {
        this.savePaymentMethod = savePaymentMethod;
    }
    
    public String getConfirmationMethod() {
        return confirmationMethod;
    }
    
    public void setConfirmationMethod(String confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
    }
    
    public String getCaptureMethod() {
        return captureMethod;
    }
    
    public void setCaptureMethod(String captureMethod) {
        this.captureMethod = captureMethod;
    }
    
    /**
     * Get formatted amount
     * 
     * @return formatted amount string
     */
    public String getFormattedAmount() {
        return String.format("%s %.2f", this.currency, this.amount);
    }
    
    /**
     * Check if payment method should be saved
     * 
     * @return true if payment method should be saved
     */
    public boolean shouldSavePaymentMethod() {
        return Boolean.TRUE.equals(this.savePaymentMethod);
    }
    
    /**
     * Check if confirmation method is manual
     * 
     * @return true if confirmation method is manual
     */
    public boolean isManualConfirmation() {
        return "manual".equalsIgnoreCase(this.confirmationMethod);
    }
    
    /**
     * Check if capture method is manual
     * 
     * @return true if capture method is manual
     */
    public boolean isManualCapture() {
        return "manual".equalsIgnoreCase(this.captureMethod);
    }
    
    /**
     * equals method for DTO comparison
     * 
     * @param obj Object to compare
     * @return true if objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PaymentRequest that = (PaymentRequest) obj;
        return java.util.Objects.equals(amount, that.amount) &&
               java.util.Objects.equals(currency, that.currency) &&
               java.util.Objects.equals(customerEmail, that.customerEmail) &&
               java.util.Objects.equals(paymentMethodId, that.paymentMethodId);
    }
    
    /**
     * hashCode method for DTO hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(amount, currency, customerEmail, paymentMethodId);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                ", description='" + description + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", paymentMethodId='" + paymentMethodId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", savePaymentMethod=" + savePaymentMethod +
                ", confirmationMethod='" + confirmationMethod + '\'' +
                ", captureMethod='" + captureMethod + '\'' +
                '}';
    }
}
