package com.example.ankizero.ui.management

import android.app.Application // Added
import androidx.lifecycle.AndroidViewModel // Changed from ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import com.example.ankizero.util.AnalyticsHelper // Added
// Re-import or define SortOption if it was removed from CardManagementScreen.kt
// For this refactor, assume SortOption is defined here or in a common place.
// enum class SortOption { Alphabetical, Recent, Difficulty } // Make sure this is the one used
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// This is the UiState CardManagementScreen.kt was designed for (or should be adapted to)
data class CardManagementUiState(
    val displayedCards: List<Flashcard> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.Recent, // Default sort
    val selectedCardIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val sortMenuExpanded: Boolean = false // If needed for UI, though often local state in screen
)

class CardManagementViewModel(
    application: Application, // Added
    private val repository: FlashcardRepository
) : AndroidViewModel(application) { // Changed to AndroidViewModel

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.Recent)
    private val _selectedCardIds = MutableStateFlow(emptySet<Long>())
    private val _isLoading = MutableStateFlow(true)
    private val _sortMenuExpanded = MutableStateFlow(false) // Example if UI needs it from VM

    // Master list from repository
    private val masterCardsFlow: Flow<List<Flashcard>> = repository.getAllCards()

    init {
        viewModelScope.launch {
            masterCardsFlow.take(1).collect { // Collect only the first emission
                _isLoading.value = false // Set loading to false once data is received
            }
        }
    }

    val uiState: StateFlow<CardManagementUiState> = combine(
        masterCardsFlow,
        _searchQuery,
        _sortOption,
        _selectedCardIds,
        _isLoading, // This is the StateFlow<Boolean> for loading state
        _sortMenuExpanded
    ) { cards: List<Flashcard>, query: String, sort: SortOption, selectedIds: Set<Long>, isLoadingValue: Boolean, sortMenuExpandedValue: Boolean ->
        // isLoadingValue is the current state of _isLoading.value
        // The side-effect _isLoading.value = false was removed from here.
        val filtered = if (query.isBlank()) {
            cards
        } else {
            cards.filter { flashcard -> // Explicitly name 'it' to 'flashcard' for clarity
                flashcard.frenchWord.contains(query, ignoreCase = true) ||
                flashcard.englishTranslation.contains(query, ignoreCase = true)
            }
        }
        val sortedList: List<Flashcard> = when (sort) {
            SortOption.Alphabetical -> filtered.sortedBy { flashcard -> flashcard.frenchWord }
            SortOption.Recent -> filtered.sortedByDescending { flashcard -> flashcard.creationDate }
            SortOption.Difficulty -> filtered.sortedBy { flashcard -> flashcard.difficulty ?: 3 } // Handle null difficulty
        }
        CardManagementUiState(
            displayedCards = sortedList,
            searchQuery = query,
            sortOption = sort,
            selectedCardIds = selectedIds,
            isLoading = isLoadingValue, // Use the value from the _isLoading flow
            sortMenuExpanded = sortMenuExpandedValue
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CardManagementUiState() // Initial state with isLoading = true
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) { // Log only if search term is not blank
            AnalyticsHelper.logSearchPerformed(getApplication(), query)
        }
    }

    fun updateSortOrder(sortOption: SortOption) {
        _sortOption.value = sortOption
        AnalyticsHelper.logSortChanged(getApplication(), sortOption.name)
        _sortMenuExpanded.value = false // Close menu on selection
    }

    fun toggleCardSelection(cardId: Long) {
        val currentSelected = _selectedCardIds.value.toMutableSet()
        if (currentSelected.contains(cardId)) {
            currentSelected.remove(cardId)
        } else {
            currentSelected.add(cardId)
        }
        _selectedCardIds.value = currentSelected
    }

    fun clearSelections() {
        _selectedCardIds.value = emptySet()
    }

    fun deleteSelectedCards() {
        viewModelScope.launch {
            val idsToDelete = _selectedCardIds.value.toList()
            if (idsToDelete.isNotEmpty()) {
                repository.deleteCards(idsToDelete) // Use new DAO method
                AnalyticsHelper.logCardDeleted(getApplication(), idsToDelete.size) // Added analytics
                _selectedCardIds.value = emptySet() // Clear selection
                // Flow will automatically update displayedCards
            }
        }
    }

    // Methods for creating/updating cards (called from Edit/Create screens eventually)
    // These were in the previous version of the ViewModel, so keeping similar logic
    fun updateCard(card: Flashcard, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(card) // Use correct repository method
            AnalyticsHelper.logCardUpdated(getApplication(), card.id) // Added analytics
            // Flow will update list, onComplete might be for UI navigation
            onComplete()
        }
    }

    fun createCard(card: Flashcard, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val newId = repository.insert(card) // Use correct repository method
            AnalyticsHelper.logNewCardSaved(getApplication(), "manual_creation") // Pass context and source
            // If newId is indeed the actual card ID, could use:
            // AnalyticsHelper.logCardCreated(getApplication(), newId)
            // Flow will update list, onComplete might be for UI navigation
            onComplete()
        }
    }

    // If sort menu expansion is controlled by screen, this can be removed
    fun setSortMenuExpanded(expanded: Boolean) {
        _sortMenuExpanded.value = expanded
    }
}
// Ensure SortOption enum is defined/accessible. If it was in CardManagementScreen.kt,
// it should be moved to a common location or duplicated in the ViewModel's package/file
// for clarity if not shared. For this overwrite, I'm assuming it's accessible.
// Example:
enum class SortOption { Alphabetical, Recent, Difficulty }
