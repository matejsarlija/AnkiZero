package com.example.ankizero.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.ankizero.data.entity.Flashcard

@Composable
fun EditCardScreen(
    card: Flashcard,
    onSave: (Flashcard) -> Unit,
    onCancel: () -> Unit
) {
    var french by remember { mutableStateOf(TextFieldValue(card.french)) }
    var english by remember { mutableStateOf(TextFieldValue(card.english)) }
    var notes by remember { mutableStateOf(TextFieldValue(card.notes ?: "")) }
    var error by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Edit Card", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = french,
            onValueChange = { french = it },
            label = { Text("French") },
            isError = french.text.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = english,
            onValueChange = { english = it },
            label = { Text("English") },
            isError = english.text.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onCancel) { Text("Cancel") }
            Button(
                onClick = {
                    if (french.text.isBlank() || english.text.isBlank()) {
                        error = "French and English fields cannot be empty."
                    } else {
                        onSave(card.copy(french = french.text, english = english.text, notes = notes.text))
                    }
                }
            ) { Text("Save") }
        }
    }
}

@Composable
fun CreateCardScreen(
    onSave: (Flashcard) -> Unit,
    onCancel: () -> Unit
) {
    var french by remember { mutableStateOf(TextFieldValue("")) }
    var english by remember { mutableStateOf(TextFieldValue("")) }
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    var error by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Create Card", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = french,
            onValueChange = { french = it },
            label = { Text("French") },
            isError = french.text.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = english,
            onValueChange = { english = it },
            label = { Text("English") },
            isError = english.text.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onCancel) { Text("Cancel") }
            Button(
                onClick = {
                    if (french.text.isBlank() || english.text.isBlank()) {
                        error = "French and English fields cannot be empty."
                    } else {
                        onSave(
                            Flashcard(
                                id = 0,
                                french = french.text,
                                english = english.text,
                                notes = notes.text,
                                interval = 1,
                                easeFactor = 2.5,
                                due = java.util.Date()
                            )
                        )
                    }
                }
            ) { Text("Save") }
        }
    }
}
