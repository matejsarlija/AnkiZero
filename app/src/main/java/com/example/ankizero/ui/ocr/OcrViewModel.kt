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
        AppLogger.d(TAG, "setImageUri called with URI: $uri")
        _uiState.update { it.copy(imageUri = uri, detectedText = "", errorMessage = null) }
        if (uri != null) {
            // Optional: Immediately start processing if an image URI is set,
            // or wait for an explicit call to processImage.
            // For now, let's assume processImage is called explicitly.
            AppLogger.d(TAG, "Image URI is set. Ready for processing.")
        } else {
            AppLogger.d(TAG, "Image URI is null.")
        }
        AppLogger.d(TAG, "setImageUri completed.")
    }

    fun processImage(uri: Uri) { // Context can be obtained from getApplication()
        AppLogger.d(TAG, "processImage called for URI: $uri")
        val context: Context = getApplication<Application>().applicationContext
        if (_uiState.value.isLoading) {
            AppLogger.d(TAG, "Already processing, skipping new request for URI: $uri")
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        AppLogger.d(TAG, "UI state updated to isLoading = true.")

        viewModelScope.launch {
            AppLogger.d(TAG, "Starting image processing coroutine for URI: $uri")
            AppLogger.d(TAG, "URI Scheme: ${uri.scheme}") // Log URI scheme

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image: InputImage
            try {
                image = InputImage.fromFilePath(context, uri)
                AppLogger.d(TAG, "InputImage created successfully from URI: $uri")
            } catch (e: IOException) {
                AppLogger.e(TAG, "Failed to create InputImage from URI: $uri. Error: ${e.toString()}", e) // Log e.toString()
                com.example.ankizero.util.GlobalErrorHandler.reportError(e, "ML Kit InputImage creation failed for URI: $uri")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load image: ${e.message}"
                    )
                }
                AppLogger.d(TAG, "processImage coroutine finished due to InputImage creation failure.")
                return@launch // Exit coroutine
            }

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    AppLogger.i(TAG, "ML Kit text recognition success. Raw detected text: ${visionText.text}") // Log raw text
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detectedText = visionText.text,
                            showEditDialog = true,
                            errorMessage = null // Clear any previous error
                        )
                    }
                    AppLogger.d(TAG, "UI state updated with detected text, isLoading = false, showEditDialog = true.")
                    AnalyticsHelper.logOcrUsed(getApplication()) // Pass context
                }
                .addOnFailureListener { e ->
                    AppLogger.e(TAG, "ML Kit text recognition failed. Error: ${e.toString()}", e) // Log e.toString()
                    com.example.ankizero.util.GlobalErrorHandler.reportError(e, "ML Kit text recognition failed")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Text recognition failed: ${e.message}" // e.message is often good enough for user
                        )
                    }
                    AppLogger.d(TAG, "UI state updated with error message, isLoading = false.")
                }
            AppLogger.d(TAG, "processImage coroutine finished processing logic.")
        }
    }

    fun showEditDialog(show: Boolean) {
        AppLogger.d(TAG, "showEditDialog called with show: $show")
        _uiState.update { it.copy(showEditDialog = show) }
        if (!show) {
            AppLogger.d(TAG, "Edit dialog hidden.")
            // Optionally clear text when dialog is hidden if it's not saved
            // _uiState.update { it.copy(detectedText = "") }
        } else {
            AppLogger.d(TAG, "Edit dialog shown.")
        }
        AppLogger.d(TAG, "showEditDialog completed.")
    }

    fun saveRecognizedText(text: String) {
        AppLogger.d(TAG, "saveRecognizedText called with text: \"$text\"")
        viewModelScope.launch {
            AppLogger.d(TAG, "Starting saveRecognizedText coroutine.")
            if (text.isNotBlank()) {
                val frenchWord = text.lines().firstOrNull() ?: text
                val englishTranslation = text.lines().drop(1).joinToString("\n").ifEmpty { "[Needs translation]" }
                AppLogger.d(TAG, "Derived French: \"$frenchWord\", English: \"$englishTranslation\"")

                val newCard = com.example.ankizero.data.entity.Flashcard(
                    frenchWord = frenchWord,
                    englishTranslation = englishTranslation,
                    creationDate = System.currentTimeMillis(),
                    nextReviewDate = System.currentTimeMillis(), // Due immediately
                    intervalInDays = 1.0,
                    easeFactor = 2.5
                    // Other fields like pronunciation, exampleSentence, notes, difficulty can be empty/default
                )
                AppLogger.d(TAG, "Created Flashcard object: $newCard")
                try {
                    repository.insert(newCard)
                    AppLogger.i(TAG, "New card saved successfully from OCR text. Card ID likely auto-generated.")
                    AnalyticsHelper.logNewCardSaved(getApplication(), "ocr") // Pass context and source
                    _uiState.update { it.copy(showEditDialog = false, detectedText = "", imageUri = null, errorMessage = null) }
                    AppLogger.d(TAG, "UI state updated after saving: dialog hidden, text/URI cleared.")
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Failed to insert new card into repository. Error: ${e.toString()}", e)
                    com.example.ankizero.util.GlobalErrorHandler.reportError(e, "Failed to save OCR card to database")
                    _uiState.update {
                        it.copy(errorMessage = "Failed to save card: ${e.message}")
                    }
                    AppLogger.d(TAG, "UI state updated with database error message.")
                }
            } else {
                AppLogger.w(TAG, "Recognized text is blank, not saving.")
                _uiState.update { it.copy(errorMessage = "Cannot save empty text.") } // This is already handled by existing code
                AppLogger.d(TAG, "UI state updated with blank text error message.")
            }
            AppLogger.d(TAG, "saveRecognizedText coroutine finished.")
        }
    }

    fun clearError() {
        AppLogger.d(TAG, "clearError called.")
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setImageCaptureFailed(errorMsg: String) {
        AppLogger.d(TAG, "setImageCaptureFailed called with errorMsg: $errorMsg")
        _uiState.update { it.copy(isLoading = false, errorMessage = "Capture failed: $errorMsg", imageUri = null) }
        AppLogger.d(TAG, "setImageCaptureFailed completed.")
    }

    fun clearAll() {
        AppLogger.d(TAG, "clearAll called.")
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
