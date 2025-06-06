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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource // Added
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Added
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.R // Added
import kotlinx.coroutines.launch // Added
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardManagementViewModel = viewModel() // Added
) {
    var frenchWord by remember { mutableStateOf("") }
    var englishTranslation by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var exampleSentence by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(2f) } // Default difficulty (e.g., 1-5, so 2f is like 3)

    var frenchWordError by remember { mutableStateOf<String?>(null) }
    var englishTranslationError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Added

    fun validateFields(): Boolean {
        frenchWordError = if (frenchWord.isBlank()) stringResource(id = R.string.french_word_empty_error) else null // Modified
        englishTranslationError = if (englishTranslation.isBlank()) stringResource(id = R.string.english_translation_empty_error) else null // Modified
        return frenchWordError == null && englishTranslationError == null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.create_card_screen_title)) }, // Placeholder, add to strings.xml
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_cd))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (validateFields()) {
                                val newCard = Flashcard(
                                    frenchWord = frenchWord.trim(),
                                    englishTranslation = englishTranslation.trim(),
                                    pronunciation = pronunciation.trim(),
                                    exampleSentence = exampleSentence.trim(),
                                    notes = notes.trim(),
                                    difficulty = difficulty.roundToInt() + 1, // Convert 0f-4f to 1-5
                                    creationDate = System.currentTimeMillis(),
                                    nextReviewDate = System.currentTimeMillis(), // For new cards, review immediately or based on logic
                                    intervalInDays = 1.0, // Default interval
                                    easeFactor = 2.5, // Default ease factor
                                    correctCount = 0,
                                    incorrectCount = 0,
                                    lastReviewedDate = 0L // Never reviewed
                                )
                                viewModel.createCard(newCard) {
                                    // This onComplete lambda is called from the ViewModel
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message = stringResource(id = R.string.card_created_successfully)) // Modified
                                    }
                                    onNavigateBack() // Navigate back after snackbar or simultaneously
                                }
                            }
                        },
                        modifier = Modifier.testTag("SaveCardButton")
                    ) {
                        Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.save_new_card_cd))
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
                modifier = Modifier.fillMaxWidth().testTag("FrenchWordTextField"), // Added
                isError = frenchWordError != null,
                singleLine = true
            )
            if (frenchWordError != null) {
                Text(frenchWordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("FrenchWordErrorText")) // Added
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = englishTranslation,
                onValueChange = { englishTranslation = it; englishTranslationError = null },
                label = { Text("English Translation*") },
                modifier = Modifier.fillMaxWidth().testTag("EnglishTranslationTextField"), // Added
                isError = englishTranslationError != null,
                singleLine = true
            )
            if (englishTranslationError != null) {
                Text(englishTranslationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("EnglishTranslationErrorText")) // Added
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Optional Fields", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pronunciation,
                onValueChange = { pronunciation = it },
                label = { Text("Pronunciation") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = exampleSentence,
                onValueChange = { exampleSentence = it },
                label = { Text("Example Sentence") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))

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
                value = difficulty,
                onValueChange = { difficulty = it },
                valueRange = 0f..4f, // Represents 1 to 5
                steps = 3, // 0, 1, 2, 3, 4 (5 steps)
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Selected: ${(difficulty.roundToInt() + 1)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true, name = "Create Card Screen - Light")
@Composable
fun CreateCardScreenPreviewLight() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        // Preview will likely fail without a ViewModel instance or a fake one.
        // For now, let's assume it's acceptable or will be handled.
        // CreateCardScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Create Card Screen - Dark")
@Composable
fun CreateCardScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        // Preview will likely fail without a ViewModel instance or a fake one.
        // CreateCardScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Create Card Screen - Error State")
@Composable
fun CreateCardScreenErrorPreview() {
    MaterialTheme(colorScheme = lightColorScheme())
        // Preview will likely fail without a ViewModel instance or a fake one.
        // CreateCardScreen(onNavigateBack = {})
    }
}
