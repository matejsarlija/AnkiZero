package com.example.ankizero.ui.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ankizero.ui.theme.AnkiZeroTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Config.OLDEST_SDK])
class EnhancedButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun enhancedButton_displaysText() {
        val buttonText = "Click Me"
        composeTestRule.setContent {
            AnkiZeroTheme {
                EnhancedButton(
                    onClick = { },
                    text = buttonText,
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            }
        }
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    @Test
    fun enhancedButton_isClickable_whenEnabled() {
        var clicked = false
        val buttonText = "Enabled Button"
        composeTestRule.setContent {
            AnkiZeroTheme {
                EnhancedButton(
                    onClick = { clicked = true },
                    text = buttonText,
                    containerColor = Color.Green,
                    contentColor = Color.Black,
                    enabled = true,
                    modifier = Modifier.testTag("enhancedButton")
                )
            }
        }
        composeTestRule.onNodeWithTag("enhancedButton").performClick()
        assertTrue(clicked)
    }

    @Test
    fun enhancedButton_isNotClickable_whenDisabled() {
        var clicked = false
        val buttonText = "Disabled Button"
        composeTestRule.setContent {
            AnkiZeroTheme {
                EnhancedButton(
                    onClick = { clicked = true },
                    text = buttonText,
                    containerColor = Color.Gray,
                    contentColor = Color.DarkGray,
                    enabled = false,
                    modifier = Modifier.testTag("enhancedButton")
                )
            }
        }
        // Perform click should not throw an error, but the onClick lambda shouldn't be invoked.
        // We also need to check that it's not enabled.
        composeTestRule.onNodeWithTag("enhancedButton").assertIsNotEnabled()

        // Try to perform click - it shouldn't change 'clicked' value
        try {
            composeTestRule.onNodeWithTag("enhancedButton").performClick()
        } catch (e: java.lang.AssertionError) {
            // Expected if performClick asserts enabled, or if the click is somehow blocked.
            // If performClick doesn't throw for disabled, that's fine, just check 'clicked'.
        }
        assertFalse(clicked)
    }

    @Test
    fun enhancedButton_appliesModifier() {
        val buttonText = "Modifier Test"
        val testTag = "myButtonTag"
        composeTestRule.setContent {
            AnkiZeroTheme {
                EnhancedButton(
                    onClick = { },
                    text = buttonText,
                    containerColor = Color.Cyan,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag(testTag)
                )
            }
        }
        composeTestRule.onNodeWithTag(testTag).assertExists()
    }

    // Test for press animation state could be complex as it involves internal state 'isPressed'
    // and visual change (scale). Directly testing the scale value is hard in unit tests.
    // We can test that the component handles pointer input without crashing.
    @Test
    fun enhancedButton_handlesPointerInputForAnimation() {
        val buttonText = "Press Test"
        composeTestRule.setContent {
            AnkiZeroTheme {
                EnhancedButton(
                    onClick = { },
                    text = buttonText,
                    containerColor = Color.Magenta,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("pressButton")
                )
            }
        }
        // Simulate a press (drag start) and release (drag end)
        // This test mainly ensures that these gestures don't cause crashes.
        // Actual visual verification of scale is for UI/visual regression tests.
        composeTestRule.onNodeWithTag("pressButton").performTouchInput {
            down(center) // Simulates press down
            // moveBy(Offset(10f, 0f)) // Optional: simulate small drag if that's part of gesture
            up()         // Simulates release
        }
        // No crash means the pointer input handling is likely okay at a basic level.
        composeTestRule.onNodeWithTag("pressButton").assertIsDisplayed() // Still displayed
    }
}
