package com.example.ankizero.ui.notifications

import android.app.Application
import androidx.datastore.core.DataStore // Added DataStore import
import androidx.datastore.preferences.core.Preferences // Added Preferences import
import androidx.datastore.preferences.core.edit // Added edit import
import androidx.datastore.preferences.preferencesDataStore // Added preferencesDataStore import
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankizero.data.NotificationPreferences // Added NotificationPreferences import
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository // Added import
import androidx.work.* // Added WorkManager imports
import com.example.ankizero.util.AnalyticsHelper // Added
import com.example.ankizero.util.AppLogger
import com.example.ankizero.util.workers.ReviewReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // Added import
import java.util.Calendar
import java.util.Locale // Added import
import java.util.concurrent.TimeUnit
import android.content.Context // Added Context import

// DataStore is defined in AnkiZeroApplication.kt

data class NotificationsUiState(
    val dueCards: List<Flashcard> = emptyList(),
    val dailyRemindersEnabled: Boolean = true,
    val reminderTime: String = "09:00" // Storing as HH:mm for easier parsing
)

class NotificationsViewModel(
    application: Application,
    private val repository: FlashcardRepository,
    private val dataStore: DataStore<Preferences> // Injected DataStore
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    private val TAG = "NotificationsViewModel"
    companion object {
        private const val REMINDER_WORK_NAME = "com.example.ankizero.ReviewReminderWorker"
    }

    // Preferences - these would ideally be loaded from DataStore/SharedPreferences
    private val _dailyRemindersEnabled = MutableStateFlow(true)
    private val _reminderTime = MutableStateFlow("09:00") // Store as HH:mm e.g. "09:00", "14:30"

    private val dueCardsFlow: Flow<List<Flashcard>> = repository.getDueCards()

    val uiState: StateFlow<NotificationsUiState> = combine(
        dueCardsFlow,
        _dailyRemindersEnabled,
        _reminderTime
    ) { cards, remindersEnabled, time ->
        NotificationsUiState(
            dueCards = cards,
            dailyRemindersEnabled = remindersEnabled,
            // Format time for display if needed, or keep as HH:mm and format in UI
            reminderTime = formatDisplayTime(time)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationsUiState(reminderTime = formatDisplayTime(_reminderTime.value))
    )

    init {
        loadSettings()
        // Example: if reminders were enabled on app start, schedule it
        // This will be handled by the collector of _dailyRemindersEnabled
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _dailyRemindersEnabled.value = preferences[NotificationPreferences.DAILY_REMINDERS_ENABLED] ?: true
                val hour = preferences[NotificationPreferences.REMINDER_TIME_HOUR] ?: 9
                val minute = preferences[NotificationPreferences.REMINDER_TIME_MINUTE] ?: 0
                _reminderTime.value = String.format(Locale.US, "%02d:%02d", hour, minute)

                // Reschedule if reminders are enabled with the loaded time
                if (_dailyRemindersEnabled.value) {
                    scheduleDailyReminder()
                }
            }
        }
    }

    private fun formatDisplayTime(timeStr: String): String {
        // Parses "HH:mm" and formats to "hh:mm AM/PM"
        return try {
            val parts = timeStr.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error formatting display time", e)
            "09:00 AM" // Default fallback
        }
    }


    fun toggleDailyReminders(isEnabled: Boolean) {
        _dailyRemindersEnabled.value = isEnabled
        saveDailyRemindersPreference(isEnabled) // Save preference
        AnalyticsHelper.logNotificationPreferenceChanged(getApplication(), isEnabled, _reminderTime.value) // Added
        if (isEnabled) {
            scheduleDailyReminder()
        } else {
            cancelDailyReminder()
        }
    }

    private fun saveDailyRemindersPreference(isEnabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[NotificationPreferences.DAILY_REMINDERS_ENABLED] = isEnabled
            }
        }
    }

    // Expects time in "HH:mm" format e.g. "14:30"
    fun updateReminderTime(newTime: String) {
        try {
            // Validate time format if necessary "HH:mm"
            val parts = newTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            if (hour !in 0..23 || minute !in 0..59) {
                throw IllegalArgumentException("Invalid time format")
            }
            val oldTime = _reminderTime.value
            _reminderTime.value = newTime
            saveReminderTimePreference(hour, minute) // Save preference
            AnalyticsHelper.logNotificationPreferenceChanged(getApplication(), _dailyRemindersEnabled.value, newTime) // Added
            if (_dailyRemindersEnabled.value && oldTime != newTime) { // Reschedule only if time actually changed
                AppLogger.i(TAG, "Reminder time updated to $newTime, rescheduling worker.")
                scheduleDailyReminder() // Reschedule with new time
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Invalid time format for updateReminderTime: $newTime", e)
            // Optionally, expose an error to UI
        }
    }

    private fun saveReminderTimePreference(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[NotificationPreferences.REMINDER_TIME_HOUR] = hour
                preferences[NotificationPreferences.REMINDER_TIME_MINUTE] = minute
            }
        }
    }

    private fun scheduleDailyReminder() {
        val (hour, minute) = try {
            val parts = _reminderTime.value.split(":")
            parts[0].toInt() to parts[1].toInt()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Invalid reminder time format: ${_reminderTime.value}, defaulting to 9 AM.", e)
            9 to 0 // Default to 9 AM if parsing fails
        }

        val currentTime = Calendar.getInstance()
        val dueTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (dueTime.before(currentTime)) {
            dueTime.add(Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelay = dueTime.timeInMillis - currentTime.timeInMillis

        val constraints = Constraints.Builder()
            // .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Default
            // .setRequiresCharging(false) // Default
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<ReviewReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Replace to update initialDelay if time changes
            reminderRequest
        )
        AppLogger.i(TAG, "Daily reminder worker scheduled. Initial delay: ${initialDelay / (1000 * 60)} minutes.")
    }

    private fun cancelDailyReminder() {
        workManager.cancelUniqueWork(REMINDER_WORK_NAME)
        AppLogger.i(TAG, "Daily reminder worker cancelled.")
    }

    fun refreshDueCards() {
        // The list updates automatically due to the Flow.
        // If manual refresh logic is needed (e.g., re-query with different date),
        // repository methods would need to support that, or trigger a re-collection.
        // For now, this method might not be strictly necessary if using repository.getDueCards()
        // which calculates current date on each call (if not memoized by Flow).
        // If getDueCards took a date, we could trigger it here.
        // For simplicity, we assume the Flow from getDueCards is sufficient.
        // If not, one might need to trigger a re-fetch if the underlying data source itself isn't pushing updates.
        // However, Room Flows typically do push updates.
    }
}
