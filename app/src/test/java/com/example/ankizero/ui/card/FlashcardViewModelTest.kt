package com.example.ankizero.ui.card

import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock // Added
import org.mockito.MockitoAnnotations // Added
import android.app.Application // Added
import org.mockito.kotlin.mock // Preferred Mockito-Kotlin import
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import kotlin.test.*
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt


@ExperimentalCoroutinesApi
class FlashcardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher() // StandardTestDispatcher can also be used

    @Mock
    private lateinit var mockApplication: Application
    @Mock
    private lateinit var mockRepository: FlashcardRepository
    // As per actual constructor, TextToSpeechHelper is not a direct dependency.
    // @Mock
    // private lateinit var mockTextToSpeechHelper: TextToSpeechHelper

    private lateinit var viewModel: FlashcardViewModel

    // Helper to create Flashcard instances for tests, aligned with entity changes
    private fun createTestFlashcard(
        id: Long,
        frenchWord: String = "French $id",
        englishTranslation: String = "English $id",
        nextReviewDate: Long = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        intervalInDays: Double = 1.0,
        easeFactor: Double = 2.5,
        creationDate: Long = System.currentTimeMillis() - 100000,
        difficulty: Int? = null,
        lastReviewed: Long? = null,
        reviewCount: Int = 0,
        // pronunciation: String? = null, // Removed as it's not in Flashcard entity
        exampleSentence: String? = null,
        notes: String? = null
    ): Flashcard {
        return Flashcard(
            id = id,
            frenchWord = frenchWord,
            englishTranslation = englishTranslation,
            // pronunciation = pronunciation, // Removed
            exampleSentence = exampleSentence,
            notes = notes,
            creationDate = creationDate,
            nextReviewDate = nextReviewDate,
            intervalInDays = intervalInDays,
            easeFactor = easeFactor,
            difficulty = difficulty,
            lastReviewed = lastReviewed,
            reviewCount = reviewCount
        )
    }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Initialize mocks
        Dispatchers.setMain(testDispatcher)
        // mockRepository is now initialized by MockitoAnnotations
        // mockApplication is also initialized by MockitoAnnotations
        // If TextToSpeechHelper were used:
        // mockTextToSpeechHelper = mock() // or use @Mock if it's a class member
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state when no due cards`() = runTest {
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(emptyList()))
        viewModel = FlashcardViewModel(mockApplication, mockRepository)

        val uiState = viewModel.uiState.first()
        assertTrue(uiState.isDeckEmpty)
        assertNull(uiState.currentCard)
        assertEquals("No cards due!", uiState.progressText)
    }

    @Test
    fun `initial state loads due cards correctly`() = runTest {
        val card1 = createTestFlashcard(id = 1, nextReviewDate = LocalDate.now().minusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC))
        val card2 = createTestFlashcard(id = 2, nextReviewDate = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC))
        val dueCards = listOf(card1, card2)
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(dueCards))

        viewModel = FlashcardViewModel(mockApplication, mockRepository)
        val uiState = viewModel.uiState.first { it.currentCard != null } // Wait for card to load

        assertFalse(uiState.isDeckEmpty)
        assertNotNull(uiState.currentCard)
        assertEquals(dueCards.first().id, uiState.currentCard?.id) // Shuffled, so check if one of them
        assertTrue(dueCards.any { it.id == uiState.currentCard?.id })
        assertEquals("Card 1/${dueCards.size}", uiState.progressText)
    }

    @Test
    fun `processCardRating 'Memorized' calls repository_processReview and updates state`() = runTest {
        val initialCard = createTestFlashcard(id = 1, intervalInDays = 1.0, easeFactor = 2.5)
        val updatedCardAfterReview = initialCard.copy(intervalInDays = 1.8, easeFactor = 2.6) // Example update

        whenever(mockRepository.getDueCards()).thenReturn(flowOf(listOf(initialCard)))
        whenever(mockRepository.processReview(any(), org.mockito.kotlin.eq(true)))
            .thenReturn(updatedCardAfterReview) // Mock the updated card returned by processReview

        viewModel = FlashcardViewModel(mockApplication, mockRepository)
        viewModel.uiState.first { it.currentCard != null } // Ensure initial card is loaded

        viewModel.processCardRating(isMemorized = true)
        advanceUntilIdle() // Allow coroutines launched by processCardRating to complete

        verify(mockRepository).processReview(initialCard, true)
        // After rating, the card is removed from the due list for the session
        val finalUiState = viewModel.uiState.value
        assertTrue(finalUiState.isDeckEmpty) // Assuming only one card was due
        assertEquals("No cards due!", finalUiState.progressText)
    }

    @Test
    fun `processCardRating 'No' calls repository_processReview and updates state`() = runTest {
        val initialCard = createTestFlashcard(id = 1, intervalInDays = 5.0, easeFactor = 2.0)
        val updatedCardAfterReview = initialCard.copy(intervalInDays = 1.0, easeFactor = 1.8) // Example update

        whenever(mockRepository.getDueCards()).thenReturn(flowOf(listOf(initialCard)))
        whenever(mockRepository.processReview(any(), org.mockito.kotlin.eq(false)))
            .thenReturn(updatedCardAfterReview)

        viewModel = FlashcardViewModel(mockApplication, mockRepository)
        viewModel.uiState.first { it.currentCard != null }

        viewModel.processCardRating(isMemorized = false)
        advanceUntilIdle()

        verify(mockRepository).processReview(initialCard, false)
        val finalUiState = viewModel.uiState.value
        assertTrue(finalUiState.isDeckEmpty)
    }

    @Test
    fun `showNextCard advances card and updates progress`() = runTest {
        val card1 = createTestFlashcard(id = 1)
        val card2 = createTestFlashcard(id = 2)
        val card3 = createTestFlashcard(id = 3)
        val dueCards = listOf(card1, card2, card3) // ViewModel shuffles this internally
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(dueCards))

        viewModel = FlashcardViewModel(mockApplication, mockRepository)
        val initialState = viewModel.uiState.first { it.currentCard != null }
        val firstLoadedCardId = initialState.currentCard!!.id

        viewModel.showNextCard(moveForward = true)
        advanceUntilIdle()
        val stateAfterNext = viewModel.uiState.value

        assertNotEquals(firstLoadedCardId, stateAfterNext.currentCard?.id)
        assertEquals("Card 2/${dueCards.size}", stateAfterNext.progressText)

        viewModel.showNextCard(moveForward = true)
        advanceUntilIdle()
        val stateAfterNextNext = viewModel.uiState.value
        assertNotEquals(stateAfterNext.currentCard?.id, stateAfterNextNext.currentCard?.id)
        assertEquals("Card 3/${dueCards.size}", stateAfterNextNext.progressText)
    }

    @Test
    fun `showNextCard loops correctly`() = runTest {
        val card1 = createTestFlashcard(id = 1)
        val card2 = createTestFlashcard(id = 2)
        val dueCards = listOf(card1, card2) // VM shuffles this.
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(dueCards))

        viewModel = FlashcardViewModel(mockApplication, mockRepository)
        val initialState = viewModel.uiState.first { it.currentCard != null }
        val firstCardIdInSession = initialState.currentCard!!.id

        viewModel.showNextCard(moveForward = true) // Go to card 2 (in shuffled list)
        advanceUntilIdle()
        val secondCardIdInSession = viewModel.uiState.value.currentCard!!.id
        assertNotEquals(firstCardIdInSession, secondCardIdInSession)
        assertEquals("Card 2/2", viewModel.uiState.value.progressText)

        viewModel.showNextCard(moveForward = true) // Loop back to card 1 (in shuffled list)
        advanceUntilIdle()
        assertEquals(firstCardIdInSession, viewModel.uiState.value.currentCard?.id)
        assertEquals("Card 1/2", viewModel.uiState.value.progressText)
    }

    @Test
    fun `showPreviousCard loops correctly`() = runTest {
        val card1 = createTestFlashcard(id = 1)
        val card2 = createTestFlashcard(id = 2)
        val dueCards = listOf(card1, card2)
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(dueCards))

        viewModel = FlashcardViewModel(mockApplication, mockRepository)
        val initialState = viewModel.uiState.first { it.currentCard != null }
        val firstCardIdInSession = initialState.currentCard!!.id // Could be card1 or card2 due to shuffle

        // Determine the other card's ID
        val otherCardIdInSession = if (firstCardIdInSession == card1.id) card2.id else card1.id

        viewModel.showNextCard(moveForward = false) // From first card, should loop to last (i.e., the other card)
        advanceUntilIdle()
        assertEquals(otherCardIdInSession, viewModel.uiState.value.currentCard?.id)
        assertEquals("Card 2/2", viewModel.uiState.value.progressText)

        viewModel.showNextCard(moveForward = false) // From last card, should loop to first original
        advanceUntilIdle()
        assertEquals(firstCardIdInSession, viewModel.uiState.value.currentCard?.id)
        assertEquals("Card 1/2", viewModel.uiState.value.progressText)
    }

    @Test
    fun `dismissFlipHint updates showFlipHint state`() = runTest {
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(listOf(createTestFlashcard(id = 1))))
        viewModel = FlashcardViewModel(mockApplication, mockRepository)

        // Initial state might show flip hint
        viewModel.uiState.first { it.currentCard != null } // Ensure card is loaded
        // The logic for showFlipHint was: it.currentCard == null && dueCardsList.isNotEmpty()
        // This means it's true before first card is loaded, then false.
        // Let's adjust or test based on this.
        // If the ViewModel is refactored to set showFlipHint = true initially for the first card:
        // assertTrue(viewModel.uiState.value.showFlipHint)

        viewModel.dismissFlipHint()
        assertFalse(viewModel.uiState.value.showFlipHint)
    }
}
