package com.example.ankizero.ui.notifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository // Added import

class NotificationsViewModelFactory(
    private val application: Application
    // private val repository: FlashcardRepository // Removed repository from constructor
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            // Instantiate repository here
            val database = AppDatabase.getDatabase(application)
            val flashCardDao = database.flashCardDao()
            val repository = FlashcardRepository(flashCardDao)
            return NotificationsViewModel(application, repository) as T // Pass application and new repository
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
