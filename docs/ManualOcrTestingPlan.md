# Manual OCR Functionality Testing Plan for AnkiZero

This document outlines manual testing scenarios to verify the complete Optical Character Recognition (OCR) flow in the AnkiZero application, from image capture/selection to saving a new flashcard. These tests should be performed on a physical device or emulator.

## Prerequisites:
- A debug build of the AnkiZero application with ML Kit Text Recognition integrated into the OCR flow.
- Access to the device camera and image gallery.
- Ability to inspect app data (e.g., through Card Management screen or direct database inspection via Android Studio's App Inspection) to verify card creation.
- A variety of test images (clear text, no text, different fonts/lighting if possible).

## I. Permission Handling:

1.  **Scenario: First Launch & Permission Denied**
    *   **Action:** Launch the AnkiZero app and navigate to the OCR screen for the first time (or after clearing app data/permissions).
    *   **Expected:** A system camera permission prompt should appear.
    *   **Action:** Deny the permission.
    *   **Expected:**
        *   The OCR screen displays a message indicating that camera permission is required.
        *   A button or option to "Grant Permission" (or similar) is visible.
        *   The camera preview area is not active (e.g., shows a placeholder or is blank).
        *   The "Capture Photo" button is disabled or, if tapped, re-prompts for permission or shows a message.
        *   The "Import from Gallery" button should still be functional (as it might not require camera permission, though it needs storage access which is usually granted differently or implicitly by the picker).

2.  **Scenario: Granting Permission via App's UI**
    *   **Action:** From the state in I.1 (permission denied), tap the "Grant Permission" button on the OCR screen.
    *   **Expected:** The system camera permission prompt reappears.
    *   **Action:** Grant the permission.
    *   **Expected:** The OCR screen updates, and the live camera preview becomes active. "Capture Photo" button becomes fully functional.

3.  **Scenario: Permission Already Granted**
    *   **Action:** Launch the OCR screen when camera permission has already been granted in a previous session.
    *   **Expected:** The camera preview is active immediately without any permission prompts.

## II. Image Capture via Camera:

1.  **Scenario: Successful Capture and Text Recognition**
    *   **Action:** On the OCR screen (with camera permission granted), point the camera at a piece of paper with clear, legible French text (e.g., "Bonjour le monde").
    *   **Action:** Tap the "Capture Photo" button.
    *   **Expected:**
        *   A brief visual indication of capture (if any, e.g., shutter sound, screen flash - app specific).
        *   A loading indicator (`LoadingIndicator`) appears while ML Kit processes the image.
        *   The `RecognizedTextDialog` appears, pre-filled in its `TextField` with the recognized text (e.g., "Bonjour le monde").
    *   **Action:** Edit the text in the dialog if necessary (e.g., correct a minor OCR error or split lines for French/English). For example, change to "Bonjour le monde - Hello world".
    *   **Action:** Tap the "Save" button in the dialog.
    *   **Expected:**
        *   The dialog closes.
        *   The OCR screen resets (e.g., `imageUri` and `detectedText` in ViewModel are cleared, ready for a new capture).
        *   Navigate to the Card Management screen. The newly created flashcard (e.g., French: "Bonjour le monde", English: "Hello world") should be visible in the list.
        *   **(DB Verification):** Optionally, inspect the database to confirm the card was saved with correct fields and default SRS parameters.

2.  **Scenario: Capture with No Recognizable Text**
    *   **Action:** Point the camera at a blank surface or an image without any text. Tap "Capture Photo".
    *   **Expected:**
        *   Loading indicator appears and then disappears.
        *   The `RecognizedTextDialog` appears. The `TextField` for recognized text is empty or contains a message like "No text found" (if ViewModel sets this for empty `visionText.text`).
        *   *(Alternative Expected based on current OcrViewModel.saveRecognizedText logic):* If text is empty, tapping "Save" in dialog should result in an error message "Cannot save empty text." and dialog might remain or close.
    *   **Action:** Tap "Cancel" in the dialog.
    *   **Expected:** Dialog closes, no card saved. OCR screen resets.

3.  **Scenario: Capture and Cancel Edit**
    *   **Action:** Capture an image with text. Dialog appears with recognized text.
    *   **Action:** Tap "Cancel" in the dialog.
    *   **Expected:** Dialog closes. No flashcard is created. OCR screen resets.

4.  **Scenario: Photo Capture Error (Hard to Simulate Manually)**
    *   **Action:** (If a situation can be forced where `CameraUtils.takePhoto` itself calls its `onError` callback - e.g., no storage space, camera hardware error).
    *   **Expected:** An `ErrorMessage` composable should appear on the `OcrScreen` indicating "Capture failed: [specific error message]". The loading indicator should hide. The user should be able to dismiss the error and try again.

## III. Image Import from Gallery:

1.  **Scenario: Successful Import and Text Recognition**
    *   **Action:** Tap the "Import from Gallery" button.
    *   **Expected:** The system image picker/gallery appears.
    *   **Action:** Select an image containing clear, legible French text.
    *   **Expected:**
        *   Loading indicator appears.
        *   `RecognizedTextDialog` appears with the recognized text.
    *   **Action:** Edit text if needed. Tap "Save".
    *   **Expected:** Card saved (verify in Card Management/DB). OCR screen resets.

2.  **Scenario: Import Image with No Recognizable Text**
    *   **Action:** Import an image with no text.
    *   **Expected:** Loading indicator. Dialog with empty text field or "No text found" message.
    *   **Action:** Tap "Cancel".
    *   **Expected:** No card saved. OCR screen resets.

3.  **Scenario: Import Unsupported File Type / Corrupted Image**
    *   **Action:** (If the system picker allows selecting non-image files or corrupted images that `InputImage.fromFilePath` cannot handle). Select such a file.
    *   **Expected:**
        *   Loading indicator may appear briefly.
        *   An `ErrorMessage` should be displayed on the `OcrScreen` (e.g., "Failed to load image: [specific error message]").
        *   The `RecognizedTextDialog` should not appear.

## IV. ML Kit Text Recognition Accuracy (Qualitative Assessment):

1.  **Scenario: Varied Text Inputs**
    *   **Action:** Use both Camera Capture and Gallery Import for a variety of images:
        *   Images with standard printed French text.
        *   Images with different (clear) font styles.
        *   Images taken in good vs. slightly lower lighting conditions.
        *   Images where text is slightly rotated or skewed.
        *   Text containing accents, diacritics, and common French punctuation.
    *   **Expected:**
        *   Observe the accuracy of the text populated in the `RecognizedTextDialog`.
        *   Note any patterns of errors (e.g., specific characters consistently misrecognized, issues with small text, problems with certain backgrounds).
        *   This is for qualitative feedback to understand ML Kit's performance with typical input. Perfect accuracy is not expected, but it should be reasonably good for clear inputs.

## V. UI States and General User Experience:

1.  **Scenario: Loading Indicator Visibility**
    *   **Action:** Initiate capture or import.
    *   **Expected:** The `LoadingIndicator` should be clearly visible and overlay the screen content, preventing further interaction until processing is complete. It should disappear once processing finishes (success or error).

2.  **Scenario: Error Message Display and Dismissal**
    *   **Action:** Trigger an error state (e.g., by trying to save empty text from the dialog, or if an image loading/processing error occurs).
    *   **Expected:** The `ErrorMessage` composable appears, displaying a relevant error.
    *   **Action:** If the error message has a dismiss option, tap it.
    *   **Expected:** The error message disappears. The user can continue interacting with the screen.

3.  **Scenario: Dialog Lifecycle**
    *   **Action:** Perform actions that show and hide the `RecognizedTextDialog`.
    *   **Expected:** The dialog appears when text is recognized and should hide when "Save" or "Cancel" is tapped, or when an action causes `viewModel.showEditDialog(false)` to be called.

4.  **Scenario: Screen Reset After Operation**
    *   **Action:** After successfully saving a card or canceling the dialog.
    *   **Expected:** The `OcrScreen` should be in a clean state, ready for a new capture or import. `imageUri` and `detectedText` in the ViewModel should be cleared, so the dialog doesn't reappear with old data if the user navigates away and back (unless that's desired behavior, which it currently isn't).

This manual testing plan should help ensure the OCR functionality is working correctly from end-to-end and that the UI provides a good user experience.
