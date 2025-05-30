# Manual Database Testing Plan for AnkiZero

This document outlines manual testing scenarios to verify database integration and core data-related functionalities in the AnkiZero application. These tests should be performed on a physical device or emulator.

## Prerequisites:
- A debug build of the AnkiZero application with all database and repository logic integrated.
- Access to Android Studio's App Inspection tool (for direct database verification) or other methods to inspect the app's database.
- Familiarity with the app's UI for card creation, review, and management.

## I. Card Creation and Persistence:

1.  **Via "Create Card" Screen:**
    *   **Action:** Navigate to Card Management -> Tap "+" (FAB) to go to Create Card Screen.
    *   **Input:** Fill in all fields:
        *   French Word: "Maison"
        *   English Translation: "House"
        *   Pronunciation: "meh-zohn"
        *   Example Sentence: "C'est une grande maison."
        *   Notes: "Feminine noun."
        *   Difficulty: Set to 3.
    *   **Action:** Tap "Save".
    *   **Verification (UI):**
        *   User is navigated back to Card Management screen.
        *   The new card "Maison" appears in the list.
    *   **Verification (DB):**
        *   Inspect `flashcard_table` in the database.
        *   Confirm a new row exists for "Maison".
        *   Verify all fields match the input.
        *   Verify `creationDate` and `nextReviewDate` are set (e.g., to current time).
        *   Verify `intervalInDays` is `1.0` and `easeFactor` is `2.5` (or other defined defaults for new cards).
        *   Verify `reviewCount` is `0`.

2.  **Via OCR Screen (Simulated Text):**
    *   **Action:** Navigate to OCR Screen.
    *   **Input:** (Assuming OCR simulation) Trigger OCR processing (e.g., capture/import, leading to simulated text "Voiture\nCar").
    *   **Action:** In the "Edit Recognized Text" dialog, ensure text is as expected. Tap "Save".
    *   **Verification (UI):**
        *   Dialog closes.
        *   Navigate to Card Management screen. The new card "Voiture" should appear.
    *   **Verification (DB):**
        *   Inspect `flashcard_table`.
        *   Confirm a new row exists for "Voiture".
        *   Verify `frenchWord` is "Voiture" and `englishTranslation` is "Car" (or based on parsing logic in `OcrViewModel.saveRecognizedText`).
        *   Verify default SRS parameters are set as per new card logic.

3.  **Data Persistence Across App Sessions:**
    *   **Action:** After creating a few cards, fully close the AnkiZero app (swipe away from recents or force stop).
    *   **Action:** Re-open the app.
    *   **Verification (UI):** Navigate to Card Management. All previously created cards should still be present.
    *   **Verification (DB):** Data should remain unchanged in the database.

## II. Card Review and SRS Logic:

1.  **Review a New Card - Mark "Memorized":**
    *   **Setup:** Ensure a new card (e.g., "Maison" from I.1) is due for review (its `nextReviewDate` should be current or past).
    *   **Action:** Navigate to Flashcard Review screen. The new card should appear.
    *   **Action:** Tap "Memorized".
    *   **Verification (UI):** The card is removed from the current review session (or screen updates to next card/empty state).
    *   **Verification (DB):**
        *   Inspect the "Maison" card in `flashcard_table`.
        *   `nextReviewDate` should be updated to a future date (e.g., creationDate + approx `1.0 * 1.8` days).
        *   `intervalInDays` should be updated (e.g., approx `1.0 * 1.8`).
        *   `easeFactor` should increase slightly (e.g., `2.5 + 0.1 = 2.6`).
        *   `lastReviewed` should be set to the current time.
        *   `reviewCount` should be `1`.

2.  **Review a Card - Mark "No":**
    *   **Setup:** Use a card, either new or previously reviewed (e.g., "Voiture" from I.2). Ensure it's due.
    *   **Action:** Navigate to Flashcard Review screen. The card appears.
    *   **Action:** Tap "No".
    *   **Verification (UI):** Card removed from current session / screen updates.
    *   **Verification (DB):**
        *   Inspect the card in `flashcard_table`.
        *   `nextReviewDate` should be updated to roughly tomorrow.
        *   `intervalInDays` should reset to `1.0`.
        *   `easeFactor` should decrease (e.g., `2.5 - 0.2 = 2.3`, minimum 1.3).
        *   `lastReviewed` updated.
        *   `reviewCount` incremented.

3.  **Due Card Logic:**
    *   **Setup:** Have a mix of cards: one due today, one due tomorrow, one reviewed "Memorized" (due in several days).
    *   **Action:** Navigate to Flashcard Review screen.
    *   **Verification (UI):** Only the card(s) with `nextReviewDate` <= current date should appear for review.
    *   **Action:** Review all due cards until the "No cards due" message appears.
    *   **Verification (DB):** All cards just reviewed should have their `nextReviewDate` moved to the future.

## III. Card Management:

1.  **Edit a Card:**
    *   **Action:** Navigate to Card Management. Tap on an existing card (e.g., "Maison").
    *   **Input:** In Edit Card Screen, change English translation to "Big House" and notes to "Updated notes." Tap "Save."
    *   **Verification (UI):**
        *   User returns to Card Management.
        *   The "Maison" card entry in the list should reflect "Big House" (if English is shown) or updated details when re-opened.
    *   **Verification (DB):** Inspect the "Maison" card. `englishTranslation` and `notes` fields should be updated. Other fields like SRS params should remain unchanged.

2.  **Delete a Single Card:**
    *   **Action:** Navigate to Card Management. Long-press on a card (e.g., "Voiture") to select it. Tap the "Delete" icon in TopAppBar. (Confirm if dialog appears).
    *   **Verification (UI):** The "Voiture" card is removed from the list.
    *   **Verification (DB):** The row for "Voiture" is deleted from `flashcard_table`.

3.  **Delete Multiple Cards:**
    *   **Setup:** Ensure at least 2-3 cards exist.
    *   **Action:** Navigate to Card Management. Long-press one card, then tap to select one or two more. Tap "Delete" icon.
    *   **Verification (UI):** All selected cards are removed from the list.
    *   **Verification (DB):** Corresponding rows are deleted.

4.  **Search and Sort Functionality (with DB data):**
    *   **Action:** In Card Management, use the search bar. Enter "Maison".
    *   **Verification (UI):** Only the "Maison" card (and any others matching) should appear.
    *   **Action:** Clear search. Try different sort options (Alphabetical, Recent, Difficulty).
    *   **Verification (UI):** The list should re-order correctly based on data fetched from the database.

## IV. Notifications Screen (Data Display):

1.  **Due Cards List:**
    *   **Setup:** Have a known set of cards that are due (based on `nextReviewDate` <= current time).
    *   **Action:** Navigate to the Notifications screen (e.g., via debug route).
    *   **Verification (UI):** The list of "Hypothetically Due Cards" should accurately reflect the cards currently due in the database.
    *   **Action:** Go to Flashcard Review, review some cards so they are no longer due.
    *   **Action:** Revisit Notifications screen.
    *   **Verification (UI):** The list of due cards should update accordingly (cards just reviewed should no longer be listed if their `nextReviewDate` is now in the future).

## V. Edge Cases & Error Handling (Conceptual):
    *   Attempt to create a card with empty required fields (UI should prevent, but good to note).
    *   (If possible) Test with a very large number of cards to see if UI performance in Card Management (scrolling, searching, sorting) is acceptable.
    *   Test operations if the database is somehow unavailable or corrupted (though this is harder to simulate manually without specific tools).

This plan provides a structured approach to manually verify that data operations are working as expected with the Room database.
