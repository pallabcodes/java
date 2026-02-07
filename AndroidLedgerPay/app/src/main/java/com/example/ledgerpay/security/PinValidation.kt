package com.example.ledgerpay.security

object PinValidation {
    private const val PIN_PREFIX = "sha256/"
    private val BASE64_REGEX = Regex("^[A-Za-z0-9+/=]+$")

    fun validatedPins(
        isDebugBuild: Boolean,
        primaryPin: String,
        backupPin: String
    ): List<String> {
        val providedPins = listOf(primaryPin.trim(), backupPin.trim())
            .filter { it.isNotBlank() }

        if (isDebugBuild && providedPins.isEmpty()) {
            return emptyList()
        }

        if (providedPins.isEmpty()) {
            throw IllegalStateException(
                "Release build requires LedgerPay pins (primary + backup)."
            )
        }

        val normalized = providedPins.map(::normalizeAndValidatePin)
        if (normalized.size < 2) {
            throw IllegalStateException(
                "Release build requires at least two LedgerPay pins for rotation safety."
            )
        }

        if (normalized.toSet().size != normalized.size) {
            throw IllegalStateException("LedgerPay pins must be unique.")
        }

        return normalized
    }

    private fun normalizeAndValidatePin(pin: String): String {
        require(pin.startsWith(PIN_PREFIX)) {
            "Certificate pin must start with '$PIN_PREFIX'."
        }
        val hash = pin.removePrefix(PIN_PREFIX)
        require(hash.length == 44) {
            "Certificate pin hash must be 44 base64 characters."
        }
        require(BASE64_REGEX.matches(hash)) {
            "Certificate pin hash must be base64."
        }
        return pin
    }
}
