package com.example.ankizero.ui.ocr

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating instances of [OcrViewModel].
 *
 * This factory provides the [Application] context required by [OcrViewModel]
 * (as it inherits from AndroidViewModel). It also requires a FlashcardRepository.
 */
class OcrViewModelFactory(
    private val application: Application,
    private val repository: com.example.ankizero.data.repository.FlashcardRepository // Added repository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OcrViewModel::class.java)) {
            return OcrViewModel(application, repository) as T // Pass repository
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
