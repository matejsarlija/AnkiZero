package com.example.ankizero.ui.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed // For isVisible = false
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ankizero.ui.theme.AnkiZeroTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest // For advancing clocks if animations were longer/complex
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi // For runTest if needed
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Config.OLDEST_SDK])
class AnimatedTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun animatedText_typewriter_eventuallyDisplaysAllCharacters_whenVisible() {
        val textToTest = "Hello"
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedText(
                    text = textToTest,
                    isVisible = true,
                    animationType = TextAnimationType.TYPEWRITER,
                    charRevealDelayMillis = 1L // Very short delay for testing
                )
            }
        }

        // Wait for the animation to complete.
        // The total time would be textToTest.length * charRevealDelayMillis.
        // For "Hello" (5 chars) * 1ms = 5ms.
        // composeTestRule.mainClock.advanceTimeBy(textToTest.length * 1L + 1) // Ensure it finishes
        // Advancing clock with Robolectric might need specific setup or coroutine test dispatcher.
        // For simplicity, let's assume default behavior makes it appear within reasonable test time.
        // A more robust test would use test dispatchers and advanceUntilIdle.

        // Check each character individually - this is too granular and might fail due to timing.
        // Instead, check the whole text after a reasonable delay for the animation.
        // Since the delay is 1ms per char, it should be quick.

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            // This condition checks if all characters are individually findable.
            // It assumes AnimatedText renders each character in its own node structure via AnimatedCharacter.
            // If AnimatedText renders the full string into a single Text node eventually, this needs adjustment.
            // Based on current AnimatedText impl, it uses a Row of AnimatedCharacters.
            var allFound = true
            for (char_ in textToTest) {
                // This is not ideal as onNodeWithText(char.toString()) might find multiple if chars repeat.
                // And it doesn't guarantee they are *all* visible from our AnimatedText.
                // This test is limited by how AnimatedText is structured internally.
                // A better test might involve testTags on each AnimatedCharacter.
            }
            // A simpler check: the full text is displayed. This assumes the Row of AnimatedCharacters
            // would form the full text semantically.
            try {
                composeTestRule.onNodeWithText(textToTest).assertIsDisplayed()
                true // text is found and displayed
            } catch (e: AssertionError) {
                false // text not yet displayed
            }
        }
        // Final assertion after waitUntil
        composeTestRule.onNodeWithText(textToTest).assertIsDisplayed()
    }

    @Test
    fun animatedText_fadeInAll_displaysAllCharacters_whenVisible() {
        val textToTest = "FadeIn"
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedText(
                    text = textToTest,
                    isVisible = true,
                    animationType = TextAnimationType.FADE_IN_ALL,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        // For FADE_IN_ALL, all characters should be visible immediately (animation is on alpha of each character)
        composeTestRule.onNodeWithText(textToTest).assertIsDisplayed()
    }

    @Test
    fun animatedText_isNotDisplayed_whenNotVisible() {
        val textToTest = "Hidden"
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedText(
                    text = textToTest,
                    isVisible = false,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        // The entire AnimatedText component's content should not be displayed.
        // This relies on AnimatedCharacter correctly becoming not displayed.
        composeTestRule.onNodeWithText(textToTest).assertIsNotDisplayed()
    }

    @Test
    fun animatedText_displaysCorrectText_whenVisible() {
        val textToTest = "Correct Text"
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedText(
                    text = textToTest,
                    isVisible = true,
                    animationType = TextAnimationType.FADE_IN_ALL // Easier to test correctness
                )
            }
        }
        composeTestRule.onNodeWithText(textToTest).assertIsDisplayed()
    }
}
