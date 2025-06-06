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
import androidx.lifecycle.viewmodel.compose.viewModel // Added
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.R // Added
import kotlinx.coroutines.launch // Added
import kotlin.math.roundToInt

// Placeholder for preview and initial state
// val previewEditFlashcard = Flashcard( // Commented out as it will be handled by ViewModel or mocked
//     id = 1L,
//     frenchWord = "Bonjour",
//     englishTranslation = "Hello",
//     notes = "Common greeting.",
//     difficulty = 3,
//     creationDate = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000,
//     nextReviewDate = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000
// )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardId: Long, // Changed: Pass ID and fetch the card via ViewModel
    onNavigateBack: () -> Unit,
    viewModel: CardManagementViewModel = viewModel() // Added
) {
    val uiState by viewModel.uiState.collectAsState() // Added
    var cardToEditState by remember { mutableStateOf<Flashcard?>(null) } // Added

    var frenchWord by remember { mutableStateOf("") }
    var englishTranslation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var difficultySliderValue by remember { mutableStateOf(2f) } // Default to medium (3)

    var frenchWordError by remember { mutableStateOf<String?>(null) }
    var englishTranslationError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Added

    // Fetch card details when cardId or uiState changes
    LaunchedEffect(cardId, uiState) { // Added
        val card = uiState.displayedCards.find { it.id == cardId } ?: uiState.allCards.find { it.id == cardId }
        if (card != null) {
            cardToEditState = card
            frenchWord = card.frenchWord
            englishTranslation = card.englishTranslation
            notes = card.notes ?: ""
            difficultySliderValue = (card.difficulty ?: 3).toFloat() - 1
        }
    }

    fun validateFields(): Boolean {
        frenchWordError = if (frenchWord.isBlank()) stringResource(id = R.string.french_word_empty_error) else null // Modified
        englishTranslationError = if (englishTranslation.isBlank()) stringResource(id = R.string.english_translation_empty_error) else null // Modified
        return frenchWordError == null && englishTranslationError == null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_card_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_cd))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (validateFields() && cardToEditState != null) {
                                val updatedCard = cardToEditState!!.copy(
                                    frenchWord = frenchWord.trim(),
                                    englishTranslation = englishTranslation.trim(),
                                    notes = notes.trim().ifEmpty { null }, // Ensure null if empty
                                    difficulty = difficultySliderValue.roundToInt() + 1 // Convert 0f-4f to 1-5
                                )
                                viewModel.updateCard(updatedCard) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message = stringResource(id = R.string.card_updated_successfully)) // Modified
                                    }
                                    onNavigateBack()
                                }
                            }
                        },
                        enabled = cardToEditState != null // Added: Disable button if card not loaded
                    ) {
                        Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.save_changes_to_card_cd))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cardToEditState == null) { // Added: Loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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

// All @Preview functions removed to fix syntax error and as they are non-essential.
