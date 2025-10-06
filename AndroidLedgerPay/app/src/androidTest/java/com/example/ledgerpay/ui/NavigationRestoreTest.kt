package com.example.ledgerpay.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ledgerpay.MainActivity
import org.junit.Rule
import org.junit.Test

class NavigationRestoreTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigate_between_tabs_restores_screens() {
        // Payments tab
        rule.onNodeWithText("Payments").performClick()
        rule.onNodeWithText("Create Intent").assertExists()
        // Ledger tab
        rule.onNodeWithText("Ledger").performClick()
        rule.onNodeWithText("Ledger Screen").assertExists()
        // Back to Home
        rule.onNodeWithText("Home").performClick()
        // Home currently has button labeled Go to Payments
        rule.onNodeWithText("Go to Payments").assertExists()
    }
}
