package com.example.ankizero.ui.management

import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any // Added for org.mockito.kotlin.any
import org.mockito.Mock // Added for @Mock
import kotlin.test.*
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.system.measureTimeMillis
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import android.app.Application
// import org.mockito.Mockito.`when` // Removed, use org.mockito.kotlin.whenever
import org.mockito.ArgumentMatchers // Added for anyInt and captor
// import org.mockito.ArgumentMatchers.anyInt // Removed
// import org.mockito.ArgumentMatchers.any // Removed, use org.mockito.kotlin.any
import org.mockito.kotlin.eq // for eq()


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CardManagementViewModelTest {

    @Captor private lateinit var flashcardCaptor: ArgumentCaptor<Flashcard>

    @Mock
    private lateinit var mockApplication: Application // Changed to @Mock
    @Mock
    private lateinit var mockRepository: FlashcardRepository // Changed to @Mock

    private val testDispatcher = UnconfinedTestDispatcher() // Or StandardTestDispatcher for more control

    private lateinit var viewModel: CardManagementViewModel
    // private lateinit var mockRepository: FlashcardRepository // Now a @Mock field
    private lateinit var initialMockCards: List<Flashcard>
    private lateinit var mockCardsFlow: MutableStateFlow<List<Flashcard>>


    // Helper to create Flashcard instances for tests
    private fun createTestFlashcard(
        id: Long,
        frenchWord: String = "French $id",
        englishTranslation: String = "English $id",
        creationDate: Long = System.currentTimeMillis() - (id * 1000 * 60 * 60 * 24), // Stagger creation dates
        difficulty: Int? = ((id % 5) + 1).toInt(),
        nextReviewDate: Long = System.currentTimeMillis(),
        intervalInDays: Double = 1.0,
        easeFactor: Double = 2.5
    ): Flashcard {
        return Flashcard(
            id = id,
            frenchWord = frenchWord,
            englishTranslation = englishTranslation,
            creationDate = creationDate,
            lastReviewed = null, // Or some sensible default for tests if needed
            reviewCount = 0,    // Or some sensible default for tests if needed
            easeFactor = easeFactor,
            intervalInDays = intervalInDays,
            nextReviewDate = nextReviewDate,
            exampleSentence = null, // Or some sensible default for tests if needed
            notes = null,          // Or some sensible default for tests if needed
            difficulty = difficulty
        )
    }


    @Before
    fun setUp() {
        // MockitoJUnitRunner handles mock initialization for @Mock annotated fields
        // val mockApplication = mock<Application>() // Removed, mockApplication is now a @Mock field
        whenever(mockApplication.getString(ArgumentMatchers.anyInt())).thenReturn("Mocked error string")
        Dispatchers.setMain(testDispatcher)
        // mockRepository = mock() // Removed, mockRepository is now a @Mock field

        // Setup initial mock data and the flow that emits it
        initialMockCards = listOf(
            createTestFlashcard(id = 1, frenchWord = "Bonjour", englishTranslation = "Hello", creationDate = LocalDate.now().minusDays(2).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000, difficulty = 3),
            createTestFlashcard(id = 2, frenchWord = "Au revoir", englishTranslation = "Goodbye", creationDate = LocalDate.now().minusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000, difficulty = 1),
            createTestFlashcard(id = 3, frenchWord = "Merci", englishTranslation = "Thank you", creationDate = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000, difficulty = 5)
        )
        mockCardsFlow = MutableStateFlow(initialMockCards)
        whenever(mockRepository.getAllCards()).thenReturn(mockCardsFlow.asStateFlow())

        viewModel = CardManagementViewModel(mockApplication, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads cards with default sort (Recent)`() = runTest {
        val uiState = viewModel.uiState.first { !it.isLoading } // Wait for initial combine

        assertFalse(uiState.displayedCards.isEmpty())
        assertEquals(SortOption.Recent, uiState.sortOption)
        // Check if sorted by creationDate descending
        for (i in 0 until uiState.displayedCards.size - 1) {
            assertTrue(uiState.displayedCards[i].creationDate >= uiState.displayedCards[i+1].creationDate)
        }
    }

    @Test
    fun `updateSearchQuery filters cards correctly`() = runTest {
        viewModel.uiState.first { !it.isLoading } // Ensure initial load

        viewModel.updateSearchQuery("Bonjour")
        var displayedCards = viewModel.uiState.value.displayedCards
        assertEquals(1, displayedCards.size)
        assertEquals("Bonjour", displayedCards.first().frenchWord)

        viewModel.updateSearchQuery("thank") // Case insensitive, part of word
        displayedCards = viewModel.uiState.value.displayedCards
        assertEquals(1, displayedCards.size)
        assertEquals("Merci", displayedCards.first().frenchWord)

        viewModel.updateSearchQuery("") // Clear search
        displayedCards = viewModel.uiState.value.displayedCards
        assertEquals(initialMockCards.size, displayedCards.size)
    }

    @Test
    fun `updateSortOrder sorts Alphabetical correctly`() = runTest {
        viewModel.uiState.first { !it.isLoading }
        viewModel.updateSortOrder(SortOption.Alphabetical)
        val cards = viewModel.uiState.value.displayedCards
        assertEquals(SortOption.Alphabetical, viewModel.uiState.value.sortOption)
        assertEquals("Au revoir", cards[0].frenchWord)
        assertEquals("Bonjour", cards[1].frenchWord)
        assertEquals("Merci", cards[2].frenchWord)
    }

    @Test
    fun `updateSortOrder sorts Difficulty correctly`() = runTest {
        viewModel.uiState.first { !it.isLoading }
        viewModel.updateSortOrder(SortOption.Difficulty)
        val cards = viewModel.uiState.value.displayedCards
        assertEquals(SortOption.Difficulty, viewModel.uiState.value.sortOption)
        assertEquals(1, cards[0].difficulty) // Au revoir
        assertEquals(3, cards[1].difficulty) // Bonjour
        assertEquals(5, cards[2].difficulty) // Merci
    }


    @Test
    fun `toggleCardSelection updates selectedCardIds`() = runTest {
        viewModel.uiState.first { !it.isLoading }
        val cardToSelect = initialMockCards.first()

        viewModel.toggleCardSelection(cardToSelect.id)
        assertTrue(viewModel.uiState.value.selectedCardIds.contains(cardToSelect.id))

        viewModel.toggleCardSelection(cardToSelect.id) // Toggle off
        assertFalse(viewModel.uiState.value.selectedCardIds.contains(cardToSelect.id))
    }

    @Test
    fun `clearSelections clears selectedCardIds`() = runTest {
        viewModel.uiState.first { !it.isLoading }
        val cardToSelect = initialMockCards.first()
        viewModel.toggleCardSelection(cardToSelect.id) // Select one

        viewModel.clearSelections()
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
    }

    @Test
    fun `deleteSelectedCards calls repository and clears selection`() = runTest {
        viewModel.uiState.first { !it.isLoading }
        val cardToDelete1 = initialMockCards[0]
        val cardToDelete2 = initialMockCards[1]

        viewModel.toggleCardSelection(cardToDelete1.id)
        viewModel.toggleCardSelection(cardToDelete2.id)
        assertEquals(2, viewModel.uiState.value.selectedCardIds.size)

        viewModel.deleteSelectedCards()
        advanceUntilIdle() // For the launch block in deleteSelectedCards

        verify(mockRepository).deleteCards(listOf(cardToDelete1.id, cardToDelete2.id))
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())

        // Simulate repository updating the main flow
        mockCardsFlow.value = listOf(initialMockCards[2]) // Only "Merci" should remain
        val finalCards = viewModel.uiState.value.displayedCards
        assertEquals(1, finalCards.size)
        assertEquals("Merci", finalCards.first().frenchWord)
    }

    @Test
    fun `saveNewCard_initializesNextReviewDateCorrectly`() = runTest {
        // Given
        val frenchWord = "Test French"
        val englishTranslation = "Test English"
        val exampleSentence = "Test example."
        val notes = "Test notes."
        val difficulty = 2.5f // Form state uses Float

        // Mock repository insert to return a dummy ID (e.g., 1L)
        whenever(mockRepository.insert(any<Flashcard>())).thenReturn(1L) // Corrected to any<Flashcard>()

        // When
        viewModel.updateNewFrenchWord(frenchWord)
        viewModel.updateNewEnglishTranslation(englishTranslation)
        viewModel.updateNewExampleSentence(exampleSentence)
        viewModel.updateNewNotes(notes)
        viewModel.updateNewDifficulty(difficulty)

        val timeBeforeSave = System.currentTimeMillis()
        viewModel.saveNewCard { /* onSuccess callback */ }
        advanceUntilIdle() // Ensure coroutines launched by saveNewCard complete

        // Then
        verify(mockRepository).insert(flashcardCaptor.capture())
        val capturedFlashcard = flashcardCaptor.value

        assertEquals(frenchWord, capturedFlashcard.frenchWord)
        assertEquals(englishTranslation, capturedFlashcard.englishTranslation)
        assertEquals(exampleSentence, capturedFlashcard.exampleSentence)
        assertEquals(notes, capturedFlashcard.notes)
        assertEquals(difficulty.toInt(), capturedFlashcard.difficulty) // As per current implementation

        assertNotNull(capturedFlashcard.creationDate)
        assertNotNull(capturedFlashcard.nextReviewDate)
        assertEquals(capturedFlashcard.creationDate, capturedFlashcard.nextReviewDate)

        // Check that creationDate is recent (e.g., within 5 seconds of the call)
        assertTrue("Creation date should be recent", capturedFlashcard.creationDate >= timeBeforeSave && capturedFlashcard.creationDate <= System.currentTimeMillis() + 1000) // Allow a small delta

        // Verify form state is reset
        val formState = viewModel.createCardFormState.value
        assertEquals("", formState.frenchWord)
        assertEquals("", formState.englishTranslation)
        // ... also check other fields if necessary
    }
}
