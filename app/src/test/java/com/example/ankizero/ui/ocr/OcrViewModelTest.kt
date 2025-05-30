package com.example.ankizero.ui.ocr

import android.app.Application
import android.net.Uri
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.any // For any() matcher if needed for other verifications

@ExperimentalCoroutinesApi
class OcrViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockApplication: Application
    private lateinit var mockRepository: FlashcardRepository
    private lateinit var viewModel: OcrViewModel

    @Captor
    private lateinit var flashcardCaptor: ArgumentCaptor<Flashcard>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Initialize @Captor
        Dispatchers.setMain(testDispatcher)
        mockApplication = mock()
        mockRepository = mock()
        viewModel = OcrViewModel(mockApplication, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val uiState = viewModel.uiState.value
        assertNull(uiState.imageUri)
        assertEquals("", uiState.detectedText)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.showEditDialog)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `setImageUri updates imageUri and clears previous text and error`() = runTest {
        val testUri = Uri.parse("content://test/image.jpg")
        // Simulate initial error state for clearing
        viewModel.uiState.value.copy(errorMessage = "Initial Error", detectedText = "Initial Text")


        viewModel.setImageUri(testUri)
        val uiState = viewModel.uiState.value

        assertEquals(testUri, uiState.imageUri)
        assertEquals("", uiState.detectedText)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `processImage updates isLoading, detectedText on (simulated) success`() = runTest {
        val testUri = Uri.parse("content://test/image_success.jpg")
        viewModel.setImageUri(testUri)

        val job = launch { viewModel.processImage(testUri) }
        // Check intermediate isLoading state if using StandardTestDispatcher and advanceTime
        // With UnconfinedTestDispatcher, it might complete too fast.
        // For simplicity, we check final state.
        job.join() // Wait for completion

        val finalUiState = viewModel.uiState.value
        assertFalse(finalUiState.isLoading)
        assertTrue(finalUiState.detectedText.contains("Simulated recognized text"))
        assertTrue(finalUiState.showEditDialog)
        assertNull(finalUiState.errorMessage)
    }

    @Test
    fun `saveRecognizedText calls repository insert and updates state`() = runTest {
        val detectedTextFromOcr = "French Word\nEnglish Translation"
        // Setup initial state as if OCR was successful
        viewModel.setImageUri(Uri.parse("content://dummy"))
        // Directly update state for test, as processImage has delay
        (viewModel.uiState as MutableStateFlow).value = OcrUiState(
            imageUri = Uri.parse("content://dummy"),
            detectedText = detectedTextFromOcr,
            isLoading = false,
            showEditDialog = true, // Dialog is shown before save
            errorMessage = null
        )


        viewModel.saveRecognizedText(detectedTextFromOcr)
        advanceUntilIdle() // Ensure launch block in saveRecognizedText completes

        verify(mockRepository).insert(capture(flashcardCaptor))
        val capturedCard = flashcardCaptor.value
        assertEquals("French Word", capturedCard.frenchWord)
        assertEquals("English Translation", capturedCard.englishTranslation)
        // Check other default values if necessary (creationDate, nextReviewDate close to now, etc.)
        assertTrue(System.currentTimeMillis() - capturedCard.creationDate < 1000)


        val finalUiState = viewModel.uiState.value
        assertFalse(finalUiState.showEditDialog)
        assertEquals("", finalUiState.detectedText)
        assertNull(finalUiState.imageUri)
    }

    @Test
    fun `saveRecognizedText with blank text sets error message`() = runTest {
        viewModel.saveRecognizedText("  ") // Blank text
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.errorMessage)
        assertEquals("Cannot save empty text.", uiState.errorMessage)
        assertFalse(uiState.showEditDialog) // Should not proceed to hide dialog if error
    }


    // Other tests (showEditDialog, clearError, clearAll) from previous version remain relevant
    @Test
    fun `showEditDialog updates uiState_showEditDialog`() = runTest {
        viewModel.showEditDialog(true)
        assertTrue(viewModel.uiState.value.showEditDialog)

        viewModel.showEditDialog(false)
        assertFalse(viewModel.uiState.value.showEditDialog)
    }

    @Test
    fun `clearError clears errorMessage`() = runTest {
        // Simulate an error state
         (viewModel.uiState as MutableStateFlow).value = OcrUiState(errorMessage = "Test Error")
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearAll resets state`() = runTest {
        viewModel.setImageUri(Uri.parse("content://test"))
        (viewModel.uiState as MutableStateFlow).value = OcrUiState(detectedText = "Test", isLoading = true)


        viewModel.clearAll()
        val uiState = viewModel.uiState.value
        assertEquals(OcrUiState(), uiState) // Compare with default initial state
    }
}
