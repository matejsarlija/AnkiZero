package com.example.ankizero.ui.management

import android.app.Application // Added
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.repository.FlashcardRepository

class CardManagementViewModelFactory(
    private val application: Application, // Added
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardManagementViewModel(application, repository) as T // Pass application
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
