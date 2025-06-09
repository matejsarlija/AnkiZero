package com.example.ankizero.ui.notifications

import android.app.Application
import androidx.datastore.core.DataStore // Added DataStore import
import androidx.datastore.preferences.core.Preferences // Added Preferences import
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository // Added import

class NotificationsViewModelFactory(
    private val application: Application,
    private val repository: FlashcardRepository, // Added repository to constructor
    private val dataStore: DataStore<Preferences> // Added dataStore to constructor
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            // Repository and dataStore are now passed via constructor
            return NotificationsViewModel(application, repository, dataStore) as T // Pass application, repository and dataStore
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
