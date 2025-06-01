package com.example.ankizero.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // Added
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.R // Added
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

// Placeholder for preview and initial state
val previewEditFlashcard = Flashcard(
    id = 1L,
    frenchWord = "Bonjour",
    englishTranslation = "Hello",
    // pronunciation = "bon-zhoor", // Removed as per error analysis
    // example = "Bonjour, comment ça va?", // Removed as per error analysis
    notes = "Common greeting.",
    difficulty = 3, // Assuming 1-5
    creationDate = LocalDate.now().minusDays(10).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
    nextReviewDate = LocalDate.now().plusDays(2).atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    // easeFactor = 2.5f, // Removed as per error analysis (screen doesn't use it, Flashcard might not have it)
    // interval = 5 // Removed as per error analysis (screen doesn't use it, Flashcard might not have it)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    // cardId: Long, // In a real scenario, you'd pass ID and fetch the card via ViewModel
    // For this UI task, we'll pass a full Flashcard object for simplicity as per MainActivity setup
    cardToEdit: Flashcard, // Assuming this Flashcard object might not have pronunciation, example, etc.
    onNavigateBack: () -> Unit
    // onUpdateCard: (Flashcard) -> Unit // To be used when ViewModel is integrated
) {
    var frenchWord by remember { mutableStateOf(cardToEdit.frenchWord) }
    var englishTranslation by remember { mutableState of(cardToEdit.englishTranslation) }
    // var pronunciation by remember { mutableStateOf(cardToEdit.pronunciation ?: "") } // Removed
    // var exampleSentence by remember { mutableStateOf(cardToEdit.example ?: "") } // Removed
    var notes by remember { mutableStateOf(cardToEdit.notes ?: "") }
    // Difficulty: Slider 0f-4f maps to 1-5. So, (difficulty - 1) for slider.
    var difficultySliderValue by remember { mutableStateOf((cardToEdit.difficulty ?: 3).toFloat() - 1) }


    var frenchWordError by remember { mutableStateOf<String?>(null) }
    var englishTranslationError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun validateFields(): Boolean {
        frenchWordError = if (frenchWord.isBlank()) "French word cannot be empty" else null
        englishTranslationError = if (englishTranslation.isBlank()) "English translation cannot be empty" else null
        return frenchWordError == null && englishTranslationError == null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_card_screen_title)) }, // Updated
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_cd))  // Updated
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (validateFields()) {
                            // ... card update ...
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.save_changes_to_card_cd)) // Updated
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Make form scrollable
        ) {
            OutlinedTextField(
                value = frenchWord,
                onValueChange = { frenchWord = it; frenchWordError = null },
                label = { Text("French Word*") },
                modifier = Modifier.fillMaxWidth(),
                isError = frenchWordError != null,
                singleLine = true
            )
            if (frenchWordError != null) {
                Text(frenchWordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = englishTranslation,
                onValueChange = { englishTranslation = it; englishTranslationError = null },
                label = { Text("English Translation*") },
                modifier = Modifier.fillMaxWidth(),
                isError = englishTranslationError != null,
                singleLine = true
            )
            if (englishTranslationError != null) {
                Text(englishTranslationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Optional Fields", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            // OutlinedTextField for pronunciation removed
            // Spacer(modifier = Modifier.height(12.dp)) // Removed corresponding spacer

            // OutlinedTextField for exampleSentence removed
            // Spacer(modifier = Modifier.height(12.dp)) // Removed corresponding spacer

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Difficulty (1-5)", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = difficultySliderValue,
                onValueChange = { difficultySliderValue = it },
                valueRange = 0f..4f, // Represents 1 to 5
                steps = 3, // 0, 1, 2, 3, 4 (5 steps)
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Selected: ${(difficultySliderValue.roundToInt() + 1)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true, name = "Edit Card Screen - Light")
@Composable
fun EditCardScreenPreviewLight() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        EditCardScreen(cardToEdit = previewEditFlashcard, onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Edit Card Screen - Dark")
@Composable
fun EditCardScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        EditCardScreen(cardToEdit = previewEditFlashcard, onNavigateBack = {})
    }
}
