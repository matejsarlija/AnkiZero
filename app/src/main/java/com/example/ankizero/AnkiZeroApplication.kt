package com.example.ankizero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
// import android.util.Log // Replaced by AppLogger
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository
import com.example.ankizero.util.AppLogger // Using AppLogger
import com.example.ankizero.util.workers.STUDY_REMINDERS_CHANNEL_ID
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import androidx.datastore.core.DataStore // Added DataStore import
import androidx.datastore.preferences.core.Preferences // Added Preferences import
import androidx.datastore.preferences.preferencesDataStore // Added preferencesDataStore import


// Define DataStore at the top level, tied to the Application context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AnkiZeroApplication : Application() {

    // Lazily initialize database and repository so they are created only when needed
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: FlashcardRepository by lazy { FlashcardRepository(database.flashCardDao()) }
    // DataStore is already defined at the top-level using Context.dataStore extension

    lateinit var firebaseAnalytics: FirebaseAnalytics
        private set // Make it accessible read-only if needed, or keep private

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase Analytics
        firebaseAnalytics = Firebase.analytics
        AppLogger.i("AnkiZeroApplication", "Firebase Analytics initialized.")

        // Initialize Firebase Crashlytics
        // Disable in debug builds, enable in release builds (or based on preference)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        AppLogger.i("AnkiZeroApplication", "Crashlytics collection enabled: ${!BuildConfig.DEBUG}")

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name) // Ensure these are in strings.xml
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(STUDY_REMINDERS_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            AppLogger.i("AnkiZeroApplication", "Notification channel '${STUDY_REMINDERS_CHANNEL_ID}' created.")
        } else {
            AppLogger.d("AnkiZeroApplication", "Notification channel creation not needed for API < 26.")
        }
    }
}
