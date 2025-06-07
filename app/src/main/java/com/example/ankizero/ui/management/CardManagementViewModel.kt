package com.example.ankizero.ui.management

import android.app.Application // Added
import androidx.lifecycle.AndroidViewModel // Changed from ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import com.example.ankizero.util.AnalyticsHelper // Added
import com.example.ankizero.R // Added for string resources
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

// Define CreateCardFormState data class
data class CreateCardFormState(
    val frenchWord: String = "",
    val englishTranslation: String = "",
    val exampleSentence: String = "",
    val notes: String = "",
    val difficulty: Float = 0.5f, // Default difficulty
    val frenchWordError: String? = null,
    val englishTranslationError: String? = null
)

// Define EditCardFormState data class
data class EditCardFormState(
    val id: Long? = null,
    val frenchWord: String = "",
    val englishTranslation: String = "",
    // val exampleSentence: String = "", // Not specified in requirements for EditCardFormState but present in CreateCardFormState. Assuming not needed unless clarified.
    val notes: String = "",
    val difficulty: Float = 0.5f, // Default difficulty
    val frenchWordError: String? = null,
    val englishTranslationError: String? = null,
    val isLoading: Boolean = true, // To indicate if card data is being loaded
    val cardNotFound: Boolean = false // To indicate if the card couldn't be loaded
)

