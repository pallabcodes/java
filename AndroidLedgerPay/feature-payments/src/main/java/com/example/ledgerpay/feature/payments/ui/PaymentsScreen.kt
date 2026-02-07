package com.example.ledgerpay.feature.payments.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    val isLoading = state is UiState.Loading
    val context = androidx.compose.ui.platform.LocalContext.current

    Column {
        Button(
            enabled = !isLoading,
            onClick = { vm.createPayment(1000, "USD") }
        ) { 
            Text(text = androidx.compose.ui.res.stringResource(com.example.ledgerpay.feature.payments.R.string.create_intent_button)) 
        }
        when (state) {
            is UiState.Loading -> CircularProgressIndicator(
                modifier = androidx.compose.ui.Modifier.semantics.semantics {
                    contentDescription = context.getString(com.example.ledgerpay.feature.payments.R.string.loading_description)
                }
            )
            is UiState.Error -> Text(text = androidx.compose.ui.res.stringResource(com.example.ledgerpay.feature.payments.R.string.error_prefix, state.message))
            is UiState.Success -> Text(text = state.message)
            else -> {}
        }
        if (id != null) { 
            Text(text = androidx.compose.ui.res.stringResource(com.example.ledgerpay.feature.payments.R.string.intent_label_prefix, id)) 
        }
        LazyColumn {
            items(recent) { item -> 
                Text(
                    text = "${item.id} ${item.amountMinor} ${item.currency}",
                    modifier = androidx.compose.ui.Modifier.semantics.semantics {
                        contentDescription = context.getString(
                            com.example.ledgerpay.feature.payments.R.string.payment_item_description, 
                            item.amountMinor, 
                            item.currency, 
                            item.id
                        )
                    }
                ) 
            }
        }
    }
}
