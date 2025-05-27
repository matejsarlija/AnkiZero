package com.example.ankizero.ui.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardManagementViewModel(private val repository: FlashcardRepository) : ViewModel() {
    data class UiState(
        val cards: List<Flashcard> = emptyList(),
        val selected: Set<Long> = emptySet(),
        val sortMenuExpanded: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var allCards: List<Flashcard> = emptyList()

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            allCards = repository.getAllFlashcards()
            _uiState.value = _uiState.value.copy(cards = allCards)
        }
    }

    fun search(query: String) {
        val filtered = allCards.filter { it.french.contains(query, ignoreCase = true) }
        _uiState.value = _uiState.value.copy(cards = filtered)
    }

    fun sort(option: SortOption) {
        val sorted = when (option) {
            SortOption.ALPHABETICAL -> allCards.sortedBy { it.french }
            SortOption.RECENT -> allCards.sortedByDescending { it.id }
            SortOption.DIFFICULTY -> allCards.sortedBy { it.easeFactor }
        }
        _uiState.value = _uiState.value.copy(cards = sorted)
    }

    fun setSortMenuExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(sortMenuExpanded = expanded)
    }

    fun toggleSelect(id: Long) {
        val selected = _uiState.value.selected.toMutableSet()
        if (selected.contains(id)) selected.remove(id) else selected.add(id)
        _uiState.value = _uiState.value.copy(selected = selected)
    }

    fun deleteSelected() {
        val toDelete = _uiState.value.selected
        viewModelScope.launch {
            repository.deleteFlashcards(toDelete.toList())
            loadCards()
            _uiState.value = _uiState.value.copy(selected = emptySet())
        }
    }

    fun updateCard(card: Flashcard, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateFlashcard(card)
            loadCards()
            onComplete()
        }
    }

    fun createCard(card: Flashcard, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertFlashcard(card)
            loadCards()
            onComplete()
        }
    }
}
