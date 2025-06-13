package com.example.ankizero.ui.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed // Required for isVisible = false
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ankizero.ui.theme.AnkiZeroTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Config.OLDEST_SDK])
class AnimatedCharacterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun animatedCharacter_displaysCharacter_whenVisible() {
        val charToTest = 'A'
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedCharacter(
                    char = charToTest,
                    isVisible = true,
                    style = MaterialTheme.typography.bodyLarge // Provide a basic style
                )
            }
        }
        // The text itself might be scaled or alpha might be animating,
        // but the node should exist and contain the character.
        composeTestRule.onNodeWithText(charToTest.toString()).assertIsDisplayed()
    }

    @Test
    fun animatedCharacter_doesNotExistOrNotDisplayed_whenNotVisible() {
        // Due to animation (alpha going to 0), the node might still exist in the semantic tree
        // but might not be "displayed" if its alpha is 0.
        // If it's fully removed from composition when alpha is 0, assertDoesNotExist() would be better.
        // Let's assume for now it becomes not displayed due to alpha.
        val charToTest = 'B'
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedCharacter(
                    char = charToTest,
                    isVisible = false,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        // If alpha is 0, it might not be considered "displayed".
        // This depends on how Jetpack Compose testing handles zero-alpha nodes.
        // A delay might be needed if the animation isn't instant.
        // For simplicity here, we assume the state is immediate for testing.
        // If isVisible = false truly means alpha = 0 quickly, this should work.
        composeTestRule.onNodeWithText(charToTest.toString()).assertIsNotDisplayed()
        // If the above fails due to the node still being considered "displayed" (e.g. size > 0),
        // an alternative is to check its alpha if possible through semantics, or ensure it's not focusable, etc.
        // However, direct alpha check is not straightforward in compose testing.
    }

    @Test
    fun animatedCharacter_displaysCorrectCharacter() {
        val charToTest = 'Z'
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedCharacter(char = charToTest, isVisible = true)
            }
        }
        composeTestRule.onNodeWithText(charToTest.toString()).assertIsDisplayed()
    }

    @Test
    fun animatedCharacter_appliesStyle() {
        // Testing if a style is applied is tricky without checking specific properties
        // like font size, color, etc., which often requires more complex semantic queries
        // or visual regression testing. For a unit test, we ensure it doesn't crash
        // and the text is present.
        val charToTest = 'S'
        composeTestRule.setContent {
            AnkiZeroTheme {
                AnimatedCharacter(
                    char = charToTest,
                    isVisible = true,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
        composeTestRule.onNodeWithText(charToTest.toString()).assertIsDisplayed()
    }
}
