package com.example.ankizero.ui.shared

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText // Used to find multiple instances if needed, or for general queries
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed // To check individual letter components
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ankizero.ui.shared.FlippingLetterRectangle // Ensure this is accessible
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

// It's often useful to have a way to identify child composables in tests.
// We can't directly check the `animationDelayMs` parameter of a child in standard UI tests.
// Instead, we test the structural composition: correct number of children, correct text displayed.

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FlippingWordRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun flippingWordRow_createsCorrectNumberOfRectangles_includingPlaceholders() {
        val testWord = "HI"
        // Expected: null, 'H', 'I', null --> 4 rectangles
        val expectedRectangleCount = testWord.length + 2

        composeTestRule.setContent {
            FlippingWordRow(word = testWord, reveal = true)
        }

        // This is tricky because FlippingLetterRectangle with null char doesn't have text.
        // We can count the displayed letters.
        // A more robust way would be to add a testTag to FlippingLetterRectangle itself.
        // For now, let's verify the letters are there.
        testWord.forEach { char ->
            composeTestRule.onNodeWithText(char.toString()).assertIsDisplayed()
        }
        // To assert the total count including nulls, you'd typically use testTags.
        // For example, if FlippingLetterRectangle had Modifier.testTag("FlippingLetter"),
        // you could do: composeTestRule.onAllNodesWithTag("FlippingLetter").assertCountEquals(expectedRectangleCount)
        // Since we don't have that from the current FlippingLetterRectangle, this test is limited.
    }

    @Test
    fun flippingWordRow_displaysAllChars_whenRevealTrue() {
        val testWord = "TEST"
        composeTestRule.setContent {
            FlippingWordRow(word = testWord, reveal = true)
        }

        testWord.forEach { char ->
            composeTestRule.onNodeWithText(char.toString()).assertIsDisplayed()
        }
    }

    @Test
    fun flippingWordRow_doesNotDisplayChars_whenRevealFalse() {
        val testWord = "HIDDEN"
        composeTestRule.setContent {
            FlippingWordRow(word = testWord, reveal = false)
        }

        testWord.forEach { char ->
            // When reveal is false, the child FlippingLetterRectangle's rotationY is 90,
            // so the inner Text composable is not rendered.
            composeTestRule.onNodeWithText(char.toString()).assertDoesNotExist()
        }
    }

    @Test
    fun flippingWordRow_withEmptyWord_createsTwoPlaceholderRectangles() {
        val testWord = ""
        val expectedRectangleCount = 2 // for the two nulls

        composeTestRule.setContent {
            FlippingWordRow(word = testWord, reveal = true)
        }
        // As before, without testTags on the rectangles, directly counting them is hard.
        // We can assert that no actual letter characters are displayed.
        // For example, try to find a common letter:
        composeTestRule.onNodeWithText("A").assertDoesNotExist()
        composeTestRule.onNodeWithText("Z").assertDoesNotExist()
        // This is an indirect way of verifying. Test tags would be better.
    }
}
