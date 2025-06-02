# Project TODO List

This document outlines potential fixes, suggestions, and areas for further development based on a recent code review.

## General / Cross-Cutting Concerns

*   **Further Development:**
    *   [ ] **Dependency Injection:** Consistently use a DI framework (like Hilt) to provide ViewModels, Repositories, and other dependencies instead of manual instantiation in Composable default parameters or previews.
    *   [ ] **String Resources:** Ensure all user-facing strings (labels, messages, errors, placeholders) are extracted to `strings.xml` for localization. (Partially done, but needs a full pass).
    *   [ ] **UI Testing:** Continue expanding UI tests based on the TODO lists found in some screen files. Use test tags consistently.
    *   [ ] **Error Handling:** Standardize how user-facing errors are presented (e.g., Snackbars via a shared mechanism, dedicated error sections in UI state). `GlobalErrorHandler` usage in `OcrViewModel` is a good start for logging.

---

## `CardManagementViewModel.kt` & `CardManagementScreen.kt`

*   **Suggestions/Further Development (ViewModel):**
    *   [ ] Consider refactoring the `combine` operator if a version with typed parameters (avoiding `Array<*>`) is available and compatible, to improve type safety and reduce boilerplate casting.
    *   [ ] Move `SortOption` enum to a more shared location if it's used by both ViewModel and Screen directly (e.g., alongside `CardManagementUiState` or in `ui.management.model`).
*   **Suggestions/Further Development (Screen):**
    *   [ ] (Covered by General) Refine ViewModel/Repository instantiation for production use, favoring DI.

---

## `FlashcardViewModel.kt` & `FlashcardScreen.kt`

*   **Potential Fixes/Refinements (ViewModel):**
    *   [ ] Review `showFlipHint` logic in `updateUiWithCurrentCard` for clarity. Consider more explicit control.
*   **Suggestions/Further Development (ViewModel):**
    *   [ ] Consider if the imperative management of `dueCardsList` and `currentCardIndex` could be made more declarative/Flow-idiomatic.
*   **Suggestions/Further Development (Screen):**
    *   [ ] Minor polish for swipe gesture: animate `dragOffsetX` back to 0 on `onDragEnd` for smoother visual reset.

---

## `NotificationsViewModel.kt` & `NotificationsScreen.kt`

*   **CRITICAL FIXES (ViewModel & Screen):**
    *   [ ] **Persist Notification Settings:** Implement saving and loading for `_dailyRemindersEnabled` and `_reminderTime` using DataStore or SharedPreferences.
*   **CRITICAL FIXES (Screen):**
    *   [ ] **Implement Time Picker:** The "Reminder Time" `TextButton` should open a time picker dialog.
    *   [ ] **Implement "Review Now" Action:** The "Review Due Cards Now" button should navigate to `FlashcardScreen` or trigger the review session.
*   **Suggestions/Further Development (ViewModel & Screen):**
    *   [ ] Clarify user expectation for "Hypothetically Due Cards" or investigate more dynamic ways to update this list if needed.

---

## `OcrViewModel.kt` & `OcrScreen.kt`

*   **Suggestions/Further Development (ViewModel):**
    *   [ ] **Improve Text Parsing for Save:** Enhance how OCR'd text is assigned to French/English fields post-detection.
    *   [ ] **Image Pre-processing:** Implement image downscaling before sending to ML Kit (as noted in ViewModel TODOs).
    *   [ ] **Image Rotation:** Thoroughly test and ensure image rotation is handled correctly for ML Kit input (as noted in ViewModel TODOs).
*   **Suggestions/Further Development (Screen):**
    *   [ ] **Actual Bounding Box Interaction:** If live preview OCR interaction is desired, replace simulated bounding boxes with actual ML Kit preview detection. Otherwise, simplify/remove simulation.
    *   [ ] **CameraPreviewView Executor:** Revisit `Executors.newSingleThreadExecutor()` creation in `CameraPreviewView` for better resource management.
    *   [ ] **Image Preview in Dialog:** Display the captured/selected image in `RecognizedTextDialog`.
    *   [ ] **Error Message Display:** Consider using Snackbars for `ErrorMessage` from shared components.

---

## `CreateCardScreen.kt`

*   **CRITICAL FIXES:**
    *   [ ] **ViewModel Integration:** Integrate with `CardManagementViewModel.createCard()` (or a dedicated ViewModel) to save the card.
*   **Suggestions/Further Development:**
    *   [ ] (Covered by General) Use string resources for all labels and error messages.
    *   [ ] Consider using `SnackbarHostState` for feedback on save success/failure.

---

## `EditCardScreen.kt`

*   **CRITICAL FIXES:**
    *   [ ] **ViewModel Integration (Load):** Load the card to be edited using its ID via `CardManagementViewModel` (or a dedicated ViewModel).
    *   [ ] **ViewModel Integration (Save):** Integrate with `CardManagementViewModel.updateCard()` to save changes.
*   **Potential Fixes/Clarifications:**
    *   [ ] **Missing Editable Fields:** Verify which fields of the `Flashcard` entity (e.g., `exampleSentence`) are intended to be editable and ensure they are present on the form.
*   **Suggestions/Further Development:**
    *   [ ] (Covered by General) Use string resources for all labels and error messages.
    *   [ ] Consider using `SnackbarHostState` for feedback on update success/failure.

---
