package com.example.kotlinpay.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Externalized configuration properties for payment platform.
 * 
 * In production, these values should come from:
 * - Kubernetes ConfigMaps for non-sensitive configuration
 * - Kubernetes Secrets or external secret management (Vault, AWS Secrets Manager) for sensitive data
 * - Environment variables
 */
@Configuration
@ConfigurationProperties(prefix = "app")
class ApplicationProperties {
    
    data class Database(
        var url: String = "",
        var username: String = "",
        var password: String = "",
        var poolSize: Int = 20,
        var connectionTimeout: Long = 30000
    )
    
    data class Redis(
        var host: String = "localhost",
        var port: Int = 6379,
        var password: String = "",
        var ttl: Long = 3600
    )
    
    data class Security(
        var jwtSecret: String = "",
        var jwtExpiration: Long = 3600000,
        var encryptionKey: String = "",
        var tokenizationKey: String = ""
    )
    
    data class PaymentGateway(
        var stripeApiKey: String = "",
        var stripeWebhookSecret: String = "",
        var paypalClientId: String = "",
        var paypalClientSecret: String = "",
        var braintreeMerchantId: String = "",
        var braintreePublicKey: String = "",
        var braintreePrivateKey: String = "",
        var adyenApiKey: String = "",
        var adyenMerchantAccount: String = ""
    )
    
    data class Risk(
        var highThreshold: Double = 10000.0,
        var criticalThreshold: Double = 50000.0,
        var blacklistedCountries: List<String> = listOf("KP", "IR", "CU", "SY"),
        var maxTransactionsPerHour: Int = 10
    )
    
    data class Compliance(
        var auditLogRetentionDays: Int = 2555, // 7 years
        var enableGDPR: Boolean = true,
        var enableSOX: Boolean = true,
        var enablePCIDSS: Boolean = true
    )
    
    data class Monitoring(
        var enableTracing: Boolean = true,
        var tracingSampleRate: Double = 1.0,
        var metricsEnabled: Boolean = true,
        var logLevel: String = "INFO"
    )
    
    var database = Database()
    var redis = Redis()
    var security = Security()
    var paymentGateway = PaymentGateway()
    var risk = Risk()
    var compliance = Compliance()
    var monitoring = Monitoring()
}

