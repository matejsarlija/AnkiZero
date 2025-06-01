package com.example.ankizero.ui.card

import android.app.Application // Added
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository

class FlashcardViewModelFactory(
    private val application: Application // Added
    // private val repository: FlashcardRepository // Removed repository from constructor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashcardViewModel::class.java)) {
            // Instantiate repository here
            val database = AppDatabase.getDatabase(application)
            val flashCardDao = database.flashCardDao()
            val repository = FlashcardRepository(flashCardDao)
            @Suppress("UNCHECKED_CAST")
            return FlashcardViewModel(application, repository) as T // Pass application and new repository
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
