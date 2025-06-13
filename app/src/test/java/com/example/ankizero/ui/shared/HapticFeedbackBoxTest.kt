package com.example.ankizero.ui.shared

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
// import androidx.test.ext.junit.runners.AndroidJUnit4 // Removed for local test attempt
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.os.Build // Already present, but good to note
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.*

// @RunWith(AndroidJUnit4::class) // Removed for local test attempt
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
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