class CardManagementViewModel(
    application: Application, // Added
    private val repository: FlashcardRepository
) : AndroidViewModel(application) { // Changed to AndroidViewModel

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.Recent)
    private val _selectedCardIds = MutableStateFlow(emptySet<Long>())
    private val _isLoading = MutableStateFlow(true) // General loading for card list
    private val _sortMenuExpanded = MutableStateFlow(false) // Example if UI needs it from VM

    // CreateCardFormState management
    private val _createCardFormState = MutableStateFlow(CreateCardFormState())
    val createCardFormState: StateFlow<CreateCardFormState> = _createCardFormState.asStateFlow()

    // EditCardFormState management
    private val _editCardFormState = MutableStateFlow(EditCardFormState())
    val editCardFormState: StateFlow<EditCardFormState> = _editCardFormState.asStateFlow()

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
    ) { values: Array<*> ->
        val cards = values[0] as List<Flashcard>
        val query = values[1] as String
        val sort = values[2] as SortOption
        val selectedIds = values[3] as Set<Long>
        val isLoadingValue = values[4] as Boolean
        val sortMenuExpandedValue = values[5] as Boolean

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

    // Functions to update CreateCardFormState
    fun updateNewFrenchWord(word: String) {
        _createCardFormState.update { it.copy(frenchWord = word, frenchWordError = null) }
    }

    fun updateNewEnglishTranslation(translation: String) {
        _createCardFormState.update { it.copy(englishTranslation = translation, englishTranslationError = null) }
    }

    fun updateNewExampleSentence(sentence: String) {
        _createCardFormState.update { it.copy(exampleSentence = sentence) }
    }

    fun updateNewNotes(notes: String) {
        _createCardFormState.update { it.copy(notes = notes) }
    }

    fun updateNewDifficulty(difficulty: Float) {
        _createCardFormState.update { it.copy(difficulty = difficulty) }
    }

    private fun validateNewCardForm(): Boolean {
        val currentState = _createCardFormState.value
        var isValid = true
        if (currentState.frenchWord.isBlank()) {
            _createCardFormState.update { it.copy(frenchWordError = getApplication<Application>().getString(R.string.french_word_empty_error)) }
            isValid = false
        }
        if (currentState.englishTranslation.isBlank()) {
            _createCardFormState.update { it.copy(englishTranslationError = getApplication<Application>().getString(R.string.english_translation_empty_error)) }
            isValid = false
        }
        return isValid
    }

    fun saveNewCard(onSuccess: () -> Unit) {
        if (validateNewCardForm()) {
            val formState = _createCardFormState.value
            val newCard = Flashcard(
                frenchWord = formState.frenchWord,
                englishTranslation = formState.englishTranslation,
                exampleSentence = formState.exampleSentence,
                notes = formState.notes,
                difficulty = formState.difficulty.toInt(), // Assuming difficulty is stored as Int in Flashcard
                creationDate = System.currentTimeMillis() // Or your preferred way to set creation date
            )
            createCard(newCard, onSuccess) // createCard already handles repo interaction and analytics
            resetCreateCardFormState()
        }
    }

    fun resetCreateCardFormState() {
        _createCardFormState.value = CreateCardFormState()
    }

    // Functions for EditCardFormState
    fun loadCardForEditing(cardId: Long) {
        _editCardFormState.update { it.copy(isLoading = true, cardNotFound = false) }
        viewModelScope.launch {
            // Try to find the card in the current uiState's displayedCards or masterCardsFlow
            // Using masterCardsFlow ensures we get the most up-to-date card list.
            val card = masterCardsFlow.firstOrNull()?.find { it.id == cardId }

            if (card != null) {
                _editCardFormState.update {
                    it.copy(
                        id = card.id,
                        frenchWord = card.frenchWord,
                        englishTranslation = card.englishTranslation,
                        // exampleSentence = card.exampleSentence ?: "", // Uncomment if exampleSentence is added to EditCardFormState
                        notes = card.notes ?: "",
                        difficulty = (card.difficulty ?: 3) - 1f, // Assuming difficulty 1-5 maps to 0f-4f
                        frenchWordError = null,
                        englishTranslationError = null,
                        isLoading = false
                    )
                }
            } else {
                // Card not found
                _editCardFormState.update { it.copy(isLoading = false, cardNotFound = true) }
                // Log error or handle as appropriate
                // For now, cardNotFound flag is set.
            }
        }
    }

    fun updateEditFrenchWord(word: String) {
        _editCardFormState.update { it.copy(frenchWord = word, frenchWordError = null) }
    }

    fun updateEditEnglishTranslation(translation: String) {
        _editCardFormState.update { it.copy(englishTranslation = translation, englishTranslationError = null) }
    }

    // fun updateEditExampleSentence(sentence: String) { // Uncomment if exampleSentence is added
    // _editCardFormState.update { it.copy(exampleSentence = sentence) }
    // }

    fun updateEditNotes(notes: String) {
        _editCardFormState.update { it.copy(notes = notes) }
    }

    fun updateEditDifficulty(difficulty: Float) {
        _editCardFormState.update { it.copy(difficulty = difficulty) }
    }

    private fun validateEditCardForm(): Boolean {
        val currentState = _editCardFormState.value
        var isValid = true
        if (currentState.frenchWord.isBlank()) {
            _editCardFormState.update { it.copy(frenchWordError = getApplication<Application>().getString(R.string.french_word_empty_error)) }
            isValid = false
        }
        if (currentState.englishTranslation.isBlank()) {
            _editCardFormState.update { it.copy(englishTranslationError = getApplication<Application>().getString(R.string.english_translation_empty_error)) }
            isValid = false
        }
        return isValid
    }

    fun saveEditedCard(onSuccess: () -> Unit) {
        if (validateEditCardForm()) {
            val formState = _editCardFormState.value
            if (formState.id != null) {
                // Retrieve the original card to maintain other properties not directly edited in this form
                viewModelScope.launch {
                    val originalCard = masterCardsFlow.firstOrNull()?.find { it.id == formState.id }
                    if (originalCard != null) {
                        val updatedCard = originalCard.copy(
                            frenchWord = formState.frenchWord.trim(),
                            englishTranslation = formState.englishTranslation.trim(),
                            // exampleSentence = formState.exampleSentence.trim().ifEmpty { null }, // Uncomment if exampleSentence is part of EditCardFormState
                            notes = formState.notes.trim().ifEmpty { null },
                            difficulty = formState.difficulty.toInt() + 1 // Convert 0f-4f slider to 1-5
                        )
                        updateCard(updatedCard, onSuccess) // updateCard handles repo interaction and analytics
                        resetEditCardFormState()
                    } else {
                        // Handle case where original card is not found, though unlikely if ID is present
                        _editCardFormState.update { it.copy(cardNotFound = true) }
                    }
                }
            }
        }
    }

    fun resetEditCardFormState() {
        _editCardFormState.value = EditCardFormState()
    }

}
// Ensure SortOption enum is defined/accessible. If it was in CardManagementScreen.kt,
// it should be moved to a common location or duplicated in the ViewModel's package/file
// for clarity if not shared. For this overwrite, I'm assuming it's accessible.
// Example:
enum class SortOption { Alphabetical, Recent, Difficulty }
