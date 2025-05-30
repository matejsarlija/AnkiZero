package com.example.ankizero.util

// import android.util.Log // Replaced by AppLogger

object AnalyticsHelper {

    private const val TAG = "AppAnalytics"

    fun logCardReviewed(cardId: Long, memorized: Boolean) {
        // In a real implementation, this would log to Firebase Analytics or another provider.
        // Example Firebase Analytics event:
        // Firebase.analytics.logEvent("card_reviewed") {
        //     param("card_id", cardId.toString())
        //     param("memorized", memorized.toString())
        // }
        AppLogger.d(TAG, "Card Reviewed: ID=$cardId, Memorized: $memorized")
    }

    fun logCardCreated(cardId: Long) { // Assuming cardId is available after creation
        // Firebase.analytics.logEvent("card_created") {
        //     param("card_id", cardId.toString())
        // }
        AppLogger.d(TAG, "Card Created: ID=$cardId")
    }

    fun logNewCardSaved() { // If cardId is not immediately available or for general save action
        // Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) { ... } // Or custom event
        AppLogger.d(TAG, "New Card Saved (creation screen)")
    }

    fun logCardUpdated() {
        AppLogger.d(TAG, "Card Updated (edit screen)")
    }

    fun logOcrUsed() {
        // Firebase.analytics.logEvent("ocr_feature_used", null)
        AppLogger.d(TAG, "OCR Feature Used")
    }

    fun logScreenView(screenName: String) {
        // Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        //     param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        //     param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName) // Or actual class name
        // }
        AppLogger.d(TAG, "Screen Viewed: $screenName")
    }

    // Add more specific events as needed:
    // e.g., logDeckImported, logDeckExported, logNotificationPreferenceChanged, etc.
}
