package com.example.ledgerpay

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ComposeUiTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun showsHello() {
        composeRule.onNodeWithText("LedgerPay Hello 0").assertExists()
    }
}
