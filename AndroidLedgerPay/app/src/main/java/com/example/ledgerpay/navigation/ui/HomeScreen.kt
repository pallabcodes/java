package com.example.ledgerpay.navigation.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(onGoPayments: () -> Unit) {
    Button(onClick = onGoPayments) { Text("Go to Payments") }
}
