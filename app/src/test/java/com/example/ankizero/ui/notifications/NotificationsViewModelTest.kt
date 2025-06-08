package com.example.ankizero.ui.notifications

import android.app.Application
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify // Added
import org.mockito.kotlin.any // Added
import org.mockito.ArgumentMatchers // Added
import kotlin.test.*
import java.time.LocalDate
import java.time.ZoneOffset

@ExperimentalCoroutinesApi
class NotificationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    @Mock
    private lateinit var mockApplication: Application // Changed to @Mock
    @Mock
    private lateinit var mockRepository: FlashcardRepository // Changed to @Mock
    private lateinit var viewModel: NotificationsViewModel

    private fun createTestFlashcard(id: Long, frenchWord: String = "French $id"): Flashcard {
        return Flashcard(
            id = id,
            frenchWord = frenchWord,
            englishTranslation = "English $id",
            creationDate = System.currentTimeMillis(),
            nextReviewDate = LocalDate.now().minusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000, // Due yesterday
            intervalInDays = 1.0,
            easeFactor = 2.5
        )
    }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Initialize mocks
        Dispatchers.setMain(testDispatcher)
        // mockApplication and mockRepository are now initialized by MockitoAnnotations
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads due cards and default preferences`() = runTest {
        val dueCardsSample = listOf(createTestFlashcard(1), createTestFlashcard(2))
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(dueCardsSample))

        viewModel = NotificationsViewModel(mockApplication, mockRepository)
        val uiState = viewModel.uiState.first() // Initial state from combine

        assertEquals(dueCardsSample, uiState.dueCards)
        assertEquals(true, uiState.dailyRemindersEnabled) // Default from ViewModel's StateFlow
        assertEquals("09:00 AM", uiState.reminderTime)   // Default from ViewModel's StateFlow
    }

    @Test
    fun `initial state with no due cards`() = runTest {
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(emptyList()))
        viewModel = NotificationsViewModel(mockApplication, mockRepository)
        val uiState = viewModel.uiState.first()

        assertTrue(uiState.dueCards.isEmpty())
    }

    @Test
    fun `toggleDailyReminders updates state`() = runTest {
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(emptyList())) // Irrelevant for this test
        viewModel = NotificationsViewModel(mockApplication, mockRepository)

        viewModel.toggleDailyReminders(false)
        assertEquals(false, viewModel.uiState.value.dailyRemindersEnabled)

        viewModel.toggleDailyReminders(true)
        assertEquals(true, viewModel.uiState.value.dailyRemindersEnabled)
    }

    @Test
    fun `setReminderTime updates state`() = runTest {
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(emptyList()))
        viewModel = NotificationsViewModel(mockApplication, mockRepository)

        viewModel.updateReminderTime("14:30") // Changed to updateReminderTime
        assertEquals("02:30 PM", viewModel.uiState.value.reminderTime)

        viewModel.updateReminderTime("08:05") // Changed to updateReminderTime
        assertEquals("08:05 AM", viewModel.uiState.value.reminderTime)

        viewModel.updateReminderTime("00:15") // Changed to updateReminderTime
        assertEquals("12:15 AM", viewModel.uiState.value.reminderTime)

        viewModel.updateReminderTime("12:00") // Changed to updateReminderTime
        assertEquals("12:00 PM", viewModel.uiState.value.reminderTime)
    }

    // The test name should reflect that it now tests updateReminderTime and its effects
    // e.g. `updateReminderTime updates state and schedules notification` - but scheduling is an internal detail not easily verified here without more complex WorkManager testing.
    // For now, keeping it focused on state.
    @Test
    fun `refreshDueCards does not change state if underlying flow is same`() = runTest {
        val dueCardsSample = listOf(createTestFlashcard(1, "Test Card"))
        whenever(mockRepository.getDueCards()).thenReturn(flowOf(dueCardsSample))
        viewModel = NotificationsViewModel(mockApplication, mockRepository)

        val initialState = viewModel.uiState.first() // Collect initial state
        assertEquals(dueCardsSample, initialState.dueCards)

        viewModel.refreshDueCards() // This method is currently a no-op as Flow handles updates
        advanceUntilIdle()

        val stateAfterRefresh = viewModel.uiState.value
        assertEquals(dueCardsSample, stateAfterRefresh.dueCards) // Should remain the same
    }
}
