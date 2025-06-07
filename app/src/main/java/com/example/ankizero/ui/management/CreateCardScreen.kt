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
// import androidx.compose.ui.tooling.preview.Preview // Preview removed
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Added
// import com.example.ankizero.data.entity.Flashcard // No longer creating Flashcard here
import com.example.ankizero.R // Added
// import kotlinx.coroutines.launch // No longer needed for local scope
// import java.time.LocalDate // Not used
// import java.time.ZoneOffset // Not used
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardManagementViewModel = viewModel()
) {
    val createCardFormState by viewModel.createCardFormState.collectAsState()

    // val snackbarHostState = remember { SnackbarHostState() } // Keep if snackbar is shown from here, or move logic to VM if it controls snackbar
    // val scope = rememberCoroutineScope() // Keep if snackbar is shown from here

    // DisposableEffect to reset form state when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetCreateCardFormState()
        }
    }

    Scaffold(
        // snackbarHost = { SnackbarHost(snackbarHostState) }, // Keep or remove based on where snackbar is handled
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.create_card_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_cd))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveNewCard {
                                // Assuming onNavigateBack is the onSuccess action
                                onNavigateBack()
                                // If snackbar needs to be shown, it should be triggered here or by listening to a state from VM
                                // scope.launch {
                                // snackbarHostState.showSnackbar(message = stringResource(id = R.string.card_created_successfully))
                                // }
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
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = createCardFormState.frenchWord,
                onValueChange = { viewModel.updateNewFrenchWord(it) },
                label = { Text("French Word*") },
                modifier = Modifier.fillMaxWidth().testTag("FrenchWordTextField"),
                isError = createCardFormState.frenchWordError != null,
                singleLine = true
            )
            if (createCardFormState.frenchWordError != null) {
                Text(createCardFormState.frenchWordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("FrenchWordErrorText"))
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = createCardFormState.englishTranslation,
                onValueChange = { viewModel.updateNewEnglishTranslation(it) },
                label = { Text("English Translation*") },
                modifier = Modifier.fillMaxWidth().testTag("EnglishTranslationTextField"),
                isError = createCardFormState.englishTranslationError != null,
                singleLine = true
            )
            if (createCardFormState.englishTranslationError != null) {
                Text(createCardFormState.englishTranslationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("EnglishTranslationErrorText"))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Optional Fields", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = createCardFormState.exampleSentence,
                onValueChange = { viewModel.updateNewExampleSentence(it) },
                label = { Text("Example Sentence") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = createCardFormState.notes,
                onValueChange = { viewModel.updateNewNotes(it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Difficulty (1-5)", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = createCardFormState.difficulty,
                onValueChange = { viewModel.updateNewDifficulty(it) },
                valueRange = 0f..4f, // Represents 1 to 5. ViewModel stores 0f-4f, Flashcard entity stores 1-5
                steps = 3, // 0, 1, 2, 3, 4 (5 steps)
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Selected: ${(createCardFormState.difficulty.roundToInt() + 1)}", // UI display remains 1-5
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// Preview functions are typically removed or conditionalized for production code if they cause issues.
// For this refactoring, ensure no @Preview is left if it was causing errors.
