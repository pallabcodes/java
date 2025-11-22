package com.example.kotlinpay.risk.domain

import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Amount threshold rule - flags high-value transactions
 */
@Component
class AmountThresholdRule : RiskRule {
    override val name = "Amount Threshold"
    override val weight = 30

    private val highAmountThreshold = BigDecimal("10000.00") // $10,000
    private val criticalAmountThreshold = BigDecimal("50000.00") // $50,000

    override fun evaluate(request: RiskEvaluationRequest): RiskEvaluation {
        val reasons = mutableListOf<String>()
        var score = 0

        when {
            request.amount >= criticalAmountThreshold -> {
                score = weight
                reasons.add("Transaction amount exceeds critical threshold: $${request.amount}")
            }
            request.amount >= highAmountThreshold -> {
                score = weight / 2
                reasons.add("Transaction amount exceeds high threshold: $${request.amount}")
            }
        }

        return RiskEvaluation(
            triggered = score > 0,
            score = score,
            reasons = reasons
        )
    }
}

/**
 * Country blacklist rule - blocks transactions from high-risk countries
 */
@Component
class CountryBlacklistRule : RiskRule {
    override val name = "Country Blacklist"
    override val weight = 50

    private val blacklistedCountries = setOf(
        "KP", // North Korea
        "IR", // Iran
        "CU", // Cuba
        "SY", // Syria
        "ZZ"  // Unknown/Invalid
    )

    override fun evaluate(request: RiskEvaluationRequest): RiskEvaluation {
        val countryCode = request.countryCode.uppercase()
        val triggered = blacklistedCountries.contains(countryCode)

        return RiskEvaluation(
            triggered = triggered,
            score = if (triggered) weight else 0,
            reasons = if (triggered) listOf("Transaction from blacklisted country: $countryCode") else emptyList()
        )
    }
}

/**
 * Velocity rule - detects rapid successive transactions
 */
@Component
class VelocityRule : RiskRule {
    override val name = "Transaction Velocity"
    override val weight = 25

    // In a real implementation, this would check a database/cache
    // For demo purposes, we'll use a simple heuristic
    private val suspiciousPatterns = setOf(
        "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999", "0000"
    )

    override fun evaluate(request: RiskEvaluationRequest): RiskEvaluation {
        val reasons = mutableListOf<String>()
        var score = 0

        // Check for suspicious card patterns (demo logic)
        if (suspiciousPatterns.contains(request.cardLastFour)) {
            score += weight / 2
            reasons.add("Suspicious card pattern detected")
        }

        // In production, this would check transaction frequency from same customer/IP
        // For demo, we'll flag if customer ID contains certain patterns
        if (request.customerId.contains("suspicious") || request.customerId.length < 3) {
            score += weight / 2
            reasons.add("Unusual transaction velocity detected")
        }

        return RiskEvaluation(
            triggered = score > 0,
            score = score,
            reasons = reasons
        )
    }
}

/**
 * IP reputation rule - checks IP address reputation
 */
@Component
class IpReputationRule : RiskRule {
    override val name = "IP Reputation"
    override val weight = 20

    private val suspiciousIpRanges = listOf(
        "10.0.0.0/8",     // Private network
        "172.16.0.0/12",  // Private network
        "192.168.0.0/16", // Private network
        "127.0.0.0/8"     // Loopback
    )

    override fun evaluate(request: RiskEvaluationRequest): RiskEvaluation {
        val reasons = mutableListOf<String>()

        // Check for private/reserved IP addresses
        if (isPrivateIp(request.ipAddress)) {
            reasons.add("Transaction from private/reserved IP address: ${request.ipAddress}")
        }

        // Check for suspicious user agents
        if (isSuspiciousUserAgent(request.userAgent)) {
            reasons.add("Suspicious user agent detected")
        }

        val score = if (reasons.isNotEmpty()) weight else 0

        return RiskEvaluation(
            triggered = score > 0,
            score = score,
            reasons = reasons
        )
    }

    private fun isPrivateIp(ipAddress: String): Boolean {
        // Simplified check - in production, use proper IP range checking
        return ipAddress.startsWith("10.") ||
               ipAddress.startsWith("172.") ||
               ipAddress.startsWith("192.168.") ||
               ipAddress.startsWith("127.")
    }

    private fun isSuspiciousUserAgent(userAgent: String): Boolean {
        val suspiciousPatterns = listOf(
            "bot", "crawler", "spider", "scraper",
            "python", "curl", "wget", "postman"
        )

        return suspiciousPatterns.any { pattern ->
            userAgent.lowercase().contains(pattern)
        }
    }
}
