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
import kotlin.random.Random

// Removed: import kotlin.collections.shuffled - this is redundant
private const val MINIMUM_CARDS_FOR_REVIEW = 2
// import kotlin.math.max // No longer used
// import kotlin.math.min // No longer used
// import kotlin.math.roundToInt // Not used after refactor

enum class ReviewMode {
    NONE, // No review active, or no cards due initially
    NORMAL, // Reviewing normally due cards
    MANUAL  // Reviewing all cards manually
}

// This is the UiState that FlashcardScreen.kt was designed to work with
data class FlashcardUiState(
    val currentCard: Flashcard? = null,
    val progressText: String = "",
    val isDeckEmpty: Boolean = false, // True if no cards for the current mode, or no cards at all
    val showFlipHint: Boolean = true, // To control initial flip hint visibility
    val dueCardsList: List<Flashcard> = emptyList(), // Added
    val currentCardIndex: Int = 0, // Added
    val reviewMode: ReviewMode = ReviewMode.NONE // Added review mode
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
                // When refresh is triggered, we default to loading due cards (normal review)
                repository.getDueCards()
            }.collectLatest { cards ->
                val shuffledCards = cards.shuffled() // Fixed: Use consistent shuffled() call
                // Reset session counters if the set of card IDs has changed.
                // This should happen when the mode or the actual list of cards changes significantly.
                val oldIds = _uiState.value.dueCardsList.map { it.id }.toSet()
                val newIds = shuffledCards.map { it.id }.toSet()
                if (oldIds != newIds) {
                    cardsReviewedThisSession = 0
                    sessionStartTime = System.currentTimeMillis()
                }

                if (shuffledCards.size < MINIMUM_CARDS_FOR_REVIEW && shuffledCards.isNotEmpty()) { // Added isNotEmpty check
                    _uiState.update { currentState ->
                        currentState.copy(
                            currentCard = null,
                            progressText = "Add at least $MINIMUM_CARDS_FOR_REVIEW cards to start a review.",
                            isDeckEmpty = true,
                            reviewMode = ReviewMode.NONE,
                            dueCardsList = emptyList(), // Clear due cards list
                            currentCardIndex = 0 // Reset index
                        )
                    }
                } else {
                    _uiState.update { currentState ->
                        // Determine reviewMode based on whether cards were found by getDueCards
                        val newReviewMode = if (shuffledCards.isNotEmpty()) ReviewMode.NORMAL else ReviewMode.NONE

                        var newIndex = 0 // Default to 0
                    val currentCardId = currentState.currentCard?.id

                    if (shuffledCards.isNotEmpty()) {
                        if (currentCardId != null && currentState.reviewMode == newReviewMode) { // Preserve index if mode is same
                            val foundIdx = shuffledCards.indexOfFirst { it.id == currentCardId }
                            if (foundIdx != -1) {
                                newIndex = foundIdx
                            }
                        }
                        if (newIndex >= shuffledCards.size) { // Ensure index is valid
                            newIndex = 0
                        }
                    }

                    // Reset session counters if mode changes or card set changes significantly
                    if (currentState.reviewMode != newReviewMode || oldIds != shuffledCards.map{it.id}.toSet()) {
                        cardsReviewedThisSession = 0
                        sessionStartTime = System.currentTimeMillis()
                    }

                        currentState.copy(
                            dueCardsList = shuffledCards,
                            currentCardIndex = newIndex,
                            currentCard = shuffledCards.getOrNull(newIndex),
                            progressText = if (shuffledCards.isEmpty()) "No cards due!" else "Card ${newIndex + 1}/${shuffledCards.size}",
                            isDeckEmpty = shuffledCards.isEmpty(),
                            reviewMode = newReviewMode,
                            showFlipHint = (currentState.currentCard?.id != shuffledCards.getOrNull(newIndex)?.id && shuffledCards.isNotEmpty()) || (shuffledCards.isNotEmpty() && newIndex == 0 && cardsReviewedThisSession == 0 && newReviewMode != ReviewMode.NONE)
                        )
                    }
                }
            }
        }
    }

    fun startManualReview() {
        viewModelScope.launch {
            // Fixed: Proper null handling and consistent shuffling
            val allCardsList: List<Flashcard>? = repository.getAllCards().firstOrNull()
            val cards = allCardsList?.shuffled() ?: emptyList() // Removed unnecessary Random.Default parameter

            if (cards.size < MINIMUM_CARDS_FOR_REVIEW) {
                _uiState.update {
                    it.copy(
                        currentCard = null,
                        progressText = "Not enough cards in your deck to start a review. Add at least $MINIMUM_CARDS_FOR_REVIEW cards.",
                        isDeckEmpty = true,
                        reviewMode = ReviewMode.NONE,
                        dueCardsList = emptyList(),
                        currentCardIndex = 0
                    )
                }
            } else {
                cardsReviewedThisSession = 0
                sessionStartTime = System.currentTimeMillis()

                _uiState.update {
                    it.copy(
                        dueCardsList = cards,
                        currentCardIndex = 0,
                        currentCard = cards.getOrNull(0),
                        progressText = if (cards.isEmpty()) "No cards in deck." else "Card 1/${cards.size}",
                        isDeckEmpty = cards.isEmpty(),
                        reviewMode = ReviewMode.MANUAL,
                        showFlipHint = cards.isNotEmpty() // Show hint if manual review starts with cards
                    )
                }
            }
        }
    }

    // loadDueCardsFromRepository is removed as its logic is in init's collectLatest
    // updateUiWithCurrentCard is removed as its logic is in init's collectLatest

    fun processCardRating(isMemorized: Boolean) {
        val cardToProcess = _uiState.value.currentCard ?: return // Get card from current state

        viewModelScope.launch {
            repository.processReview(cardToProcess, isMemorized)
            AnalyticsHelper.logCardReviewed(getApplication(), cardToProcess.id, isMemorized)
            cardsReviewedThisSession++

            // Optimistically update the UI by removing the card and advancing
            _uiState.update { currentState ->
                // It's crucial to use currentState.dueCardsList here, NOT _uiState.value.dueCardsList
                // to avoid race conditions if the state was updated by another coroutine/flow
                // between reading cardToProcess and this update block.
                val listWithoutProcessedCard = currentState.dueCardsList.filterNot { it.id == cardToProcess.id }

                if (listWithoutProcessedCard.isEmpty()) {
                    // This was the last card
                    if (cardsReviewedThisSession > 0) { // Check if any card was reviewed to log session
                        val sessionDurationSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
                        AnalyticsHelper.logReviewSessionCompleted(getApplication(), cardsReviewedThisSession, sessionDurationSeconds)
                        // Reset session counters now that the session is complete.
                        // cardsReviewedThisSession = 0 // This will be reset if a new list loads via collectLatest
                        // sessionStartTime = System.currentTimeMillis() // Same as above
                    }
                    currentState.copy(
                        currentCard = null,
                        dueCardsList = emptyList(),
                        currentCardIndex = 0,
                        progressText = "Review complete!",
                        isDeckEmpty = true,
                        showFlipHint = false
                    )
                } else {
                    // Determine the new index.
                    // If currentCardIndex from `currentState` was pointing to a position within the bounds of the new shorter list,
                    // it means the items before it were kept, and the item at that index is the correct next card.
                    // If currentCardIndex is now out of bounds (e.g., it was the last item that was removed),
                    // or if it pointed past the end of the new list size, wrap to 0.
                    val newIndex = if (currentState.currentCardIndex < listWithoutProcessedCard.size) {
                        currentState.currentCardIndex
                    } else {
                        0 // Loop to the beginning if the end was reached or index is out of bounds
                    }
                    currentState.copy(
                        dueCardsList = listWithoutProcessedCard,
                        currentCard = listWithoutProcessedCard.getOrNull(newIndex),
                        currentCardIndex = newIndex,
                        progressText = "Card ${newIndex + 1}/${listWithoutProcessedCard.size}",
                        showFlipHint = true // Show hint for the new card
                    )
                }
            }
            // The collectLatest in init will eventually get an update from repository.getDueCards().
            // This new list should ideally match `listWithoutProcessedCard` or be a superset if new cards became due.
            // If there's a discrepancy (e.g., another card became due simultaneously),
            // collectLatest will handle it and become the source of truth for dueCardsList.
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
        // Only trigger refresh if not in manual mode, or perhaps always to reset to due cards?
        // Current behavior: onResume re-fetches due cards, effectively ending manual review.
        // This might be desired. If not, add a condition: if (_uiState.value.reviewMode != ReviewMode.MANUAL)
        refreshTrigger.value = System.currentTimeMillis()
    }
}