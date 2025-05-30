package com.example.ankizero.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application // Required for AndroidViewModel to get context
import androidx.lifecycle.AndroidViewModel // Changed from ViewModel
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import com.example.ankizero.util.AnalyticsHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// This is the UiState that FlashcardScreen.kt was designed to work with
data class FlashcardUiState(
    val currentCard: Flashcard? = null,
    val progressText: String = "",
    val isDeckEmpty: Boolean = false,
    val showFlipHint: Boolean = true // To control initial flip hint visibility
)

// Changed to AndroidViewModel to get Application context
class FlashcardViewModel(
    application: Application,
    private val repository: FlashcardRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private var dueCardsList: List<Flashcard> = emptyList()
    private var currentCardIndex = 0
    private var cardsReviewedThisSession = 0 // Counter for analytics
    private var sessionStartTime = System.currentTimeMillis() // For session duration

    init {
        loadDueCardsFromRepository()
    }

    private fun loadDueCardsFromRepository() {
        viewModelScope.launch {
            repository.getDueCards() // Assumes getDueCards() correctly fetches for 'today'
                .collectLatest { cards ->
                    dueCardsList = cards.shuffled() // Shuffle for variety
                    cardsReviewedThisSession = 0 // Reset counter for new batch of cards
                    sessionStartTime = System.currentTimeMillis() // Reset session start time
                    updateUiWithCurrentCard()
                }
        }
    }

    private fun updateUiWithCurrentCard() {
        if (dueCardsList.isEmpty()) {
            if (cardsReviewedThisSession > 0) { // Log session completed only if cards were reviewed
                val sessionDurationSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
                AnalyticsHelper.logReviewSessionCompleted(getApplication(), cardsReviewedThisSession, sessionDurationSeconds)
            }
            _uiState.update {
                it.copy(
                    currentCard = null,
                    progressText = "No cards due!",
                    isDeckEmpty = true,
                    showFlipHint = false
                )
            }
        } else {
            // Ensure index is within bounds, reset if necessary (e.g., after deletion/rating all)
            if (currentCardIndex !in dueCardsList.indices && dueCardsList.isNotEmpty()) {
                currentCardIndex = 0 // Or handle completion
            }
            _uiState.update {
                it.copy(
                    currentCard = dueCardsList.getOrNull(currentCardIndex),
                    progressText = "Card ${currentCardIndex + 1}/${dueCardsList.size}",
                    isDeckEmpty = false,
                    showFlipHint = it.currentCard == null && dueCardsList.isNotEmpty()
                )
            }
        }
    }


    fun processCardRating(isMemorized: Boolean) {
        val cardToProcess = uiState.value.currentCard ?: return

        viewModelScope.launch {
            val updatedCard = repository.processReview(cardToProcess, isMemorized)
            AnalyticsHelper.logCardReviewed(getApplication(), updatedCard.id, isMemorized)
            cardsReviewedThisSession++ // Increment reviewed cards counter

            dueCardsList = dueCardsList.filterNot { it.id == updatedCard.id }
            if (currentCardIndex >= dueCardsList.size && dueCardsList.isNotEmpty()) {
                 currentCardIndex = dueCardsList.size - 1
            }

            updateUiWithCurrentCard() // This will handle empty list and log session completion
        }
    }

    fun showNextCard(moveForward: Boolean = true) {
        if (dueCardsList.isEmpty()) {
            updateUiWithCurrentCard() // Update to empty state
            return
        }

        if (moveForward) {
            currentCardIndex = (currentCardIndex + 1) % dueCardsList.size
        } else { // Moving backward
            currentCardIndex = if (currentCardIndex == 0) dueCardsList.size - 1 else currentCardIndex - 1
        }
        // When navigating, don't show flip hint unless it's a new session logic
        _uiState.update { it.copy(showFlipHint = false) }
        updateUiWithCurrentCard()
    }

    fun dismissFlipHint() {
        _uiState.update { it.copy(showFlipHint = false) }
    }
}
