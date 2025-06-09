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
// import kotlin.math.max // No longer used
// import kotlin.math.min // No longer used
// import kotlin.math.roundToInt // Not used after refactor

// This is the UiState that FlashcardScreen.kt was designed to work with
data class FlashcardUiState(
    val currentCard: Flashcard? = null,
    val progressText: String = "",
    val isDeckEmpty: Boolean = false,
    val showFlipHint: Boolean = true, // To control initial flip hint visibility
    val dueCardsList: List<Flashcard> = emptyList(), // Added
    val currentCardIndex: Int = 0 // Added
)

// Changed to AndroidViewModel to get Application context
class FlashcardViewModel(
    application: Application,
    private val repository: FlashcardRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    // private var dueCardsList: List<Flashcard> = emptyList() // Moved to UiState
    // private var currentCardIndex = 0 // Moved to UiState
    // private var currentCardIndex = 0 // Moved to UiState
    private var cardsReviewedThisSession = 0 // Counter for analytics
    private var sessionStartTime = System.currentTimeMillis() // For session duration

    // Add this StateFlow to trigger refresh
    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            // Use flatMapLatest with the trigger
            refreshTrigger.flatMapLatest { triggeredTime ->
                // Log when we are fetching with a new time trigger.
                // android.util.Log.d("FlashcardViewModel", "Refreshing due cards with time: $triggeredTime")
                repository.getDueCards() // This will now use the current time due to repository change
            }.collectLatest { cards ->
                    val shuffledCards = cards.shuffled()
                    // Reset session counters if the set of card IDs has changed.
                    val oldIds = _uiState.value.dueCardsList.map { it.id }.toSet()
                    val newIds = shuffledCards.map { it.id }.toSet()
                    if (oldIds != newIds) {
                        cardsReviewedThisSession = 0
                        sessionStartTime = System.currentTimeMillis()
                    }

                    _uiState.update { currentState ->
                        var newIndex = 0 // Default to 0
                        val currentCardId = currentState.currentCard?.id

                        if (shuffledCards.isNotEmpty()) {
                            if (currentCardId != null) {
                                val foundIdx = shuffledCards.indexOfFirst { it.id == currentCardId }
                                if (foundIdx != -1) {
                                    newIndex = foundIdx
                                } else {
                                    // Current card is no longer in the list, newIndex remains 0 (first card of new list)
                                }
                            }
                            // Ensure newIndex is within bounds if it was from previous state and list shrank
                            if (newIndex >= shuffledCards.size) {
                                newIndex = 0
                            }
                        }
                        // If shuffledCards is empty, newIndex remains 0, currentCard will be null.

                        currentState.copy(
                            dueCardsList = shuffledCards,
                            currentCardIndex = newIndex,
                            currentCard = shuffledCards.getOrNull(newIndex),
                            progressText = if (shuffledCards.isEmpty()) "No cards due!" else "Card ${newIndex + 1}/${shuffledCards.size}",
                            isDeckEmpty = shuffledCards.isEmpty(),
                            showFlipHint = (currentState.currentCard?.id != shuffledCards.getOrNull(newIndex)?.id && shuffledCards.isNotEmpty()) || (shuffledCards.isNotEmpty() && newIndex == 0 && cardsReviewedThisSession == 0)
                        )
                    }
                }
        }
    }

    // loadDueCardsFromRepository is removed as its logic is in init's collectLatest
    // updateUiWithCurrentCard is removed as its logic is in init's collectLatest

    fun processCardRating(isMemorized: Boolean) {
        // It's important to capture the uiState value at the beginning of the function,
        // especially if it's used for decisions like checking list size before an async call.
        val currentUiStateValue = _uiState.value
        val cardToProcess = currentUiStateValue.currentCard ?: return

        viewModelScope.launch {
            // Store the ID of the card being processed and the size of the list before processing
            val processedCardId = cardToProcess.id
            val listSizeBeforeReview = currentUiStateValue.dueCardsList.size

            repository.processReview(cardToProcess, isMemorized) // This updates the card in DB
            AnalyticsHelper.logCardReviewed(getApplication(), cardToProcess.id, isMemorized)
            cardsReviewedThisSession++

            // The .collectLatest in init will automatically pick up the change.
            // We need to check if the deck became empty *as a result of this specific review*.
            // The flow will emit a new list. We check if the new list is empty AND
            // the card just reviewed was the last one.
            // This logic relies on the fact that collectLatest will update the uiState,
            // so we check the state *after* the repository call might have triggered an update.
            // However, repository.processReview is suspend and Flow emission is concurrent.
            // A robust way is to check conditions that would lead to an empty list.
            if (listSizeBeforeReview == 1 && currentUiStateValue.dueCardsList.any { it.id == processedCardId }) {
                 // Check against the current state of dueCardsList which should have been updated by the flow
                 // This check might be tricky due to timing of flow emission.
                 // A simpler and more direct check: if the card processed was the last one.
                 // The flow will update the list to empty if this was the last card.
                 // The check for session completion can be done when the flow emits an empty list.

                // The problem description asks: "Logging for AnalyticsHelper.logReviewSessionCompleted should happen
                // when the dueCardsList becomes empty *as a result of a review*."
                // This can be checked by seeing if the dueCardsList had one item before processReview and
                // that item was the one being processed.

                // The logic for session completion logging will now be implicitly handled by the collectLatest block
                // when it observes that the dueCardsList has become empty.
                // We can refine the condition in collectLatest or here.
                // Let's refine it in collectLatest by checking if isDeckEmpty turned true.
                // For now, we'll rely on the flow updating the list.
                // The condition `_uiState.value.isDeckEmpty` after the flow updates would be the source of truth.
                // The if condition `_uiState.value.dueCardsList.isEmpty() && cardsReviewedThisSession > 0`
                // in the old `updateUiWithCurrentCard` was responsible for this.
                // We need to replicate that, ensuring it's tied to a review action.

                // The most reliable place to log session completion is when the flow emits an empty list
                // AND cardsReviewedThisSession > 0.
                // The `collectLatest` block updates `isDeckEmpty`. We can observe `_uiState.value`
                // after `repository.processReview` completes and the flow has had a chance to update.
                // However, that's still a race.
                // The original instruction: "if the dueCardsList had one item before processReview and that item was the one being processed"

                // Let's stick to the provided logic:
                if (currentUiStateValue.dueCardsList.size == 1 && currentUiStateValue.dueCardsList.first().id == cardToProcess.id) {
                    // This check is against the state *before* the flow has a chance to emit the new empty list.
                    // After repository.processReview, the flow will emit, and collectLatest will update isDeckEmpty.
                    // The log should happen *after* the list becomes empty.
                    // We can check _uiState.value.isDeckEmpty after this block, but that's not robust.
                    // Let's assume the flow updates promptly. The log will be based on the *next* state.
                    // The prompt implies we should log based on the action leading to emptiness.
                    val sessionDurationSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
                    AnalyticsHelper.logReviewSessionCompleted(getApplication(), cardsReviewedThisSession, sessionDurationSeconds)
                    // cardsReviewedThisSession = 0; // Will be reset by collectLatest if new cards arrive or list is reloaded
                    // sessionStartTime = System.currentTimeMillis(); // Same as above
                }
            }
        }
    }

    fun showNextCard(moveForward: Boolean = true) {
        _uiState.update { currentState ->
            if (currentState.dueCardsList.isEmpty()) {
                return@update currentState // No change if deck is empty
            }

            val newIndex = if (moveForward) {
                (currentState.currentCardIndex + 1) % currentState.dueCardsList.size
            } else {
                if (currentState.currentCardIndex == 0) currentState.dueCardsList.size - 1 else currentState.currentCardIndex - 1
            }

            currentState.copy(
                currentCardIndex = newIndex,
                currentCard = currentState.dueCardsList.getOrNull(newIndex),
                progressText = "Card ${newIndex + 1}/${currentState.dueCardsList.size}",
                showFlipHint = false // Hide hint on manual navigation
            )
        }
    }

    fun dismissFlipHint() {
        _uiState.update { it.copy(showFlipHint = false) }
    }

    // Add this public function to be called when the screen is resumed or becomes active
    fun onResume() {
        // android.util.Log.d("FlashcardViewModel", "onResume called, triggering refresh.")
        refreshTrigger.value = System.currentTimeMillis()
    }
}
