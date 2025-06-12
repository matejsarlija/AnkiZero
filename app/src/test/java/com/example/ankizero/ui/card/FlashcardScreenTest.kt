package com.example.ankizero.ui.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.ankizero.ui.theme.AnkiZeroTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

// Using Robolectric for better UI testing including native graphics.

@OptIn(ExperimentalCoroutinesApi::class) // For mainClock manipulation
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FlashcardViewAnimationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val initialFrontText = "Hello" // 5 chars
    private val initialBackText = "World"
    private val newFrontText = "New" // 3 chars

    // Animation parameters from implementation
    private val staggerDelay = 50L
    private val revealDuration = 150L // Duration of individual char scale animation

    @Test
    fun flashcardView_frontText_initiallyHidden() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = initialFrontText,
                    backText = initialBackText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }

        // Wait for a very short moment for initial composition.
        composeTestRule.mainClock.advanceTimeBy(10)


        initialFrontText.forEachIndexed { index, _ ->
            // If scaleY is 0 and TransformOrigin.Top, height should be 0.dp
            composeTestRule.onNodeWithTag("CharacterBoxFront-$index")
                .assertHeightIsEqualTo(0.dp)
        }
    }

    @Test
    fun flashcardView_frontText_revealsSequentially() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = initialFrontText,
                    backText = initialBackText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        // Initial state check after composition
        composeTestRule.mainClock.advanceTimeBy(10)
        initialFrontText.forEachIndexed { index, _ ->
             composeTestRule.onNodeWithTag("CharacterBoxFront-$index").assertHeightIsEqualTo(0.dp)
        }

        // Char 0 starts revealing after first staggerDelay
        composeTestRule.mainClock.advanceTimeBy(staggerDelay)
        composeTestRule.onNodeWithTag("CharacterBoxFront-0").assertHeightIsGreaterThan(0.dp)
        // Char 1 should still be hidden
        composeTestRule.onNodeWithTag("CharacterBoxFront-1").assertHeightIsEqualTo(0.dp)

        // Char 0 completes its animation (150ms), Char 1 has already had its staggerDelay (was simultaneous with char 0's animation)
        // So, advance by revealDuration (150ms) - staggerDelay (50ms) for char 0 to complete from this point,
        // and for char 1 to have been revealing for 50ms.
        composeTestRule.mainClock.advanceTimeBy(revealDuration) // Total elapsed: 10(initial) + 50 (stagger0) + 150 (reveal0_and_stagger_others)

        composeTestRule.onNodeWithTag("CharacterBoxFront-0").assertHeightIsGreaterThan(0.dp) // Fully visible
        composeTestRule.onNodeWithTag("CharacterBoxFront-1").assertHeightIsGreaterThan(0.dp) // Started
        if (initialFrontText.length > 2) {
            composeTestRule.onNodeWithTag("CharacterBoxFront-2").assertHeightIsGreaterThan(0.dp) // Started (due to overlapping delays)
        }


        // Advance by another staggerDelay for char 2 to be more fully revealed / char 3 to start
        composeTestRule.mainClock.advanceTimeBy(staggerDelay)
        if (initialFrontText.length > 3) {
            composeTestRule.onNodeWithTag("CharacterBoxFront-3").assertHeightIsGreaterThan(0.dp)
        }
    }

    @Test
    fun flashcardView_frontText_fullyVisibleAfterAnimation() {
        val text = "Hi" // Short text
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = text,
                    backText = initialBackText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }

        // Total time: (last char's stagger start) + (its own reveal duration)
        // Stagger for char 'i' is (length-1) * staggerDelay
        // total = (text.length -1) * staggerDelay + revealDuration should be enough for last char to finish
        // adding a small buffer
        val totalAnimationTime = ((text.length -1) * staggerDelay) + revealDuration + 50L
        composeTestRule.mainClock.advanceTimeBy(totalAnimationTime)

        text.forEachIndexed { index, _ ->
            composeTestRule.onNodeWithTag("CharacterBoxFront-$index")
                .assertHeightIsGreaterThan(0.dp)
        }
    }

    @Test
    fun flashcardView_backText_unaffectedByAnimation() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = initialFrontText,
                    backText = initialBackText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }

        composeTestRule.onNodeWithTag("FlashcardView").performClick() // Flip to back
        composeTestRule.waitForIdle()

        initialBackText.forEachIndexed { index, char ->
            val node = composeTestRule.onNodeWithTag("CharacterBoxBack-$index")
            node.assertIsDisplayed() // Regular display
            node.onChildren().assertAny(SemanticsMatcher.expectValue(SemanticsProperties.Text, listOf(char.toString())))
        }
    }

    @Test
    fun flashcardView_frontText_animationResetsOnFlip() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = initialFrontText,
                    backText = initialBackText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        composeTestRule.mainClock.advanceTimeBy(10) // initial composition

        // Let some animation run
        composeTestRule.mainClock.advanceTimeBy(staggerDelay * 2) // First two letters start revealing
        composeTestRule.onNodeWithTag("CharacterBoxFront-0").assertHeightIsGreaterThan(0.dp)
        composeTestRule.onNodeWithTag("CharacterBoxFront-1").assertHeightIsGreaterThan(0.dp)

        // Flip to back
        composeTestRule.onNodeWithTag("FlashcardView").performClick()
        composeTestRule.waitForIdle() // Wait for flip

        // Flip back to front
        composeTestRule.onNodeWithTag("FlashcardView").performClick()
        composeTestRule.waitForIdle() // Wait for flip

        // Animation should restart: all front text chars initially hidden (height 0)
        initialFrontText.forEachIndexed { index, _ ->
            composeTestRule.onNodeWithTag("CharacterBoxFront-$index").assertHeightIsEqualTo(0.dp)
        }

        // Advance time to see animation begin again for first char
        composeTestRule.mainClock.advanceTimeBy(staggerDelay)
        composeTestRule.onNodeWithTag("CharacterBoxFront-0").assertHeightIsGreaterThan(0.dp)
        composeTestRule.onNodeWithTag("CharacterBoxFront-1").assertHeightIsEqualTo(0.dp)
    }

    @Test
    fun flashcardView_frontText_animationResetsOnTextChange() {
        var currentText by mutableStateOf(initialFrontText)
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = currentText, // Initially "Hello"
                    backText = initialBackText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        composeTestRule.mainClock.advanceTimeBy(10)


        // Let "Hello" animation complete
        val totalInitialTime = ((initialFrontText.length - 1) * staggerDelay) + revealDuration + 50L
        composeTestRule.mainClock.advanceTimeBy(totalInitialTime)
        initialFrontText.forEachIndexed { index, _ ->
            composeTestRule.onNodeWithTag("CharacterBoxFront-$index").assertHeightIsGreaterThan(0.dp)
        }

        // Change text to "New"
        currentText = newFrontText
        composeTestRule.waitForIdle() // Recomposition

        // Verify animation restarts for "New": all chars initially hidden
        newFrontText.forEachIndexed { index, _ ->
            composeTestRule.onNodeWithTag("CharacterBoxFront-$index").assertHeightIsEqualTo(0.dp)
        }

        // Advance time to see "N" start revealing
        composeTestRule.mainClock.advanceTimeBy(staggerDelay)
        composeTestRule.onNodeWithTag("CharacterBoxFront-0").assertHeightIsGreaterThan(0.dp) // "N"
        if (newFrontText.length > 1) {
            composeTestRule.onNodeWithTag("CharacterBoxFront-1").assertHeightIsEqualTo(0.dp) // "e"
        }
    }
}


