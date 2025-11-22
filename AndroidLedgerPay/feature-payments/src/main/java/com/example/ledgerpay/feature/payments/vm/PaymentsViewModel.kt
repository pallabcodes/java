package com.example.ledgerpay.feature.payments.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledgerpay.core.data.PaymentsRepository
import com.example.ledgerpay.core.data.db.PaymentIntentEntity
import com.example.ledgerpay.core.data.monitoring.Monitoring
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Error(val message: String) : UiState()
    data class Success(val message: String) : UiState()
}

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val repo: PaymentsRepository,
    private val monitoring: Monitoring
) : ViewModel() {

    private val _intentId = MutableStateFlow<String?>(null)
    val intentId: StateFlow<String?> = _intentId.asStateFlow()

    private val _recent = MutableStateFlow<List<PaymentIntentEntity>>(emptyList())
    val recent: StateFlow<List<PaymentIntentEntity>> = _recent.asStateFlow()

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        loadRecentPayments()
    }

    fun createPayment(amountMinor: Long, currency: String) {
        monitoring.logUserAction("create_payment_attempted", mapOf(
            "amount_minor" to amountMinor,
            "currency" to currency
        ))

        // Security: Input validation in ViewModel layer
        if (!isValidAmount(amountMinor)) {
            monitoring.logSecurityEvent("invalid_payment_amount", mapOf("amount" to amountMinor))
            _state.value = UiState.Error("Invalid payment amount")
            return
        }

        if (!isValidCurrency(currency)) {
            monitoring.logSecurityEvent("invalid_currency_code", mapOf("currency" to currency))
            _state.value = UiState.Error("Invalid currency code")
            return
        }

        viewModelScope.launch {
            _state.value = UiState.Loading

            when (val result = repo.createIntent(amountMinor, currency)) {
                is PaymentsRepository.Result.Success -> {
                    _intentId.value = result.data
                    loadRecentPayments()
                    monitoring.logUserAction("create_payment_success", mapOf(
                        "payment_id" to result.data,
                        "amount_minor" to amountMinor,
                        "currency" to currency
                    ))
                    _state.value = UiState.Success("Payment intent created successfully")
                }
                is PaymentsRepository.Result.Error -> {
                    monitoring.logUserAction("create_payment_failed", mapOf(
                        "error" to result.exception.message,
                        "amount_minor" to amountMinor,
                        "currency" to currency
                    ))
                    _state.value = UiState.Error(mapErrorMessage(result.exception))
                }
            }
        }
    }

    private fun loadRecentPayments() {
        viewModelScope.launch {
            when (val result = repo.listRecent()) {
                is PaymentsRepository.Result.Success -> {
                    _recent.value = result.data
                    monitoring.logUserAction("recent_payments_loaded", mapOf(
                        "count" to result.data.size
                    ))
                }
                is PaymentsRepository.Result.Error -> {
                    // Don't show error for loading recent payments, just log
                    monitoring.logError(result.exception, "Failed to load recent payments")
                    _recent.value = emptyList()
                }
            }
        }
    }

    private fun isValidAmount(amount: Long): Boolean {
        return amount > 0 && amount <= 1_000_000_00L // Max $10,000
    }

    private fun isValidCurrency(currency: String): Boolean {
        return currency.matches(Regex("^[A-Z]{3}$"))
    }

    private fun mapErrorMessage(exception: Exception): String {
        return when (exception) {
            is IllegalArgumentException -> exception.message ?: "Invalid input"
            is SecurityException -> "Authentication required. Please log in again."
            is RuntimeException -> when (exception.message) {
                "Network unavailable" -> "No internet connection"
                "Request timeout" -> "Request timed out. Please try again."
                "Rate limit exceeded" -> "Too many requests. Please wait and try again."
                "Server error" -> "Server temporarily unavailable. Please try again later."
                else -> "An error occurred. Please try again."
            }
            else -> "An unexpected error occurred"
        }
    }

    fun clearError() {
        if (_state.value is UiState.Error) {
            monitoring.logUserAction("error_dismissed")
            _state.value = UiState.Idle
        }
    }
}
