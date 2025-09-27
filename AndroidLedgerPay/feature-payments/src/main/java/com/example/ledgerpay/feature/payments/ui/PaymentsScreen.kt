package com.example.ledgerpay.feature.payments.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ledgerpay.feature.payments.vm.PaymentsViewModel
import com.example.ledgerpay.feature.payments.vm.UiState

@Composable
fun PaymentsScreen(vm: PaymentsViewModel = hiltViewModel()) {
    val id = vm.intentId.collectAsState().value
    val recent = vm.recent.collectAsState().value
    val state = vm.state.collectAsState().value
    Column {
        Button(onClick = { vm.create(1000, "USD") }) { Text("Create Intent") }
        when (state) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> Text("Error: ${state.message}")
            else -> {}
        }
        if (id != null) { Text("Intent: $id") }
        LazyColumn {
            items(recent) { it -> Text("${it.id} ${it.amountMinor} ${it.currency}") }
        }
    }
}
