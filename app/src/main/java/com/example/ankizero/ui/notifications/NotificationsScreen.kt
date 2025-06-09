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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController // Added NavController import
import com.example.ankizero.AnkiZeroApplication
import com.example.ankizero.Screen // Added Screen import
import com.example.ankizero.dataStore
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
        creationDate = LocalDate.now().minusDays(10).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        nextReviewDate = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    application: Application = LocalContext.current.applicationContext as Application,
    repository: FlashcardRepository,
    navController: NavController // Added NavController
) {
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val dataStore = applicationContext.dataStore // Get DataStore instance
    val viewModel: NotificationsViewModel = viewModel(factory = NotificationsViewModelFactory(applicationContext, repository, dataStore))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showTimePicker by remember { mutableStateOf(false) }

    // Parse current reminder time for TimePickerDialog initial values
    // uiState.reminderTime is in "hh:mm a" format, needs to be parsed to HH and mm
    // viewModel._reminderTime.value is in "HH:mm"
    val (initialHour, initialMinute) = remember(uiState.reminderTime) {
        try {
            // Directly use the 24-hour format string from the ViewModel's internal state
            // This avoids parsing the display-formatted time.
            // We need to access the raw "HH:mm" value that feeds into formatDisplayTime.
            // This requires viewModel._reminderTime to be accessible or to have another state for "HH:mm".
            // For now, let's assume we can get it or parse it carefully.
            // The viewModel.uiState.reminderTime is already formatted.
            // The source is viewModel._reminderTime.value (private).
            // A better approach would be for the ViewModel to expose the raw HH:mm parts or string.
            // Let's try parsing uiState.reminderTime carefully.
            val timeParts = uiState.reminderTime.split(":", " ")
            var hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            if (timeParts.size == 3 && timeParts[2].equals("PM", ignoreCase = true) && hour != 12) {
                hour += 12
            } else if (timeParts.size == 3 && timeParts[2].equals("AM", ignoreCase = true) && hour == 12) { // 12 AM is 00 hours
                hour = 0
            }
            hour to minute
        } catch (e: Exception) {
            Log.e("NotificationsScreen", "Error parsing reminder time: ${uiState.reminderTime}", e)
            9 to 0 // Default to 09:00 if parsing fails
        }
    }


    if (showTimePicker) {
        val timePickerDialog = android.app.TimePickerDialog(
            context,
            { _, selectedHour: Int, selectedMinute: Int ->
                viewModel.updateReminderTime(String.format("%02d:%02d", selectedHour, selectedMinute))
                showTimePicker = false
            },
            initialHour,
            initialMinute,
            false // Use 24-hour format if desired, or true for AM/PM
        )
        timePickerDialog.setOnCancelListener { showTimePicker = false }
        timePickerDialog.show()
        // To prevent dialog from re-appearing on recomposition after dismissal,
        // ensure showTimePicker is correctly reset or use LaunchedEffect for showing dialog.
        // For this case, setting showTimePicker = false in onDismiss/onCancel is usually sufficient.
    }

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
                TextButton(onClick = { showTimePicker = true }) {
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
                    onClick = {
                        Log.d("NotificationsScreen", "Review Now clicked, navigating to Flashcards")
                        navController.navigate(Screen.Flashcards)
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
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val repository = applicationContext.repository
    // val dataStore = applicationContext.dataStore // Not strictly needed for this preview if NavController is stubbed

    // Create a stub NavController for preview purposes
    val navController = NavController(LocalContext.current) // Simple stub

    MaterialTheme(colorScheme = lightColorScheme()) {
        NotificationsScreen(
            application = applicationContext,
            repository = repository,
            navController = navController // Pass stub NavController
        )
    }
}

@Preview(showBackground = true, name = "Notifications Screen - Dark")
@Composable
fun NotificationsScreenPreviewDark() {
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val repository = applicationContext.repository
    // val dataStore = applicationContext.dataStore // Not strictly needed for this preview

    // Create a stub NavController for preview purposes
    val navController = NavController(LocalContext.current) // Simple stub

    MaterialTheme(colorScheme = darkColorScheme()) {
        NotificationsScreen(
            application = applicationContext,
            repository = repository,
            navController = navController // Pass stub NavController
        )
    }
}

// Removed ApplicationProvider as LocalContext.current.applicationContext is used directly.
