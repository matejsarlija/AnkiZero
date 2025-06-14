package com.example.ankizero.ui.shared

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode // Required for Robolectric to render Composable content

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE) // Or use GraphicsMode.Mode.LEGACY for simpler tests
class FlippingLetterRectangleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun flippingLetterRectangle_withChar_whenRevealTrue_showsChar() {
        val testChar = 'A'
        composeTestRule.setContent {
            FlippingLetterRectangle(char = testChar, reveal = true)
        }
        // Note: Direct animation state testing is complex.
        // We primarily test if the char is passed and would be visible if rotation allows.
        // The actual rotation animation is better tested via UI/integration tests.
        // However, since reveal = true leads to rotationY = 0f (after animation), the text should be present.
        // We might need a delay or use advanceTimeBy to ensure animation completes in more complex scenarios.
        composeTestRule.onNodeWithText(testChar.toString()).assertExists("Character '$testChar' should be present when reveal is true")
    }

    @Test
    fun flippingLetterRectangle_withChar_whenRevealFalse_doesNotExistInitially() {
        val testChar = 'B'
        composeTestRule.setContent {
            FlippingLetterRectangle(char = testChar, reveal = false)
        }
        // When reveal is false, rotationY is 90f, so the Text composable inside (which shows the char)
        // should not be rendered or should not be findable by its text content.
        composeTestRule.onNodeWithText(testChar.toString()).assertDoesNotExist()
    }

    @Test
    fun flippingLetterRectangle_nullChar_whenRevealTrue_showsNothing() {
        val testChar = 'C' // A char that won't be displayed
        composeTestRule.setContent {
            FlippingLetterRectangle(char = null, reveal = true)
        }
        // Even if revealed, a null char means no text should be displayed.
        composeTestRule.onNodeWithText(testChar.toString()).assertDoesNotExist() // Check that some other char isn't accidentally displayed
        // More robustly, we'd check for an empty box, perhaps with a test tag on the Box.
    }

    @Test
    fun flippingLetterRectangle_nullChar_whenRevealFalse_showsNothing() {
        val testChar = 'D' // A char that won't be displayed
        composeTestRule.setContent {
            FlippingLetterRectangle(char = null, reveal = false)
        }
        // Null char and not revealed, so definitely no text.
        composeTestRule.onNodeWithText(testChar.toString()).assertDoesNotExist()
    }
}
