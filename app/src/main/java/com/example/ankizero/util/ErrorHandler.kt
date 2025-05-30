package com.example.ankizero.util

import android.util.Log

object GlobalErrorHandler {

    private const val TAG = "GlobalError"

    /**
     * Reports an error. In a production app, this would typically send
     * non-fatal exceptions to a crash reporting service like Firebase Crashlytics.
     *
     * @param throwable The throwable associated with the error.
     * @param message An optional custom message to provide more context.
     */
    fun reportError(throwable: Throwable, message: String? = null) {
        val errorMessage = message ?: "An error occurred"
        Log.e(TAG, "$errorMessage: ${throwable.message}", throwable)

        // TODO: Integrate with Firebase Crashlytics or other reporting services
        // Example for Crashlytics:
        // FirebaseCrashlytics.getInstance().recordException(throwable)
        // if (message != null) {
        //     FirebaseCrashlytics.getInstance().log(message)
        // }
    }

    /**
     * Logs a specific handled error/exception with a custom message.
     * This might be used for errors that are caught and handled gracefully
     * but still worth noting for analytics or debugging.
     */
    fun logHandledException(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
        // Optionally log to analytics as a non-fatal event
        // AnalyticsHelper.logError("handled_exception", message, throwable?.javaClass?.simpleName)
    }
}
