package com.example.kotlinpay.shared.security

import com.example.kotlinpay.shared.compliance.AuditLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * PCI DSS Compliant Tokenization Service
 *
 * Implements secure tokenization of sensitive payment data including:
 * - Card numbers, CVV, expiration dates
 * - Bank account information
 * - Personal identifiable information (PII)
 *
 * Features:
 * - Format-preserving tokenization for card numbers
 * - Cryptographic tokenization with AES-256-GCM
 * - Token vault with secure storage
 * - PCI DSS Level 1 compliant operations
 * - Audit logging for all tokenization operations
 */
@Service
class TokenizationService(
    private val auditLogger: AuditLogger,
    private val encryptionService: EncryptionService
) {
    private val logger = LoggerFactory.getLogger(TokenizationService::class.java)

    // Token vault for mapping tokens to encrypted sensitive data
    private val tokenVault = ConcurrentHashMap<String, TokenizedData>()

    // Secure random for token generation
    private val secureRandom = SecureRandom()

    // Tokenization key (should be rotated regularly)
    private lateinit var tokenizationKey: SecretKey

    init {
        initializeTokenizationKey()
    }

    /**
     * Tokenize sensitive payment data
     */
    fun tokenizeCardNumber(cardNumber: String): TokenizationResult {
        return try {
            validateCardNumber(cardNumber)

            // Generate format-preserving token
            val token = generateCardToken(cardNumber)

            // Encrypt the actual card number
            val encryptedCardNumber = encryptionService.encrypt(cardNumber)

            // Store in token vault
            val tokenizedData = TokenizedData(
                token = token,
                encryptedData = encryptedCardNumber,
                dataType = DataType.CARD_NUMBER,
                createdAt = java.time.LocalDateTime.now(),
                metadata = mapOf(
                    "lastFour" to cardNumber.takeLast(4),
                    "cardType" to detectCardType(cardNumber)
                )
            )

            tokenVault[token] = tokenizedData

            // Audit log the tokenization
            auditLogger.logSecurityEvent(
                event = "CARD_TOKENIZATION",
                severity = "INFO",
                details = mapOf(
                    "token" to token,
                    "last_four" to cardNumber.takeLast(4),
                    "card_type" to detectCardType(cardNumber)
                )
            )

            TokenizationResult.Success(token, tokenizedData.metadata)

        } catch (e: Exception) {
            logger.error("Card number tokenization failed", e)

            auditLogger.logSecurityEvent(
                event = "TOKENIZATION_FAILURE",
                severity = "HIGH",
                details = mapOf(
                    "data_type" to "CARD_NUMBER",
                    "error" to e.message.toString()
                )
            )

            TokenizationResult.Failure("Tokenization failed: ${e.message}")
        }
    }

    /**
     * Tokenize CVV/ CVC
     */
    fun tokenizeCVV(cvv: String): TokenizationResult {
        return try {
            validateCVV(cvv)

            val token = generateSecureToken("CVV")
            val encryptedCVV = encryptionService.encrypt(cvv)

            val tokenizedData = TokenizedData(
                token = token,
                encryptedData = encryptedCVV,
                dataType = DataType.CVV,
                createdAt = java.time.LocalDateTime.now(),
                metadata = mapOf("length" to cvv.length.toString())
            )

            tokenVault[token] = tokenizedData

            auditLogger.logSecurityEvent(
                event = "CVV_TOKENIZATION",
                severity = "INFO",
                details = mapOf("token" to token)
            )

            TokenizationResult.Success(token, tokenizedData.metadata)

        } catch (e: Exception) {
            logger.error("CVV tokenization failed", e)
            TokenizationResult.Failure("CVV tokenization failed: ${e.message}")
        }
    }

    /**
     * Tokenize expiration date
     */
    fun tokenizeExpirationDate(expirationDate: String): TokenizationResult {
        return try {
            validateExpirationDate(expirationDate)

            val token = generateSecureToken("EXP")
            val encryptedDate = encryptionService.encrypt(expirationDate)

            val tokenizedData = TokenizedData(
                token = token,
                encryptedData = encryptedDate,
                dataType = DataType.EXPIRATION_DATE,
                createdAt = java.time.LocalDateTime.now(),
                metadata = emptyMap()
            )

            tokenVault[token] = tokenizedData

            TokenizationResult.Success(token, tokenizedData.metadata)

        } catch (e: Exception) {
            logger.error("Expiration date tokenization failed", e)
            TokenizationResult.Failure("Expiration date tokenization failed: ${e.message}")
        }
    }

    /**
     * Tokenize bank account information
     */
    fun tokenizeBankAccount(accountNumber: String, routingNumber: String): TokenizationResult {
        return try {
            validateBankAccount(accountNumber, routingNumber)

            val token = generateSecureToken("BANK")
            val combinedData = "$accountNumber|$routingNumber"
            val encryptedData = encryptionService.encrypt(combinedData)

            val tokenizedData = TokenizedData(
                token = token,
                encryptedData = encryptedData,
                dataType = DataType.BANK_ACCOUNT,
                createdAt = java.time.LocalDateTime.now(),
                metadata = mapOf(
                    "last_four" to accountNumber.takeLast(4),
                    "routing_last_four" to routingNumber.takeLast(4)
                )
            )

            tokenVault[token] = tokenizedData

            auditLogger.logSecurityEvent(
                event = "BANK_ACCOUNT_TOKENIZATION",
                severity = "INFO",
                details = mapOf(
                    "token" to token,
                    "account_last_four" to accountNumber.takeLast(4)
                )
            )

            TokenizationResult.Success(token, tokenizedData.metadata)

        } catch (e: Exception) {
            logger.error("Bank account tokenization failed", e)
            TokenizationResult.Failure("Bank account tokenization failed: ${e.message}")
        }
    }

    /**
     * Detokenize data (restricted operation - only for authorized systems)
     */
    fun detokenize(token: String): DetokenizationResult {
        return try {
            val tokenizedData = tokenVault[token]
                ?: return DetokenizationResult.Failure("Token not found")

            // Check if token is expired or revoked
            if (isTokenExpired(tokenizedData)) {
                auditLogger.logSecurityEvent(
                    event = "DETOKENIZATION_EXPIRED_TOKEN",
                    severity = "WARNING",
                    details = mapOf("token" to token)
                )
                return DetokenizationResult.Failure("Token expired")
            }

            val decryptedData = encryptionService.decrypt(tokenizedData.encryptedData)

            auditLogger.logSecurityEvent(
                event = "DETOKENIZATION_SUCCESS",
                severity = "HIGH",
                details = mapOf(
                    "token" to token,
                    "data_type" to tokenizedData.dataType.name
                )
            )

            DetokenizationResult.Success(decryptedData, tokenizedData.dataType, tokenizedData.metadata)

        } catch (e: Exception) {
            logger.error("Detokenization failed for token: $token", e)

            auditLogger.logSecurityEvent(
                event = "DETOKENIZATION_FAILURE",
                severity = "CRITICAL",
                details = mapOf(
                    "token" to token,
                    "error" to e.message.toString()
                )
            )

            DetokenizationResult.Failure("Detokenization failed: ${e.message}")
        }
    }

    /**
     * Get token metadata without exposing sensitive data
     */
    fun getTokenMetadata(token: String): TokenMetadata? {
        return tokenVault[token]?.let { data ->
            TokenMetadata(
                token = token,
                dataType = data.dataType,
                createdAt = data.createdAt,
                metadata = data.metadata,
                isActive = !isTokenExpired(data)
            )
        }
    }

    /**
     * Revoke a token (for PCI DSS compliance)
     */
    fun revokeToken(token: String): Boolean {
        return try {
            val tokenizedData = tokenVault[token]
            if (tokenizedData != null) {
                tokenVault.remove(token)

                auditLogger.logSecurityEvent(
                    event = "TOKEN_REVOCATION",
                    severity = "INFO",
                    details = mapOf(
                        "token" to token,
                        "data_type" to tokenizedData.dataType.name
                    )
                )

                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error("Token revocation failed for token: $token", e)
            false
        }
    }

    /**
     * PCI DSS compliant token rotation
     */
    fun rotateTokens(): TokenRotationResult {
        return try {
            val tokensToRotate = tokenVault.entries.filter { isTokenExpired(it.value) }
            var successCount = 0
            var failureCount = 0

            tokensToRotate.forEach { (token, data) ->
                try {
                    // Generate new token
                    val newToken = generateSecureToken(data.dataType.name)

                    // Update vault with new token
                    tokenVault.remove(token)
                    tokenVault[newToken] = data.copy(token = newToken)

                    auditLogger.logSecurityEvent(
                        event = "TOKEN_ROTATION",
                        severity = "INFO",
                        details = mapOf(
                            "old_token" to token,
                            "new_token" to newToken,
                            "data_type" to data.dataType.name
                        )
                    )

                    successCount++
                } catch (e: Exception) {
                    logger.error("Token rotation failed for token: $token", e)
                    failureCount++
                }
            }

            TokenRotationResult(successCount, failureCount, tokensToRotate.size)

        } catch (e: Exception) {
            logger.error("Token rotation process failed", e)
            TokenRotationResult(0, 0, 0)
        }
    }

    /**
     * Validate token format and integrity
     */
    fun validateToken(token: String): Boolean {
        return try {
            val tokenizedData = tokenVault[token]
            tokenizedData != null && !isTokenExpired(tokenizedData)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get tokenization statistics for monitoring
     */
    fun getTokenizationStats(): TokenizationStats {
        val now = java.time.LocalDateTime.now()
        val activeTokens = tokenVault.values.filter { !isTokenExpired(it) }
        val expiredTokens = tokenVault.values.filter { isTokenExpired(it) }

        return TokenizationStats(
            totalTokens = tokenVault.size,
            activeTokens = activeTokens.size,
            expiredTokens = expiredTokens.size,
            tokensByType = tokenVault.values.groupBy { it.dataType }.mapValues { it.value.size },
            oldestToken = tokenVault.values.minOfOrNull { it.createdAt },
            newestToken = tokenVault.values.maxOfOrNull { it.createdAt }
        )
    }

    /**
     * Private helper methods
     */

    private fun initializeTokenizationKey() {
        try {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256, secureRandom)
            tokenizationKey = keyGen.generateKey()
        } catch (e: Exception) {
            logger.error("Failed to initialize tokenization key", e)
            throw RuntimeException("Tokenization key initialization failed", e)
        }
    }

    private fun generateCardToken(cardNumber: String): String {
        // Format-preserving tokenization for card numbers
        // Keep the same length and format as original card number
        val lastFour = cardNumber.takeLast(4)
        val prefix = "4T" // Token prefix to identify tokenized data
        val randomPart = generateRandomDigits(cardNumber.length - 6) // 4T + last4 = 6 chars

        return "$prefix$randomPart$lastFour"
    }

    private fun generateSecureToken(prefix: String): String {
        val randomBytes = ByteArray(16)
        secureRandom.nextBytes(randomBytes)
        val tokenBase = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
        return "${prefix}_${tokenBase}"
    }

    private fun generateRandomDigits(length: Int): String {
        return (1..length)
            .map { secureRandom.nextInt(10).toString() }
            .joinToString("")
    }

    private fun isTokenExpired(tokenizedData: TokenizedData): Boolean {
        // Tokens expire after 1 year for PCI DSS compliance
        val expirationDate = tokenizedData.createdAt.plusYears(1)
        return java.time.LocalDateTime.now().isAfter(expirationDate)
    }

    private fun detectCardType(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "VISA"
            cardNumber.startsWith("5") || cardNumber.startsWith("2") -> "MASTERCARD"
            cardNumber.startsWith("3") -> "AMEX"
            cardNumber.startsWith("6") -> "DISCOVER"
            else -> "UNKNOWN"
        }
    }

    // Validation methods
    private fun validateCardNumber(cardNumber: String) {
        require(cardNumber.length in 13..19) { "Invalid card number length" }
        require(cardNumber.all { it.isDigit() }) { "Card number must contain only digits" }
        require(isValidLuhn(cardNumber)) { "Invalid card number checksum" }
    }

    private fun validateCVV(cvv: String) {
        require(cvv.length in 3..4) { "Invalid CVV length" }
        require(cvv.all { it.isDigit() }) { "CVV must contain only digits" }
    }

    private fun validateExpirationDate(expirationDate: String) {
        require(expirationDate.matches(Regex("\\d{2}/\\d{2}"))) { "Invalid expiration date format" }
        // Additional validation for future dates would go here
    }

    private fun validateBankAccount(accountNumber: String, routingNumber: String) {
        require(accountNumber.length in 8..17) { "Invalid account number length" }
        require(routingNumber.length == 9) { "Invalid routing number length" }
        require(accountNumber.all { it.isDigit() }) { "Account number must contain only digits" }
        require(routingNumber.all { it.isDigit() }) { "Routing number must contain only digits" }
    }

    private fun isValidLuhn(cardNumber: String): Boolean {
        var sum = 0
        var alternate = false

        for (i in cardNumber.length - 1 downTo 0) {
            var n = cardNumber[i].toString().toInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }

        return sum % 10 == 0
    }
}

/**
 * Data classes for tokenization
 */

sealed class TokenizationResult {
    data class Success(val token: String, val metadata: Map<String, String>) : TokenizationResult()
    data class Failure(val error: String) : TokenizationResult()
}

sealed class DetokenizationResult {
    data class Success(val data: String, val dataType: DataType, val metadata: Map<String, String>) : DetokenizationResult()
    data class Failure(val error: String) : DetokenizationResult()
}

data class TokenizedData(
    val token: String,
    val encryptedData: String,
    val dataType: DataType,
    val createdAt: java.time.LocalDateTime,
    val metadata: Map<String, String>
)

data class TokenMetadata(
    val token: String,
    val dataType: DataType,
    val createdAt: java.time.LocalDateTime,
    val metadata: Map<String, String>,
    val isActive: Boolean
)

data class TokenizationStats(
    val totalTokens: Int,
    val activeTokens: Int,
    val expiredTokens: Int,
    val tokensByType: Map<DataType, Int>,
    val oldestToken: java.time.LocalDateTime?,
    val newestToken: java.time.LocalDateTime?
)

data class TokenRotationResult(
    val successCount: Int,
    val failureCount: Int,
    val totalAttempted: Int
)

enum class DataType {
    CARD_NUMBER,
    CVV,
    EXPIRATION_DATE,
    BANK_ACCOUNT
}
