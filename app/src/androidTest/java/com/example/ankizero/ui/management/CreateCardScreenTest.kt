package com.example.ankizero.ui.management

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.ankizero.MainActivity
import org.junit.Rule
import org.junit.Test

class CreateCardScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createCardFlow_enterTextAndSave_navigatesBackAndShowsCard() {
        // 1. Navigate to Card Management Screen (if not already there)
        //    and then to Create Card Screen
        val manageBottomNavItemTag = composeTestRule.activity.appScreenBottomNavItems
            .find { it.route == com.example.ankizero.Screen.Management }?.label ?: "Manage"
        try {
            composeTestRule.onNodeWithTag("SearchTextField").assertIsDisplayed()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText(manageBottomNavItemTag).performClick()
            } catch (e2: AssertionError) { /* Already there or navigation failed */ }
        }
        composeTestRule.onNodeWithTag("CreateCardFab").performClick()

        // 2. Verify we are on Create Card Screen
        composeTestRule.onNodeWithText("Create New Card").assertIsDisplayed() // TopAppBar title

        // 3. Enter text into required fields
        val frenchWord = "Test French ${System.currentTimeMillis()}" // Unique word
        val englishWord = "Test English ${System.currentTimeMillis()}"
        composeTestRule.onNodeWithTag("FrenchWordTextField").performTextInput(frenchWord)
        composeTestRule.onNodeWithTag("EnglishTranslationTextField").performTextInput(englishWord)

        // 4. Click Save
        composeTestRule.onNodeWithTag("SaveCardButton").performClick()

        // 5. Verify navigation back to Card Management Screen
        //    Check for an element unique to CardManagementScreen, e.g., the FAB or search field.
        composeTestRule.onNodeWithTag("CreateCardFab").assertIsDisplayed() // Back on management screen

        // 6. Verify the new card appears in the list
        //    This requires the list to update and the new card to be findable.
        //    The default sorting is by "Recent", so it should be near the top.
        //    Scrolling may be needed if the list is long.
        //    This assertion is challenging without knowing the exact ID or having full control over the data.
        //    A simpler check might be to search for it.
        composeTestRule.onNodeWithTag("SearchTextField").performTextClearance()
        composeTestRule.onNodeWithTag("SearchTextField").performTextInput(frenchWord)

        // Wait for search results to populate
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(frenchWord, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(frenchWord, substring = true).assertIsDisplayed()
    }

    @Test
    fun createCard_validation_showsErrorForEmptyFields() {
        // 1. Navigate to Create Card Screen
         val manageBottomNavItemTag = composeTestRule.activity.appScreenBottomNavItems
            .find { it.route == com.example.ankizero.Screen.Management }?.label ?: "Manage"
        try {
            composeTestRule.onNodeWithTag("SearchTextField").assertIsDisplayed()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText(manageBottomNavItemTag).performClick()
            } catch (e2: AssertionError) { /* Already there or navigation failed */ }
        }
        composeTestRule.onNodeWithTag("CreateCardFab").performClick()

        // 2. Attempt to Save with empty fields
        composeTestRule.onNodeWithTag("SaveCardButton").performClick()

        // 3. Verify error messages are shown (assuming testTags were added to error Text composables)
        composeTestRule.onNodeWithTag("FrenchWordErrorText").assertIsDisplayed()
        composeTestRule.onNodeWithTag("EnglishTranslationErrorText").assertIsDisplayed()

        // 4. Verify still on Create Card Screen (Save action should have been blocked)
        composeTestRule.onNodeWithText("Create New Card").assertIsDisplayed() // TopAppBar title
    }
}
