package com.example.ankizero.ui.notifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.repository.FlashcardRepository // Added import

class NotificationsViewModelFactory(
    private val application: Application,
    private val repository: FlashcardRepository // Added repository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(application, repository) as T // Pass repository
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
