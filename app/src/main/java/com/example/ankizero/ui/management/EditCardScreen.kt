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
import android.app.Application // Added
import androidx.compose.ui.platform.LocalContext // Added
import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.tooling.preview.Preview // Preview removed
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// import com.example.ankizero.data.entity.Flashcard // No longer directly manipulating Flashcard entity here
import com.example.ankizero.R
import com.example.ankizero.LocalFlashcardRepository // Added
// import kotlinx.coroutines.launch // Not directly needed for snackbar, can be handled by VM or navigation callback
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardId: Long,
    onNavigateBack: () -> Unit,
    // viewModel: CardManagementViewModel = viewModel() // Old way
) {
    val application = LocalContext.current.applicationContext as Application
    val flashcardRepository = LocalFlashcardRepository.current // Access using the correct import
    val viewModel: CardManagementViewModel = viewModel(
        factory = CardManagementViewModelFactory(application, flashcardRepository)
    )
    val editCardFormState by viewModel.editCardFormState.collectAsState()

    // val snackbarHostState = remember { SnackbarHostState() } // Keep if snackbar shown from here
    // val scope = rememberCoroutineScope() // Keep if snackbar shown from here

    LaunchedEffect(cardId, viewModel) {
        viewModel.loadCardForEditing(cardId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetEditCardFormState()
        }
    }

    Scaffold(
        // snackbarHost = { SnackbarHost(snackbarHostState) }, // Keep or remove based on snackbar handling
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
                            viewModel.saveEditedCard {
                                // Assuming onNavigateBack is the onSuccess action
                                onNavigateBack()
                                // If snackbar needs to be shown, it should be triggered here or by listening to a state from VM
                                // scope.launch {
                                //     snackbarHostState.showSnackbar(message = stringResource(id = R.string.card_updated_successfully))
                                // }
                            }
                        },
                        enabled = editCardFormState.id != null && !editCardFormState.isLoading // Enable if card is loaded
                    ) {
                        Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.save_changes_to_card_cd))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            editCardFormState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            editCardFormState.cardNotFound -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(id = R.string.card_not_found_error), // Ensure this string resource exists
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            editCardFormState.id != null -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editCardFormState.frenchWord,
                        onValueChange = { viewModel.updateEditFrenchWord(it) },
                        label = { Text("French Word*") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editCardFormState.frenchWordError != null,
                        singleLine = true
                    )
                    if (editCardFormState.frenchWordError != null) {
                        Text(editCardFormState.frenchWordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editCardFormState.englishTranslation,
                        onValueChange = { viewModel.updateEditEnglishTranslation(it) },
                        label = { Text("English Translation*") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editCardFormState.englishTranslationError != null,
                        singleLine = true
                    )
                    if (editCardFormState.englishTranslationError != null) {
                        Text(editCardFormState.englishTranslationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Optional Fields", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Example Sentence field could be added here if part of EditCardFormState
                    // OutlinedTextField(
                    //     value = editCardFormState.exampleSentence,
                    //     onValueChange = { viewModel.updateEditExampleSentence(it) },
                    //     label = { Text("Example Sentence") },
                    //     modifier = Modifier.fillMaxWidth(),
                    //     maxLines = 3
                    // )
                    // Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editCardFormState.notes,
                        onValueChange = { viewModel.updateEditNotes(it) },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Difficulty (1-5)", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = editCardFormState.difficulty,
                        onValueChange = { viewModel.updateEditDifficulty(it) },
                        valueRange = 0f..4f, // Represents 1 to 5
                        steps = 3, // 0, 1, 2, 3, 4 (5 steps)
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Selected: ${(editCardFormState.difficulty.roundToInt() + 1)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            // Optionally, handle the case where id is null but not loading and not cardNotFound (e.g. initial state before load)
            else -> {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading card details...") // Or some other placeholder
                }
            }
        }
    }
}

// Preview functions are typically removed for production code.
