package com.example.ledgerpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ledgerpay.core.ui.PrimaryButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { com.example.ledgerpay.navigation.ui.AppScaffold() }
    }
}

@Composable
fun LedgerPayAppRoot() {
    Surface(color = MaterialTheme.colorScheme.background) {
        val cnt = remember { mutableStateOf(0) }
        PrimaryButton(text = "Increment") { cnt.value++ }
    }
}

@Preview
@Composable
fun PreviewLedgerPay() {
    LedgerPayAppRoot()
}
