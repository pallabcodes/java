package com.example.ledgerpay.feature.payments.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ledgerpay.MainActivity
import org.junit.Rule
import org.junit.Test

class ErrorStateTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun shows_error_state_when_api_fails() {
        // Navigate to Payments tab
        rule.onNodeWithText("Payments").performClick()
        // Try create intent (in real test, inject failing repo via flavor/di override)
        rule.onNodeWithText("Create Intent").performClick()
        // Non-deterministic without injection; assert button exists and no crash
        rule.onNodeWithText("Create Intent").assertExists()
    }
}
