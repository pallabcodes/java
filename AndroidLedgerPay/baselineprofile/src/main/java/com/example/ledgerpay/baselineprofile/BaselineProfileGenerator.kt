package com.example.ledgerpay.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = "com.example.ledgerpay",
            includeInStartupProfile = true
        ) {
            // Start the default activity
            pressHome()
            startActivityAndWait()
            
            // Note: In a real scenario, we would scroll lists here to optimize them
            // e.g., device.findObject(By.res("payment_list")).scroll(Direction.DOWN, 1.0f)
        }
    }
}
