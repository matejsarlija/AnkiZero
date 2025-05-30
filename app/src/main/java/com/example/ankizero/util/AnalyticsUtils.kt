package com.example.ankizero.util

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {

    private const val TAG = "AppAnalytics" // For AppLogger

    // Get FirebaseAnalytics instance.
    // It's a singleton, so getting it multiple times is fine, or get from Application class.
    private fun getAnalytics(): FirebaseAnalytics {
        return Firebase.analytics
    }

    /**
     * Logs a custom event to Firebase Analytics.
     * @param eventName Name of the event.
     * @param params Bundle of parameters for the event.
     */
    fun logEvent(eventName: String, params: Bundle = bundleOf()) {
        // AppLogger.d(TAG, "Logging Analytics Event: $eventName, Params: $params")
        // Uncomment above for local logging of analytics events if needed during debug.
        // Actual Firebase logging should ideally happen for all builds,
        // or be conditional based on BuildConfig.FLAVOR or similar, not just BuildConfig.DEBUG.
        getAnalytics().logEvent(eventName, params)
    }

    // Specific event logging functions

    fun logCardReviewed(context: Context, cardId: Long, memorized: Boolean) {
        // The context parameter is kept for consistency if some analytics events might need it,
        // but Firebase.analytics does not directly require it for logEvent.
        logEvent("card_reviewed", bundleOf(
            "card_id" to cardId, // Firebase recommends Long for IDs if they are numeric
            "memorized" to memorized.toString() // Booleans are often logged as strings or 0/1
        ))
        AppLogger.d(TAG, "Analytics: Card Reviewed: ID=$cardId, Memorized: $memorized") // Local log
    }

    fun logNewCardSaved(context: Context, source: String) {
        logEvent(FirebaseAnalytics.Event.GENERATE_LEAD, bundleOf( // Using a standard event for "lead" generation
            FirebaseAnalytics.Param.SOURCE to source // e.g., "ocr", "manual_creation"
        ))
        // Or a custom event:
        // logEvent("new_card_saved", bundleOf("source" to source))
        AppLogger.d(TAG, "Analytics: New Card Saved, Source: $source")
    }

    fun logCardUpdated(context: Context, cardId: Long) {
        logEvent("card_updated", bundleOf("card_id" to cardId))
        AppLogger.d(TAG, "Analytics: Card Updated, ID: $cardId")
    }

    fun logOcrUsed(context: Context) {
        logEvent("ocr_feature_used")
        AppLogger.d(TAG, "Analytics: OCR Feature Used")
    }

    fun logScreenView(context: Context, screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName,
            FirebaseAnalytics.Param.SCREEN_CLASS to screenName // Often Activity name or Composable route/name
        ))
        AppLogger.d(TAG, "Analytics: Screen Viewed: $screenName")
    }

    fun logCardDeleted(context: Context, count: Int) {
        logEvent("card_deleted", bundleOf("delete_count" to count))
        AppLogger.d(TAG, "Analytics: Cards Deleted, Count: $count")
    }

    fun logSearchPerformed(context: Context, searchTerm: String) {
        if (searchTerm.isNotBlank()) { // Only log non-empty searches
            logEvent(FirebaseAnalytics.Event.SEARCH, bundleOf(FirebaseAnalytics.Param.SEARCH_TERM to searchTerm))
            AppLogger.d(TAG, "Analytics: Search Performed, Term: $searchTerm")
        }
    }

    fun logSortChanged(context: Context, sortOption: String) {
        logEvent("sort_changed", bundleOf("sort_option" to sortOption))
        AppLogger.d(TAG, "Analytics: Sort Changed, Option: $sortOption")
    }

    fun logNotificationPreferenceChanged(context: Context, enabled: Boolean, time: String?) {
        logEvent("notification_preference_changed", bundleOf(
            "reminders_enabled" to enabled.toString(),
            "reminder_time" to (time ?: "not_set")
        ))
        AppLogger.d(TAG, "Analytics: Notification Preference Changed, Enabled: $enabled, Time: $time")
    }

    fun logReviewSessionCompleted(context: Context, cardsReviewed: Int, sessionDurationSeconds: Long) {
        if (cardsReviewed > 0) {
            logEvent("review_session_completed", bundleOf(
                "cards_reviewed" to cardsReviewed,
                "session_duration_seconds" to sessionDurationSeconds
            ))
            AppLogger.d(TAG, "Analytics: Review Session Completed, Cards: $cardsReviewed, Duration: $sessionDurationSeconds s")
        }
    }
}
