package com.example.ankizero.ui.management

import com.example.ankizero.data.entity.Flashcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

@ExperimentalCoroutinesApi
class CardManagementViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // Test data
    private val testFlashcards = listOf(
        Flashcard(id = 1, frenchWord = "Bonjour", englishTranslation = "Hello", creationDate = LocalDate.now().minusDays(2).toEpochSecond(ZoneOffset.UTC), difficulty = 3, nextReviewDate = 0),
        Flashcard(id = 2, frenchWord = "Au revoir", englishTranslation = "Goodbye", creationDate = LocalDate.now().minusDays(1).toEpochSecond(ZoneOffset.UTC), difficulty = 1, nextReviewDate = 0),
        Flashcard(id = 3, frenchWord = "Merci", englishTranslation = "Thank you", creationDate = LocalDate.now().toEpochSecond(ZoneOffset.UTC), difficulty = 5, nextReviewDate = 0),
        Flashcard(id = 4, frenchWord = "Oui", englishTranslation = "Yes", creationDate = LocalDate.now().minusDays(3).toEpochSecond(ZoneOffset.UTC), difficulty = 2, nextReviewDate = 0),
        Flashcard(id = 5, frenchWord = "Non", englishTranslation = "No", creationDate = LocalDate.now().minusDays(4).toEpochSecond(ZoneOffset.UTC), difficulty = 4, nextReviewDate = 0)
    )

    // Helper to create ViewModel and override its initial data for tests
    // This is not possible with current ViewModel which loads data in init block from its own source.
    // These tests will rely on the default placeholder data in CardManagementViewModel.
    // For more robust tests, the ViewModel should allow injecting a list of cards or a repository.

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads cards with default sort (Recent)`() = runTest {
        val viewModel = CardManagementViewModel() // Uses its own placeholder data
        val uiState = viewModel.uiState.first { !it.isLoading }

        assertFalse(uiState.displayedCards.isEmpty())
        // Default sort is Recent. Check if the first card is the most recent one from placeholder.
        // Placeholder data in CardManagementViewModel:
        // id = index.toLong(), creationDate = LocalDate.now().minusDays(index.toLong() % 7)
        // Card with index 0 or 7 or 14 (id 0, 7, 14) would be most recent (creationDate = today)
        // Card with index 1 or 8 or 15 (id 1, 8, 15) would be created yesterday
        val firstDisplayedCardCreationDate = uiState.displayedCards.first().creationDate
        val secondDisplayedCardCreationDate = uiState.displayedCards.getOrNull(1)?.creationDate

        if(secondDisplayedCardCreationDate != null) {
            assertTrue("First card should be more recent or equal to the second.", firstDisplayedCardCreationDate >= secondDisplayedCardCreationDate)
        }
        assertEquals(SortOption.Recent, uiState.sortOption) // Default sort option
    }

    @Test
    fun `updateSearchQuery filters cards correctly (french word)`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading } // Ensure initial load

        viewModel.updateSearchQuery("Mot Français 1") // Specific to placeholder data structure
        val uiState = viewModel.uiState.value

        assertTrue(uiState.displayedCards.isNotEmpty())
        uiState.displayedCards.forEach {
            assertTrue(it.frenchWord.contains("Mot Français 1", ignoreCase = true))
        }
        // Check a card that contains "Mot Français 1" (e.g. "Mot Français 10", "Mot Français 11", ...)
        val card10 = uiState.displayedCards.find { it.id == 9L } // id is index
        val card1 = uiState.displayedCards.find { it.id == 0L }
        assertNotNull(card1)
        assertNotNull(card10)

    }

    @Test
    fun `updateSearchQuery filters cards correctly (english word, case insensitive)`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading } // Ensure initial load

        viewModel.updateSearchQuery("word 2") // "English Word 2"
        val uiState = viewModel.uiState.value

        assertTrue(uiState.displayedCards.isNotEmpty())
        assertEquals(1, uiState.displayedCards.size) // "English Word 2" (id=1)
        assertEquals(1L, uiState.displayedCards.first().id)
    }

    @Test
    fun `updateSearchQuery with blank query shows all cards`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading } // Ensure initial load

        viewModel.updateSearchQuery("Filter") // Apply some filter
        assertNotEquals(viewModel.uiState.value.displayedCards.size, 20) // Assuming default 20 master cards

        viewModel.updateSearchQuery("") // Clear filter
        assertEquals(20, viewModel.uiState.value.displayedCards.size) // All cards shown
    }

    @Test
    fun `updateSortOrder sorts Alphabetical correctly`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.updateSortOrder(SortOption.Alphabetical)
        val cards = viewModel.uiState.value.displayedCards

        assertTrue(cards.isNotEmpty())
        for (i in 0 until cards.size - 1) {
            assertTrue(cards[i].frenchWord <= cards[i+1].frenchWord)
        }
        assertEquals(SortOption.Alphabetical, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `updateSortOrder sorts Recent correctly`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.updateSortOrder(SortOption.Recent) // Default, but test explicitly
        val cards = viewModel.uiState.value.displayedCards

        assertTrue(cards.isNotEmpty())
        for (i in 0 until cards.size - 1) {
            assertTrue(cards[i].creationDate >= cards[i+1].creationDate)
        }
        assertEquals(SortOption.Recent, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `updateSortOrder sorts Difficulty correctly`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.updateSortOrder(SortOption.Difficulty)
        val cards = viewModel.uiState.value.displayedCards

        assertTrue(cards.isNotEmpty())
        for (i in 0 until cards.size - 1) {
            // Placeholder difficulty: (index % 5) + 1. Lower is more difficult if not inverted.
            // ViewModel sorts by `it.difficulty ?: 3`. Lower value = higher priority.
            assertTrue((cards[i].difficulty ?: 3) <= (cards[i+1].difficulty ?: 3))
        }
        assertEquals(SortOption.Difficulty, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `toggleCardSelection updates selectedCardIds`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading }

        val cardIdToSelect = viewModel.uiState.value.displayedCards.first().id

        viewModel.toggleCardSelection(cardIdToSelect)
        assertTrue(viewModel.uiState.value.selectedCardIds.contains(cardIdToSelect))

        viewModel.toggleCardSelection(cardIdToSelect) // Toggle off
        assertFalse(viewModel.uiState.value.selectedCardIds.contains(cardIdToSelect))
    }

    @Test
    fun `clearSelections clears selectedCardIds`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading }
        val cardIdToSelect = viewModel.uiState.value.displayedCards.first().id

        viewModel.toggleCardSelection(cardIdToSelect)
        assertTrue(viewModel.uiState.value.selectedCardIds.isNotEmpty())

        viewModel.clearSelections()
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
    }

    @Test
    fun `deleteSelectedCards removes cards and clears selection`() = runTest {
        val viewModel = CardManagementViewModel()
        viewModel.uiState.first { !it.isLoading } // initial load

        val initialCardCount = viewModel.uiState.value.displayedCards.size
        assertTrue(initialCardCount > 0)

        val cardToSelect1 = viewModel.uiState.value.displayedCards[0].id
        val cardToSelect2 = viewModel.uiState.value.displayedCards[1].id

        viewModel.toggleCardSelection(cardToSelect1)
        viewModel.toggleCardSelection(cardToSelect2)
        assertEquals(2, viewModel.uiState.value.selectedCardIds.size)

        viewModel.deleteSelectedCards()

        assertEquals(initialCardCount - 2, viewModel.uiState.value.displayedCards.size)
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
        assertNull(viewModel.uiState.value.displayedCards.find { it.id == cardToSelect1 })
        assertNull(viewModel.uiState.value.displayedCards.find { it.id == cardToSelect2 })
    }
}
