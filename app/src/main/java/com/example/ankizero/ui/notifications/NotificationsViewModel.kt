package com.example.ankizero.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankizero.data.entity.Flashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

data class NotificationsUiState(
    val dueCards: List<Flashcard> = emptyList(),
    val dailyRemindersEnabled: Boolean = true,
    val reminderTime: String = "09:00 AM" // Static for now
)

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadPlaceholderDueCards()
        // In a real app, load preferences from DataStore/SharedPreferences
    }

    private fun loadPlaceholderDueCards() {
        viewModelScope.launch {
            // Simulate loading due cards
            val placeholderCards = List(5) { index ->
                Flashcard(
                    id = (100 + index).toLong(), // Avoid ID collision with other placeholders
                    frenchWord = "Due Card Example ${index + 1}",
                    englishTranslation = "Due English ${index + 1}",
                    creationDate = LocalDate.now().minusDays(20).toEpochSecond(ZoneOffset.UTC),
                    nextReviewDate = LocalDate.now().minusDays(1).toEpochSecond(ZoneOffset.UTC) // All due yesterday
                )
            }
            _uiState.update { it.copy(dueCards = placeholderCards) }
        }
    }

    fun toggleDailyReminders(isEnabled: Boolean) {
        _uiState.update { it.copy(dailyRemindersEnabled = isEnabled) }
        // In a real app, save this preference
        if (isEnabled) {
            // TODO: Schedule daily reminder worker
        } else {
            // TODO: Cancel daily reminder worker
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        // Format time properly, e.g., "09:00 AM"
        val formattedTime = String.format("%02d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, minute, if (hour < 12) "AM" else "PM")
        _uiState.update { it.copy(reminderTime = formattedTime) }
        // In a real app, save this preference and reschedule worker
    }

    // Call this if user wants to refresh the list of due cards shown on this screen
    fun refreshDueCards() {
        loadPlaceholderDueCards()
    }
}
