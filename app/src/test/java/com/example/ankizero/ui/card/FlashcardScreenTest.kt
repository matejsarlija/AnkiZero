package com.example.ankizero.ui.card

import androidx.compose.ui.Modifier
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Config.OLDEST_SDK])
class FlashcardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun flashcardView_displaysFrontText_initially() {
        val front = "Front Text"
        val back = "Back Text"
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = front,
                    backText = back,
                    onSwipeLeft = {},
                    onSwipeRight = {}
                )
            }
        }
        // Check for AnimatedText via its testTag
        composeTestRule.onNodeWithTag("CardFrontAnimatedText").assertIsDisplayed()
        // And check content (AnimatedText should display this)
        composeTestRule.onNodeWithText(front).assertIsDisplayed()
        composeTestRule.onNodeWithText(back).assertIsNotDisplayed()
    }

    @Test
    fun flashcardView_displaysBackText_afterFlip() {
        val front = "Front Text"
        val back = "Back Text"
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    modifier = Modifier.testTag("FlashcardView"),
                    frontText = front,
                    backText = back,
                    onSwipeLeft = {},
                    onSwipeRight = {}
                )
            }
        }
        // Perform click to flip
        composeTestRule.onNodeWithTag("FlashcardView").performClick()

        // After flip, back text should be visible.
        // AnimatedText (front) should become not visible.
        // Wait for animations if necessary, though isVisible should update quickly.
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText(back).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithText(front).assertIsNotDisplayed() // Front text (AnimatedText) should be hidden
        composeTestRule.onNodeWithText(back).assertIsDisplayed() // Back text should be visible
    }

    @Test
    fun flashcardView_callsOnSwipeLeft_onLeftSwipe() {
        var swipedLeft = false
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    modifier = Modifier.testTag("FlashcardView"),
                    frontText = "Swipe Test",
                    backText = "Back",
                    onSwipeLeft = { swipedLeft = true },
                    onSwipeRight = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("FlashcardView").performTouchInput { swipeLeft() }
        assertTrue(swipedLeft)
    }

    @Test
    fun flashcardView_callsOnSwipeRight_onRightSwipe() {
        var swipedRight = false
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    modifier = Modifier.testTag("FlashcardView"),
                    frontText = "Swipe Test",
                    backText = "Back",
                    onSwipeLeft = {},
                    onSwipeRight = { swipedRight = true }
                )
            }
        }
        composeTestRule.onNodeWithTag("FlashcardView").performTouchInput { swipeRight() }
        assertTrue(swipedRight)
    }

    // Add more tests:
    // - PaperGrid presence (e.g., by adding a testTag to PaperGrid within FlashcardView if possible,
    //   or by visual inspection in UI tests as canvas content is hard to verify here).
    // - Correctness of the `modifier` application to the root Card of FlashcardView.
}
