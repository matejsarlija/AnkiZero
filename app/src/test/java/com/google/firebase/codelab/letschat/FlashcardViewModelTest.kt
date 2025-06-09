package com.google.firebase.codelab.letschat

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.codelab.letschat.data.FlashcardRepository
import com.google.firebase.codelab.letschat.model.Flashcard
import com.google.firebase.codelab.letschat.viewmodel.FlashcardViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FlashcardViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // For LiveData if used by ViewModel

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: FlashcardViewModel
    private lateinit var mockRepository: FlashcardRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk<FlashcardRepository>()
        // Setup default mock behavior
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(emptyList())
        viewModel = FlashcardViewModel(mockRepository)
    }

    @Test
    fun `initial state - no due cards - currentCard is null and no flip hint`() = runTest {
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(emptyList())
        // Re-initialize viewModel or trigger state collection if necessary
        viewModel = FlashcardViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines complete

        assertNull("Current card should be null when no due cards", viewModel.uiState.value.currentCard)
        assertFalse("Flip hint should be false initially", viewModel.uiState.value.showFlipHint)
    }

    @Test
    fun `initial state - with due cards - first card is current, flip hint shown`() = runTest {
        val cards = listOf(
            Flashcard(id = "1", french = "Bonjour", english = "Hello"),
            Flashcard(id = "2", french = "Au revoir", english = "Goodbye")
        )
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(cards)
        viewModel = FlashcardViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Current card should not be null", viewModel.uiState.value.currentCard)
        assertEquals("1", viewModel.uiState.value.currentCard?.id)
        assertTrue("Flip hint should be true for a new card", viewModel.uiState.value.showFlipHint)
    }

    @Test
    fun `flipCard - toggles showAnswer and hides flip hint`() = runTest {
        val cards = listOf(Flashcard(id = "1", french = "Bonjour", english = "Hello"))
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(cards)
        viewModel = FlashcardViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showFlipHint)
        assertFalse(viewModel.uiState.value.showAnswer)

        viewModel.flipCard()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse("Flip hint should be false after flipping", viewModel.uiState.value.showFlipHint)
        assertTrue("showAnswer should be true after flipping", viewModel.uiState.value.showAnswer)

        viewModel.flipCard() // Flip back
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse("showAnswer should be false after flipping back", viewModel.uiState.value.showAnswer)
    }

    @Test
    fun `updateCardKnowledge - advances to next card if available`() = runTest {
        val cards = listOf(
            Flashcard(id = "1", french = "Un", english = "One"),
            Flashcard(id = "2", french = "Deux", english = "Two")
        )
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(cards)
        coEvery { mockRepository.updateFlashcard(any()) } returns Unit // Mock update operation
        viewModel = FlashcardViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("1", viewModel.uiState.value.currentCard?.id)

        viewModel.updateCardKnowledge(true) // Mark as known
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("2", viewModel.uiState.value.currentCard?.id)
        assertTrue("Flip hint should be shown for the new card", viewModel.uiState.value.showFlipHint)
    }

    @Test
    fun `updateCardKnowledge - no next card - currentCard becomes null`() = runTest {
        val card = Flashcard(id = "1", french = "Un", english = "One")
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(listOf(card))
        coEvery { mockRepository.updateFlashcard(any()) } returns Unit
        viewModel = FlashcardViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("1", viewModel.uiState.value.currentCard?.id)

        viewModel.updateCardKnowledge(false) // Mark as not known
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull("Current card should be null after last card is processed", viewModel.uiState.value.currentCard)
    }

    @Test
    fun `showFlipHint logic - hint shown for new card, hidden after flip`() = runTest {
        val cards = listOf(Flashcard(id = "1", french = "Oui", english = "Yes"))
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(cards)
        viewModel = FlashcardViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Flip hint should be true for a new card", viewModel.uiState.value.showFlipHint)

        viewModel.flipCard()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse("Flip hint should be false after flipping", viewModel.uiState.value.showFlipHint)
    }

    @Test
    fun `showFlipHint logic - hint shown again for next new card`() = runTest {
        val cards = listOf(
            Flashcard(id = "1", french = "Un", english = "One"),
            Flashcard(id = "2", french = "Deux", english = "Two")
        )
        coEvery { mockRepository.getDueFlashcards() } returns flowOf(cards)
        coEvery { mockRepository.updateFlashcard(any()) } returns Unit
        viewModel = FlashcardViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // First card
        assertTrue(viewModel.uiState.value.showFlipHint)
        viewModel.flipCard()
        assertFalse(viewModel.uiState.value.showFlipHint)

        // Advance to next card
        viewModel.updateCardKnowledge(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Second card
        assertEquals("2", viewModel.uiState.value.currentCard?.id)
        assertTrue("Flip hint should be true for the new (second) card", viewModel.uiState.value.showFlipHint)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
