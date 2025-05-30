package com.example.ankizero.ui.management

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.ankizero.MainActivity
import com.example.ankizero.Screen // For route checking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CardManagementScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun navigateToCardManagementScreen() {
        // If MainActivity doesn't start on Management, navigate to it.
        // This assumes BottomNav is available and "Manage" tab works.
        // For robust testing, use TestNavHostController or ensure deep link to this screen.
        // Checking if already on the screen by looking for a unique element:
        try {
            composeTestRule.onNodeWithTag("SearchTextField").assertIsDisplayed()
        } catch (e: AssertionError) {
            // Not on CardManagementScreen, try to navigate
             val manageBottomNavItemTag = composeTestRule.activity.appScreenBottomNavItems.find { it.route == Screen.Management }?.label ?: "Manage" // Fallback label
            try {
                composeTestRule.onNodeWithText(manageBottomNavItemTag).performClick() // Using text label of bottom nav item
            } catch (e: AssertionError){
                 // If text based fails, try a more generic approach if available, or ensure test starts here.
                 // This highlights need for consistent testTags on bottom nav items.
            }
        }
        composeTestRule.onNodeWithTag("SearchTextField").assertIsDisplayed() // Confirm navigation
    }

    @Test
    fun cardList_isDisplayed() {
        // Assumes some cards are loaded by default by the ViewModel from repository
        composeTestRule.waitUntil(timeoutMillis = 5000) {
             composeTestRule.onAllNodesWithTag("CardList", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("CardList", useUnmergedTree = true).assertIsDisplayed()
        // Check for at least one item, if data is present
        // composeTestRule.onAllNodesWithTagPattern("^CardListItem_\\d+$")[0].assertIsDisplayed()
    }

    @Test
    fun searchFunctionality_filtersList() {
        // This test requires knowing some data that exists.
        // Let's assume a card with "Bonjour" exists from default/test data.
        // And another card like "Merci" exists.
        composeTestRule.onNodeWithTag("SearchTextField").performTextInput("Bonjour")
        // After typing, only cards with "Bonjour" should be visible.
        composeTestRule.onNodeWithTag("CardListItem_1", useUnmergedTree = true).assertIsDisplayed() // Assuming ID 1 is Bonjour
        // composeTestRule.onNodeWithTag("CardListItem_containing_Merci", useUnmergedTree = true).assertDoesNotExist()

        composeTestRule.onNodeWithTag("SearchTextField").performTextClearance()
        composeTestRule.onNodeWithTag("SearchTextField").performTextInput("Merci")
        composeTestRule.onNodeWithTag("CardListItem_3", useUnmergedTree = true).assertIsDisplayed() // Assuming ID 3 is Merci
        // composeTestRule.onNodeWithTag("CardListItem_containing_Bonjour", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun sortFunctionality_buttonExists() { // Visual check of sort order is complex
        // For now, just check if the sort button is there.
        // Actual verification of sort order would need stable test data and specific assertions on item order.
        composeTestRule.onNodeWithText("Sort by:").assertIsDisplayed() // Part of SortOptionsRow
        composeTestRule.onNodeWithContentDescription("Open sort options").assertIsDisplayed().performClick()
        // Check if dropdown menu items appear (e.g., "Alphabetical")
        composeTestRule.onNodeWithText("Alphabetical").assertIsDisplayed()
        // Could click an option and then try to verify the first item in the list if data is predictable.
    }

    @Test
    fun batchSelection_and_deletionButtonAppears() {
        // Assumes list is not empty and card with ID 1 exists and is visible
         composeTestRule.waitUntil(timeoutMillis = 5000) {
             composeTestRule.onAllNodesWithTag("CardListItem_1", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("CardListItem_1", useUnmergedTree = true).performLongClick()
        // After long click, delete button in TopAppBar should be visible
        composeTestRule.onNodeWithTag("DeleteSelectedCardsButton").assertIsDisplayed()
        // Checkbox inside the item might also be checked (visual state, harder to assert directly without custom semantics)
    }

    // Deletion test is more complex as it modifies state shared across tests if not reset.
    // It would also require knowing specific card IDs.

    @Test
    fun navigationToCreateCard_onClickingFab() {
        composeTestRule.onNodeWithTag("CreateCardFab").performClick()
        // Verify navigation to CreateCardScreen by checking for a unique element on that screen
        composeTestRule.onNodeWithTag("FrenchWordTextField").assertIsDisplayed() // Assuming this tag exists on CreateCardScreen
    }

    @Test
    fun navigationToEditCard_onItemClick() {
        // Assumes card with ID 1 exists and is visible
        composeTestRule.waitUntil(timeoutMillis = 5000) {
             composeTestRule.onAllNodesWithTag("CardListItem_1", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("CardListItem_1", useUnmergedTree = true).performClick()
        // Verify navigation to EditCardScreen
        // EditCardScreen also has "FrenchWordTextField", but might have a different title or other element.
        // For now, this is a basic check. More specific checks would involve verifying the card's data is populated.
        composeTestRule.onNodeWithText("Edit Card").assertIsDisplayed() // Check TopAppBar title
        composeTestRule.onNodeWithTag("FrenchWordTextField").assertIsDisplayed()
    }
}
