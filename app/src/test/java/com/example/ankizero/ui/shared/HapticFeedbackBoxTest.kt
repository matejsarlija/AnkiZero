package com.example.ankizero.ui.shared

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class HapticFeedbackBoxTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hapticFeedbackBox_triggersOnClick() {
        var clicked = false
        composeTestRule.setContent {
            HapticFeedbackBox(
                onClick = { clicked = true },
                contentDescription = "haptic-box"
            ) {
                androidx.compose.material3.Text("Tap me")
            }
        }
        composeTestRule.onNodeWithContentDescription("haptic-box").performClick()
        assert(clicked)
    }
}
