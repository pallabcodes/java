package com.example.ledgerpay.feature.payments.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledgerpay.core.data.PaymentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.ledgerpay.core.data.db.PaymentIntentEntity

sealed class UiState {
    data object Idle: UiState()
    data object Loading: UiState()
    data class Error(val message: String): UiState()
}

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val repo: PaymentsRepository
) : ViewModel() {
    private val _intentId = MutableStateFlow<String?>(null)
    val intentId: StateFlow<String?> = _intentId

    private val _recent = MutableStateFlow<List<PaymentIntentEntity>>(emptyList())
    val recent: StateFlow<List<PaymentIntentEntity>> = _recent

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun create(amountMinor: Long, currency: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _intentId.value = repo.createIntent(amountMinor, currency)
                _recent.value = repo.listRecent()
                _state.value = UiState.Idle
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "unknown error")
            }
        }
    }
}
