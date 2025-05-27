package com.example.ankizero.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.repository.FlashcardRepository

class FlashcardViewModelFactory(
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashcardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashcardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
