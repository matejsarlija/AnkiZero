# Firebase Services Verification Plan

This document outlines how to verify that Firebase Analytics and Crashlytics are correctly integrated and reporting data to the Firebase console.

## Prerequisites
1.  **Correct `google-services.json`:** Ensure the correct `google-services.json` file, downloaded from your target Firebase project, has been placed in the `app/` directory of the AnkiZero Android module.
2.  **Build and Run:** The app must be built and run on a physical device or an emulator *after* the correct `google-services.json` file is in place.
3.  **Crashlytics Collection (for Crashlytics verification):**
    *   In `AnkiZeroApplication.kt`, Crashlytics collection is set by `FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)`.
    *   This means for testing crashes and seeing them in the console, you should ideally use a **release build** or temporarily change `!BuildConfig.DEBUG` to `true` for a debug build.
    *   Alternatively, after a crash in a debug build (where collection might be off by default in Firebase settings for debug), you can force send cached reports on next launch (see Firebase docs for this, though our code enables it for release).
4.  **Internet Connectivity:** The device/emulator must have internet access to send data to Firebase.

## Verifying Firebase Analytics

1.  **Enable DebugView (Recommended for Real-time Testing):**
    *   On your test device/emulator, enable Analytics debug mode by executing the following adb command (replace `com.example.ankizero` if your package name is different):
        ```shell
        adb shell setprop debug.firebase.analytics.app com.example.ankizero
        ```
    *   To disable DebugView later:
        ```shell
        adb shell setprop debug.firebase.analytics.app .none.
        ```
2.  **Open Firebase Console:** Navigate to your Firebase project (console.firebase.google.com).
3.  **Go to Analytics -> DebugView:**
    *   Select your test device from the dropdown in DebugView.
    *   You should see events appearing here in near real-time (usually within seconds) as you interact with the app.
4.  **Perform Key Actions in App:**
    *   Navigate between main screens (Flashcards, Card Management, OCR). Expect `screen_view` events.
    *   Review a few flashcards (mark "Memorized" and "No"). Expect `card_reviewed` events with `card_id` and `memorized` parameters.
    *   Use the OCR feature to "recognize" text from an image. Expect an `ocr_used` event.
    *   Save recognized text from OCR as a new card. Expect a `new_card_saved` event (or `generate_lead`) with `source="ocr"`.
    *   Manually create a new card from the Card Management screen. Expect a `new_card_saved` event with `source="manual_creation"`.
    *   Edit an existing card. Expect a `card_updated` event with `card_id`.
    *   Delete one or more cards. Expect a `card_deleted` event with `delete_count`.
    *   Perform a search in Card Management. Expect a `search` event with `search_term`.
    *   Change the sort order in Card Management. Expect a `sort_changed` event with `sort_option`.
    *   Toggle notification preferences or change reminder time (if UI allows). Expect `notification_preference_changed` events.
    *   Complete a review session (review all due cards). Expect a `review_session_completed` event with `cards_reviewed` and `session_duration_seconds`.
5.  **Observe Events in DebugView:**
    *   Verify that the corresponding events appear in DebugView.
    *   Click on an event to see its parameters and confirm they are correct (e.g., `screen_name`, `card_id`, `memorized`, `search_term`, `source`).
6.  **Check Standard Analytics Dashboards:**
    *   After some time (typically a few hours, up to 24 hours for full processing), check the main Analytics dashboards (e.g., "Events", "Audiences", "User properties" if set).
    *   Verify that your logged events are contributing to the aggregated data.

## Verifying Firebase Crashlytics

1.  **Ensure Crashlytics is Enabled for the Build:**
    *   As noted in Prerequisites, Crashlytics data collection is enabled for non-DEBUG builds by the current code (`!BuildConfig.DEBUG`). If testing with a debug build, you might need to temporarily change this to `true` in `AnkiZeroApplication.kt` or use a release build/variant.
2.  **Induce a Test Crash:**
    *   Open the AnkiZero app on a device/emulator.
    *   Navigate to where the "Test Crash (DEBUG ONLY)" button is located (currently in `MainActivity`'s top area in Debug builds).
    *   Tap the "Test Crash" button. The app should crash.
3.  **Re-open the App:**
    *   **This is a critical step.** Crashlytics typically sends pending crash reports on the next launch of the app after a crash.
    *   Close the crashed app (if it didn't fully close) and then re-open it. Allow it a moment to initialize and send the report.
4.  **Open Firebase Console:** Navigate to your Firebase project.
5.  **Go to Crashlytics Dashboard:** From the left navigation pane, select "Crashlytics" under the "Release & Monitor" section.
6.  **Check for New Issues:**
    *   Look for a new crash issue. The dashboard should list "RuntimeException: Test Crash from AnkiZero App via Button".
    *   It might take a few minutes (sometimes up to 5-10 minutes) for the first crash report to appear in the console. Be patient and refresh the page.
7.  **Inspect Crash Details:**
    *   Click on the new issue.
    *   **Stack Trace:** Verify the stack trace points to the line where the `RuntimeException` was thrown in `MainActivity.kt`.
    *   **Logs:** Check the "Logs" tab within the crash details. You should see the custom log message added by `GlobalErrorHandler.reportError()` (e.g., "Test Crash from AnkiZero App via Button: Test Crash from AnkiZero App via Button"). You should also see the log from `AppLogger.w("TestCrash", ...)` if that executed before the crash.
    *   **Device Information:** Check device details, OS version, app version, etc.
    *   **Keys (if set):** If you were setting custom keys with Crashlytics, verify them here.

## Troubleshooting Common Issues

*   **No Data in Analytics/Crashlytics:**
    *   **`google-services.json`:** Double-check that the correct `google-services.json` file is present in the `app/` directory and that it's correctly parsed (no build errors related to it). Ensure the `package_name` in the JSON matches your app's `applicationId`.
    *   **Internet Connectivity:** Ensure the device/emulator has an active internet connection.
    *   **Gradle Plugins:** Verify that `com.google.gms.google-services` and `com.google.firebase.crashlytics` Gradle plugins are correctly applied in your `build.gradle.kts` files (project and module level).
    *   **Dependencies:** Confirm Firebase dependencies (`firebase-bom`, `firebase-analytics-ktx`, `firebase-crashlytics-ktx`) are correctly added in `app/build.gradle.kts`.
    *   **Logcat:** Check Android Studio's Logcat for any error messages from Firebase or Crashlytics during app startup, event logging, or when a crash occurs. Filter by tags like "FirebaseAnalytics", "Crashlytics", "FirebaseInit", "FA", "FA-SVC".
    *   **Crashlytics Collection Enabled:** For Crashlytics, re-verify that `setCrashlyticsCollectionEnabled` is set to `true` for the build you are testing. If testing a debug build, ensure it's not being disabled.
    *   **Firebase Project Settings:** In the Firebase console, ensure Analytics and Crashlytics are enabled for your project and that there are no setup issues indicated there.
    *   **Data Processing Time:** Especially for Analytics (outside of DebugView), allow sufficient time for data to be processed and appear in the dashboards (can be several hours). Crashlytics is usually faster once a report is uploaded.
    *   **Emulator Issues:** If using an emulator, ensure it has Google Play Services installed and updated if required by certain Firebase features (though Analytics and Crashlytics usually work fine on most emulators).
```
