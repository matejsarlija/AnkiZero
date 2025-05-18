package com.example.ankizero.ui.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.ankizero.data.entity.Flashcard

@Composable
fun CardManagementScreen(
    viewModel: CardManagementViewModel,
    onEdit: (Long) -> Unit,
    onCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf(TextFieldValue("")) }
    var sortOption by remember { mutableStateOf(SortOption.ALPHABETICAL) }
    val selected = uiState.selected

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.search(it.text)
                },
                label = { Text("Search French word") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            DropdownMenu(
                expanded = uiState.sortMenuExpanded,
                onDismissRequest = { viewModel.setSortMenuExpanded(false) }
            ) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            sortOption = option
                            viewModel.sort(option)
                            viewModel.setSortMenuExpanded(false)
                        }
                    )
                }
            }
            Button(onClick = { viewModel.setSortMenuExpanded(true) }) {
                Text(sortOption.displayName)
            }
        }
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onCreate) { Text("Add Card") }
            if (selected.isNotEmpty()) {
                Button(
                    onClick = { viewModel.deleteSelected() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete Selected") }
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            items(uiState.cards) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { viewModel.toggleSelect(card.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected.contains(card.id)) Color.LightGray else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(card.french, style = MaterialTheme.typography.titleMedium)
                            Text(card.english, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { onEdit(card.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            }
        }
    }
}

enum class SortOption(val displayName: String) {
    ALPHABETICAL("A-Z"),
    RECENT("Recent"),
    DIFFICULTY("Difficulty")
}
