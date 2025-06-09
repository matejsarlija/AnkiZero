package com.google.firebase.codelab.letschat

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.mlkit.vision.text.Text
import com.google.firebase.codelab.letschat.data.ImageTextRepository
import com.google.firebase.codelab.letschat.util.GlobalErrorHandler
import com.google.firebase.codelab.letschat.viewmodel.OcrUiState
import com.google.firebase.codelab.letschat.viewmodel.OcrViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class OcrViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: OcrViewModel
    private lateinit var mockImageTextRepository: ImageTextRepository
    private lateinit var mockGlobalErrorHandler: GlobalErrorHandler
    private lateinit var mockBitmap: Bitmap

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockImageTextRepository = mockk()
        mockGlobalErrorHandler = mockk(relaxUnitFun = true) // For error logging
        mockBitmap = mockk(relaxed = true) // Relaxed mock for Bitmap

        viewModel = OcrViewModel(mockImageTextRepository, mockGlobalErrorHandler)
    }

    @Test
    fun `initial state - is idle and no image selected`() = runTest {
        val uiState = viewModel.uiState.first()
        assertTrue("Should be in Idle state initially", uiState is OcrUiState.Idle)
        assertFalse("No image should be selected initially", viewModel.hasImageSelected.first())
    }

    @Test
    fun `setImage - updates hasImageSelected and uiState to ReadyToProcess`() = runTest {
        viewModel.setImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.first()
        assertTrue("hasImageSelected should be true after setting image", viewModel.hasImageSelected.first())
        assertTrue("UI state should be ReadyToProcess", uiState is OcrUiState.ReadyToProcess)
        assertEquals(mockBitmap, (uiState as OcrUiState.ReadyToProcess).imageBitmap)
    }

    @Test
    fun `processImage - success - updates uiState to TextFound`() = runTest {
        viewModel.setImage(mockBitmap) // Set image first
        testDispatcher.scheduler.advanceUntilIdle()

        val mockTextResult = mockk<Text>()
        coEvery { mockTextResult.text } returns "French Text
English Text"
        coEvery { mockImageTextRepository.processImage(mockBitmap) } returns mockTextResult

        viewModel.processImage()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.first()
        assertTrue("UI state should be TextFound after successful processing", uiState is OcrUiState.TextFound)
        val textFoundState = uiState as OcrUiState.TextFound
        assertEquals("French Text", textFoundState.frenchText)
        assertEquals("English Text", textFoundState.englishText)
        assertEquals(mockBitmap, textFoundState.imageBitmap)
    }

    @Test
    fun `processImage - success - parses multi-line text correctly`() = runTest {
        viewModel.setImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val mockTextResult = mockk<Text>()
        // Assuming the ViewModel splits by the first newline for French/English
        coEvery { mockTextResult.text } returns "Bonjour le monde
Hello world
Another line"
        coEvery { mockImageTextRepository.processImage(mockBitmap) } returns mockTextResult

        viewModel.processImage()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.first() as OcrUiState.TextFound
        assertEquals("Bonjour le monde", uiState.frenchText)
        assertEquals("Hello world
Another line", uiState.englishText) // Or however the VM parses
    }

    @Test
    fun `processImage - success - handles single line text as French`() = runTest {
        viewModel.setImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val mockTextResult = mockk<Text>()
        coEvery { mockTextResult.text } returns "Only French Text"
        coEvery { mockImageTextRepository.processImage(mockBitmap) } returns mockTextResult

        viewModel.processImage()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.first() as OcrUiState.TextFound
        assertEquals("Only French Text", uiState.frenchText)
        assertEquals("", uiState.englishText) // English text should be empty
    }

    @Test
    fun `processImage - no text found - updates uiState to Error`() = runTest {
        viewModel.setImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val mockTextResult = mockk<Text>()
        coEvery { mockTextResult.text } returns "" // Empty text
        coEvery { mockImageTextRepository.processImage(mockBitmap) } returns mockTextResult

        viewModel.processImage()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.first()
        assertTrue("UI state should be Error when no text is found", uiState is OcrUiState.Error)
        assertNotNull((uiState as OcrUiState.Error).message)
    }

    @Test
    fun `processImage - repository throws exception - updates uiState to Error and logs`() = runTest {
        viewModel.setImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val exceptionMessage = "OCR failed badly"
        coEvery { mockImageTextRepository.processImage(mockBitmap) } throws RuntimeException(exceptionMessage)

        viewModel.processImage()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.first()
        assertTrue("UI state should be Error when repository throws exception", uiState is OcrUiState.Error)
        assertEquals(exceptionMessage, (uiState as OcrUiState.Error).message)

        // Verify error logging
        val slot = slot<Throwable>()
        verify { mockGlobalErrorHandler.logError(any(), capture(slot)) }
        assertTrue(slot.captured is RuntimeException)
        assertEquals(exceptionMessage, slot.captured.message)
    }

    @Test
    fun `resetState - returns to Idle state and clears image selection`() = runTest {
        viewModel.setImage(mockBitmap)
        // Mock processing to get into a non-idle state
        val mockTextResult = mockk<Text>()
        coEvery { mockTextResult.text } returns "Some text"
        coEvery { mockImageTextRepository.processImage(mockBitmap) } returns mockTextResult
        viewModel.processImage()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse("Should not be Idle before reset", viewModel.uiState.first() is OcrUiState.Idle)
        assertTrue(viewModel.hasImageSelected.first())

        viewModel.resetState()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.first()
        assertTrue("Should be in Idle state after reset", uiState is OcrUiState.Idle)
        assertFalse("hasImageSelected should be false after reset", viewModel.hasImageSelected.first())
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
