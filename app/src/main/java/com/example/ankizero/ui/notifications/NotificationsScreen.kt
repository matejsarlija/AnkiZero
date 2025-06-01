package com.example.ankizero.ui.notifications

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ankizero.data.database.AppDatabase // Added import
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.data.repository.FlashcardRepository // Added import
import java.time.LocalDate
// import java.time.LocalTime // Removed import
import java.time.ZoneOffset
// import java.time.format.DateTimeFormatter // Removed import
import android.util.Log // Added import
import android.content.Context // Added import

// Placeholder data for preview
val previewDueCards = List(3) { index ->
    Flashcard(
        id = index.toLong(),
        frenchWord = "Mot Français Due ${index + 1}",
        englishTranslation = "English Word Due ${index + 1}",
        creationDate = LocalDate.now().minusDays(10).toEpochSecond(ZoneOffset.UTC),
        nextReviewDate = LocalDate.now().toEpochSecond(ZoneOffset.UTC)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen( // Removed default ViewModel instantiation
    viewModel: NotificationsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Study Reminders & Due Cards") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Notification Preferences
            Text("Notification Preferences", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Daily Reminders")
                Switch(
                    checked = uiState.dailyRemindersEnabled,
                    onCheckedChange = { viewModel.toggleDailyReminders(it) }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Reminder Time")
                TextButton(onClick = { /* TODO: Implement Time Picker */ }) {
                    Text(uiState.reminderTime, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Due Cards List
            Text("Hypothetically Due Cards", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            if (uiState.dueCards.isEmpty()) {
                Text("No cards are currently marked as due for notification preview.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.dueCards, key = { it.id }) { card ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(card.frenchWord, style = MaterialTheme.typography.bodyLarge)
                                // Optionally show English translation or other details
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: Navigate to FlashcardScreen or trigger review */
                        Log.d("NotificationsScreen", "Review Now clicked")
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Review Due Cards Now (${uiState.dueCards.size})")
                }
            }
        }
    }
}

// Preview a version with placeholder data
@Preview(showBackground = true, name = "Notifications Screen - Light")
@Composable
fun NotificationsScreenPreviewLight() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    // Assuming CardRepository can be instantiated directly for preview
    val repository = FlashcardRepository(AppDatabase.getDatabase(application).flashCardDao())
    // Assuming NotificationsViewModel can be instantiated directly for preview
    val previewViewModel = NotificationsViewModel(application, repository)

    MaterialTheme(colorScheme = lightColorScheme()) {
        NotificationsScreen(viewModel = previewViewModel)
    }
}

@Preview(showBackground = true, name = "Notifications Screen - Dark")
@Composable
fun NotificationsScreenPreviewDark() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val repository = FlashcardRepository(AppDatabase.getDatabase(application).flashCardDao())
    val previewViewModel = NotificationsViewModel(application, repository)

    MaterialTheme(colorScheme = darkColorScheme()) {
        NotificationsScreen(viewModel = previewViewModel)
    }
}

// Removed ApplicationProvider as LocalContext.current.applicationContext is used directly.
