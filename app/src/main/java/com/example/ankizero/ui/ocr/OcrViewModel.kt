package com.example.ankizero.ui.ocr

import android.app.Application // For Application Context
import android.content.Context
import android.net.Uri
// import android.util.Log // Replaced by AppLogger
import androidx.lifecycle.AndroidViewModel // Use AndroidViewModel for Application context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.ankizero.util.AnalyticsHelper
import com.example.ankizero.util.AppLogger // Added AppLogger
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException // Added for InputImage.fromFilePath exception

// ML Kit imports
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions // Using latin options

data class OcrUiState(
    val imageUri: Uri? = null,
    val detectedText: String = "", // Changed from String? to String for easier TextField binding
    val isLoading: Boolean = false,
    val showEditDialog: Boolean = false,
    val errorMessage: String? = null // For showing errors from ML Kit or other operations
)

class OcrViewModel(
    application: Application,
    private val repository: com.example.ankizero.data.repository.FlashcardRepository // Added repository
) : AndroidViewModel(application) {

    private val TAG = "OcrViewModel"

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun setImageUri(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri, detectedText = "", errorMessage = null) }
        if (uri != null) {
            // Optional: Immediately start processing if an image URI is set,
            // or wait for an explicit call to processImage.
            // For now, let's assume processImage is called explicitly.
        }
    }

    fun processImage(uri: Uri) { // Context can be obtained from getApplication()
        val context: Context = getApplication<Application>().applicationContext
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            AppLogger.d(TAG, "Starting image processing for URI: $uri")
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image: InputImage
            try {
                image = InputImage.fromFilePath(context, uri)
            } catch (e: IOException) {
                AppLogger.e(TAG, "Failed to create InputImage from URI: $uri", e)
                com.example.ankizero.util.GlobalErrorHandler.reportError(e, "ML Kit InputImage creation failed for URI: $uri")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load image: ${e.message}"
                    )
                }
                return@launch // Exit coroutine
            }

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    AppLogger.i(TAG, "ML Kit text recognition success. Detected text length: ${visionText.text.length}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detectedText = visionText.text,
                            showEditDialog = true,
                            errorMessage = null // Clear any previous error
                        )
                    }
                    AnalyticsHelper.logOcrUsed(getApplication()) // Pass context
                }
                .addOnFailureListener { e ->
                    AppLogger.e(TAG, "ML Kit text recognition failed.", e)
                    com.example.ankizero.util.GlobalErrorHandler.reportError(e, "ML Kit text recognition failed")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Text recognition failed: ${e.message}"
                        )
                    }
                }
        }
    }

    fun showEditDialog(show: Boolean) {
        _uiState.update { it.copy(showEditDialog = show) }
        if (!show) {
            // Optionally clear text when dialog is hidden if it's not saved
            // _uiState.update { it.copy(detectedText = "") }
        }
    }

    fun saveRecognizedText(text: String) {
        AppLogger.d(TAG, "Attempting to save recognized text: $text")
        viewModelScope.launch {
            // Basic example: Use detected text for both French and English, or prompt user for more.
            // User might need to edit this significantly.
            // For now, let's assume the 'text' is the French part, English part needs to be added.
            // Or, treat the whole text as one field and let user sort it out later.
            // A more sophisticated approach would parse the text or allow user to assign parts.
            if (text.isNotBlank()) {
                val newCard = com.example.ankizero.data.entity.Flashcard(
                    frenchWord = text.lines().firstOrNull() ?: text, // Take first line or whole text
                    englishTranslation = text.lines().drop(1).joinToString("\n").ifEmpty { "[Needs translation]" }, // Rest as English
                    creationDate = System.currentTimeMillis(),
                    nextReviewDate = System.currentTimeMillis(), // Due immediately
                    intervalInDays = 1.0,
                    easeFactor = 2.5,
                    // Other fields like pronunciation, exampleSentence, notes, difficulty can be empty/default
                )
                repository.insert(newCard)
                AnalyticsHelper.logNewCardSaved(getApplication(), "ocr") // Pass context and source
                AppLogger.i(TAG, "New card saved from OCR text.")
                _uiState.update { it.copy(showEditDialog = false, detectedText = "", imageUri = null, errorMessage = null) }
            } else {
                AppLogger.w(TAG, "Recognized text is blank, not saving.")
                _uiState.update { it.copy(errorMessage = "Cannot save empty text.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setImageCaptureFailed(errorMsg: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = "Capture failed: $errorMsg", imageUri = null) }
    }

    fun clearAll() {
        _uiState.update { OcrUiState() } // Reset to initial state
    }
}

/*
TODO: OCR Processing Optimization Notes (for when ML Kit is fully integrated):
1.  **Run on Background Thread:**
    - ML Kit's `process()` method is asynchronous and returns a Task. Ensure callbacks handle results on the main thread for UI updates.
    - ViewModel's `viewModelScope` with `Dispatchers.IO` or `Dispatchers.Default` should be used for any CPU-intensive pre/post-processing of images or text if not directly handled by ML Kit's async nature. (Current stub uses viewModelScope.launch which is fine).

2.  **Image Downscaling:**
    - Before sending an image to ML Kit, consider downscaling it if the original resolution is very high (e.g., from a modern phone camera).
    - ML Kit Text Recognition often doesn't require extremely high-resolution images and processing smaller images is faster and uses less memory.
    - Target a reasonable resolution (e.g., 1024x768 or 1280x960) that balances accuracy and performance. This needs to be tested.
    - Bitmap manipulation (scaling, rotation if needed due to EXIF) can be done using Android's Bitmap APIs.

3.  **Model Management (Less critical for Latin text, more for other languages):**
    - For Latin-based text recognition, the default model is usually bundled or downloaded seamlessly.
    - If using other language models that are large or downloaded on demand, consider:
        - Triggering model downloads explicitly at an appropriate time (e.g., on app start, or before first OCR use, with user consent if large).
        - Checking if models are available before attempting recognition.
        - Providing feedback to the user during model downloads.
        - Clearing unused models if storage becomes an issue (less common for OCR default models).

4.  **InputImage Creation:**
    - Ensure `InputImage` is created correctly from `Uri`, `Bitmap`, `ByteBuffer`, etc.
    - Pay attention to image rotation. Images from camera might have EXIF orientation data that needs tobe read and applied to create the `InputImage` with correct rotation for ML Kit to process accurately. `InputImage.fromFilePath(context, uri)` often handles this, but verify.

5.  **Frame Processing (for live camera preview analysis - more advanced):**
    - If implementing live text detection from camera frames (using CameraX ImageAnalysis use case):
        - Process frames judiciously. Don't run OCR on every single frame if not needed, as it can be resource-intensive.
        - Use a frame throttling mechanism (e.g., process one frame every N ms or when content appears stable).
        - Ensure the ImageAnalysis pipeline is efficient (e.g., using YUV_420_888 format if possible, which is generally efficient for ML Kit).
*/
