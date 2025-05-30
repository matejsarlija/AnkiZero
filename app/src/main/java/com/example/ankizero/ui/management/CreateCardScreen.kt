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
import com.example.ankizero.data.entity.Flashcard
import com.example.ankizero.R // Added
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    onNavigateBack: () -> Unit,
    // onSaveCard: (Flashcard) -> Unit // To be used when ViewModel is integrated
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

    fun validateFields(): Boolean {
        frenchWordError = if (frenchWord.isBlank()) "French word cannot be empty" else null
        englishTranslationError = if (englishTranslation.isBlank()) "English translation cannot be empty" else null
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
                                // ... card creation ...
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("SaveCardButton")
                    ) {
                        Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.save_new_card_cd)) // Placeholder, add to strings.xml
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
        CreateCardScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Create Card Screen - Dark")
@Composable
fun CreateCardScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        CreateCardScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Create Card Screen - Error State")
@Composable
fun CreateCardScreenErrorPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        // This preview is hard to show with current state logic without interaction.
        // We'd typically pass initial error states or use a helper.
        // For now, this will render the default empty screen.
        // To see errors, one would need to run on device/emulator and attempt to save empty.
        CreateCardScreen(onNavigateBack = {})
    }
}
