package com.example.ledgerpay.core.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(text) }
}
