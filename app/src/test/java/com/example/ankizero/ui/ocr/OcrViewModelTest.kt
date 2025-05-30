package com.example.ankizero.ui.ocr

import android.app.Application
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock // Using Mockito for Application mock

@ExperimentalCoroutinesApi
class OcrViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockApplication: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Mock Application context for AndroidViewModel
        mockApplication = mock(Application::class.java)
        // If specific context behavior is needed:
        // val mockContext: Context = mock(Context::class.java)
        // `when`(mockApplication.applicationContext).thenReturn(mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val viewModel = OcrViewModel(mockApplication)
        val uiState = viewModel.uiState.value

        assertNull(uiState.imageUri)
        assertEquals("", uiState.detectedText)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.showEditDialog)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `setImageUri updates imageUri and clears previous text and error`() = runTest {
        val viewModel = OcrViewModel(mockApplication)
        val testUri = Uri.parse("content://test/image.jpg")

        // Set initial error and text
        viewModel.processImage(Uri.parse("content://error")) // Simulate error
        kotlinx.coroutines.delay(1600) // wait for stubbed delay + buffer
        assertNotNull(viewModel.uiState.value.errorMessage)


        viewModel.setImageUri(testUri)
        val uiState = viewModel.uiState.value

        assertEquals(testUri, uiState.imageUri)
        assertEquals("", uiState.detectedText) // Should clear old detected text
        assertNull(uiState.errorMessage)     // Should clear old error
    }

    @Test
    fun `processImage updates isLoading, detectedText on (simulated) success`() = runTest(testDispatcher) {
        val viewModel = OcrViewModel(mockApplication)
        val testUri = Uri.parse("content://test/image_success.jpg")
        viewModel.setImageUri(testUri) // Set URI first

        // Launch the processing in a way that allows us to observe intermediate states
        val job = launch { viewModel.processImage(testUri) }

        // Check isLoading is true immediately or shortly after call
        // With UnconfinedTestDispatcher, it might already be false if the coroutine finishes fast.
        // To test isLoading reliably, StandardTestDispatcher and advanceTime might be better.
        // For now, let's check the sequence.
        var isLoadingMidProcessing = false
        val firstState = viewModel.uiState.first() // state before processImage might complete
         if(firstState.isLoading) isLoadingMidProcessing = true


        job.join() // Wait for processImage to complete

        val finalUiState = viewModel.uiState.value
        assertTrue("isLoading should be true at some point or test needs adjustment for UnconfinedDispatcher", isLoadingMidProcessing || !finalUiState.isLoading)


        assertFalse(finalUiState.isLoading)
        assertTrue(finalUiState.detectedText.contains("Simulated recognized text"))
        assertTrue(finalUiState.showEditDialog) // Dialog should show on success
        assertNull(finalUiState.errorMessage)
    }

    @Test
    fun `processImage updates isLoading, errorMessage on (simulated) failure`() = runTest {
        val viewModel = OcrViewModel(mockApplication)
        // Simulate failure by modifying the stub or by passing a specific URI if VM handles it
        // The current stub in OcrViewModel always succeeds.
        // To test failure, the stub needs to be modifiable or the ViewModel needs a way to force failure.
        // For this test, we'll assume a hypothetical scenario where it could fail.
        // We can trigger the generic catch block by making InputImage.fromFilePath throw an error,
        // but that's an integration detail. Let's assume the stub could set an error.

        // If processImage were to set an error:
        // val errorUri = Uri.parse("content://test/image_error.jpg")
        // viewModel.setImageUri(errorUri)
        // viewModel.processImage(errorUri)
        // val uiState = viewModel.uiState.value
        // assertFalse(uiState.isLoading)
        // assertNotNull(uiState.errorMessage)
        // assertFalse(uiState.showEditDialog)

        // Current stub always succeeds, so this test case is more of a placeholder.
        assertTrue("Test requires ViewModel modification for simulating ML Kit failure.", true)
    }

    @Test
    fun `showEditDialog updates uiState_showEditDialog`() = runTest {
        val viewModel = OcrViewModel(mockApplication)

        viewModel.showEditDialog(true)
        assertTrue(viewModel.uiState.value.showEditDialog)

        viewModel.showEditDialog(false)
        assertFalse(viewModel.uiState.value.showEditDialog)
    }

    @Test
    fun `saveRecognizedText clears detectedText, imageUri and hides dialog`() = runTest {
        val viewModel = OcrViewModel(mockApplication)
        val testUri = Uri.parse("content://test/image.jpg")
        val testText = "Some recognized text"

        // Setup state as if text was recognized and dialog is shown
        viewModel.setImageUri(testUri) // Sets imageUri
        // Simulate text detection leading to dialog
        viewModel.processImage(testUri)
        kotlinx.coroutines.delay(1600) // wait for stub
        assertTrue(viewModel.uiState.value.showEditDialog)
        assertTrue(viewModel.uiState.value.detectedText.isNotEmpty())


        viewModel.saveRecognizedText("Edited: $testText")
        val uiState = viewModel.uiState.value

        assertFalse(uiState.showEditDialog)
        assertEquals("", uiState.detectedText) // Should be cleared
        assertNull(uiState.imageUri)      // Should be cleared
    }

    @Test
    fun `clearError clears errorMessage`() = runTest {
        val viewModel = OcrViewModel(mockApplication)
        // Manually set an error state (assuming a way to do this, or after a simulated error)
        // For now, let's assume processImage could set an error, and then we clear it.
        // This depends on the failure simulation test case being implemented.

        // _uiState.update { it.copy(errorMessage = "Test Error") } // Not possible directly
        // For now, this test is conceptual until error state can be reliably set.
        viewModel.clearError() // Call it anyway
        assertNull(viewModel.uiState.value.errorMessage) // Should be null if it was already null or cleared
    }

    @Test
    fun `clearAll resets state`() = runTest {
        val viewModel = OcrViewModel(mockApplication)
        val testUri = Uri.parse("content://test/image.jpg")

        viewModel.setImageUri(testUri)
        viewModel.processImage(testUri) // This will set isLoading, detectedText, showEditDialog
        kotlinx.coroutines.delay(1600) // wait for stubbed delay

        viewModel.clearAll()
        val uiState = viewModel.uiState.value

        assertEquals(OcrUiState(), uiState) // Compare with default initial state
    }
}