@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FlashcardViewStaticTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val frontText = "Hello"
    private val backText = "World"

    // Animation parameters for calculating total time
    private val staggerDelay = 50L
    private val revealDuration = 150L

    @Test
    fun flashcardView_displaysFrontText_afterAnimation() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = frontText,
                    backText = backText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        // Advance clock to ensure animation is complete for static text check
        val totalTime = ((frontText.length -1) * staggerDelay) + revealDuration + 50L // Buffer
        composeTestRule.mainClock.advanceTimeBy(totalTime)

        // Check the whole text is displayed (could be one node or multiple)
        composeTestRule.onNodeWithText(frontText).assertIsDisplayed()
        // More robust: check individual character boxes are displayed
         frontText.forEachIndexed { index, _ ->
            composeTestRule.onNodeWithTag("CharacterBoxFront-$index").onChildren()
                .assertAny(hasText(frontText[index].toString()))
        }
    }

    @Test
    fun flashcardView_displaysBackText_afterFlip() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = frontText,
                    backText = backText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        composeTestRule.onNodeWithTag("FlashcardView").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(backText).assertIsDisplayed()
    }

    @Test
    fun flashcardView_frontTextCharacters_areInBoxes_afterAnimation() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = frontText,
                    backText = backText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        val totalTime = ((frontText.length -1) * staggerDelay) + revealDuration + 50L
        composeTestRule.mainClock.advanceTimeBy(totalTime)

        frontText.forEachIndexed { index, char ->
            val charBoxNode = composeTestRule.onNodeWithTag("CharacterBoxFront-$index")
            charBoxNode.assertExists("Box for front character '$char' at index $index not found.")
            charBoxNode.onChildren().assertAny(SemanticsMatcher.expectValue(SemanticsProperties.Text, listOf(char.toString())))
        }
    }

    @Test
    fun flashcardView_backTextCharacters_areInBoxes() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = frontText,
                    backText = backText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        composeTestRule.onNodeWithTag("FlashcardView").performClick()
        composeTestRule.waitForIdle()

        backText.forEachIndexed { index, char ->
            val charBoxNode = composeTestRule.onNodeWithTag("CharacterBoxBack-$index")
            charBoxNode.assertExists("Box for back character '$char' at index $index not found.")
            charBoxNode.onChildren().assertAny(SemanticsMatcher.expectValue(SemanticsProperties.Text, listOf(char.toString())))
        }
    }

    @Test
    fun flashcardView_characterBoxes_haveTransparentBackgroundConceptually() {
        // This test confirms the Box structure, background is an implementation detail.
        composeTestRule.setContent {
            AnkiZeroTheme {
                FlashcardView(
                    frontText = frontText,
                    backText = backText,
                    onSwipeLeft = { },
                    onSwipeRight = { }
                )
            }
        }
        val totalTime = ((frontText.length -1) * staggerDelay) + revealDuration + 50L
        composeTestRule.mainClock.advanceTimeBy(totalTime)

        frontText.forEachIndexed { index, char ->
            composeTestRule.onNodeWithTag("CharacterBoxFront-$index")
                .assertExists("Box for front character '$char' at index $index should exist.")
        }

        composeTestRule.onNodeWithTag("FlashcardView").performClick()
        composeTestRule.waitForIdle()

        backText.forEachIndexed { index, char ->
            composeTestRule.onNodeWithTag("CharacterBoxBack-$index")
                .assertExists("Box for back character '$char' at index $index should exist.")
        }
    }
}
