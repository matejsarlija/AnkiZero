package com.example.ankizero.ui.notifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository // Added import

class NotificationsViewModelFactory(
    private val application: Application,
    private val repository: FlashcardRepository // Added repository to constructor
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            // Repository is now passed via constructor
            return NotificationsViewModel(application, repository) as T // Pass application and repository
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
