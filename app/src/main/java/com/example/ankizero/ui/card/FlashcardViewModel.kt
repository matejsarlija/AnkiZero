package com.example.ankizero.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.ankizero.util.AnalyticsHelper // Added
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {
    data class UiState(
        val currentCard: Flashcard? = null,
        val progress: Int = 0,
        val total: Int = 0,
        val flipped: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var dueCards: List<Flashcard> = emptyList()
    private var currentIndex = 0

    init {
        loadDueCards()
    }

    private fun loadDueCards() {
        viewModelScope.launch {
            dueCards = repository.getDueFlashcards()
            _uiState.value = _uiState.value.copy(
                currentCard = dueCards.getOrNull(0),
                progress = 0,
                total = dueCards.size,
                flipped = false
            )
        }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(flipped = !_uiState.value.flipped)
    }

    fun nextCard() {
        if (currentIndex < dueCards.size - 1) {
            currentIndex++
            _uiState.value = _uiState.value.copy(
                currentCard = dueCards.getOrNull(currentIndex),
                progress = currentIndex,
                flipped = false
            )
        }
    }

    fun prevCard() {
        if (currentIndex > 0) {
            currentIndex--
            _uiState.value = _uiState.value.copy(
                currentCard = dueCards.getOrNull(currentIndex),
                progress = currentIndex,
                flipped = false
            )
        }
    }

    fun rateMemorized() {
        val card = dueCards.getOrNull(currentIndex) ?: return
        val newInterval = (card.interval * 1.8).toInt().coerceAtLeast(1)
        val newEase = (card.easeFactor + 0.1).coerceAtMost(2.5)
        val nextDue = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, newInterval) }.time
        viewModelScope.launch {
            repository.updateFlashcard(
                card.copy(
                    interval = newInterval,
                    easeFactor = newEase,
                    due = nextDue
                )
            )
            loadDueCards()
        }
    }

    fun rateNo() {
        val card = dueCards.getOrNull(currentIndex) ?: return
        val newEase = (card.easeFactor - 0.2).coerceAtLeast(1.3)
        val nextDue = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        viewModelScope.launch {
            repository.updateFlashcard(
                card.copy(
                    interval = 1,
                    easeFactor = newEase,
                    due = nextDue
                )
            )
            loadDueCards()
        }
    }
}
