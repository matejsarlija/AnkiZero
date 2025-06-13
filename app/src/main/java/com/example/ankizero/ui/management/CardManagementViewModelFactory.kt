package com.example.ankizero.ui.management

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ankizero.data.repository.FlashcardRepository

class CardManagementViewModelFactory(
    private val application: Application,
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardManagementViewModel::class.java)) {
            return CardManagementViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
