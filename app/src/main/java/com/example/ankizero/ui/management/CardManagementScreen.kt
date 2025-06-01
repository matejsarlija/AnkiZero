package com.example.ankizero.ui.management

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource // Added
import androidx.compose.ui.tooling.preview.Preview
import com.example.ankizero.R // Added for R.string
import androidx.compose.ui.unit.dp
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.ankizero.data.CardRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ankizero.data.entity.Flashcard // Assuming Flashcard entity is here
import java.time.LocalDate
import java.time.ZoneOffset

// Using the SortOption from the ViewModel's context or assuming it's defined globally/shared.
// If CardManagementViewModel.SortOption is `internal`, this screen can't directly use it.
// For this integration, we'll assume CardManagementViewModel.SortOption is accessible
// or we map screen-level SortOption to ViewModel's SortOption.
// Let's use a local definition that mirrors the ViewModel's one for now if direct import is an issue.
typealias ViewModelSortOption = com.example.ankizero.ui.management.SortOption // Alias to ViewModel's SortOption if accessible
// If not, redefine here:
/*
enum class SortOption {
    Alphabetical, Recent, Difficulty
}
*/
// For the sake of this example, we'll use the SortOption defined in this file,
// and assume the ViewModel's updateSortOrder can handle it or maps it.
// The plan was to make the screen adapt to the ViewModel's SortOption.

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardManagementScreen(
    // Updated to provide application and repository to the factory
    application: Application = LocalContext.current.applicationContext as Application,
    repository: CardRepository = CardRepository(application), // Assuming CardRepository can be created like this
    viewModel: CardManagementViewModel = viewModel(factory = CardManagementViewModelFactory(application, repository)),
    onNavigateToEditCard: (Long) -> Unit = {},
    onNavigateToCreateCard: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.card_management_screen_title)) }, // Updated
                actions = {
                    if (uiState.selectedCardIds.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.deleteSelectedCards() },
                            modifier = Modifier.testTag("DeleteSelectedCardsButton")
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.card_management_delete_selected_cards_cd))  // Updated
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateCard,
                modifier = Modifier.testTag("CreateCardFab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.card_management_create_new_card_cd)) // Updated
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {

            // Search Field
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("SearchTextField"),
                label = { Text(stringResource(id = R.string.card_management_search_label)) }, // Updated
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.card_management_search_icon_cd)) }  // Updated
            )

            // Sort Options
            SortOptionsRow(
                // Assuming CardManagementScreen.SortOption is compatible with ViewModel's
                selectedSortOption = uiState.sortOption, // This now comes from ViewModel
                onSortOptionSelect = { viewModel.updateSortOrder(it) }
            )

            // Cards List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.displayedCards.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                 Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.card_management_no_search_results_placeholder)) // Updated
                }
            } else if (uiState.displayedCards.isEmpty()) {
                 Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.card_management_no_cards_placeholder)) // Updated
                }
            }
            else {
                LazyColumn(
                modifier = Modifier.weight(1f).testTag("CardList"), // Added
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(uiState.displayedCards, key = { it.id }) { card ->
                        val isSelected = uiState.selectedCardIds.contains(card.id)
                        CardListItem(
                        modifier = Modifier.testTag("CardListItem_${card.id}"), // Added item specific tag
                            card = card,
                            isSelected = isSelected,
                            onItemClick = {
                                if (uiState.selectedCardIds.isNotEmpty()) {
                                    viewModel.toggleCardSelection(card.id)
                                } else {
                                    onNavigateToEditCard(card.id)
                                }
                            },
                            onItemLongClick = {
                                // haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Added haptic
                                viewModel.toggleCardSelection(card.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortOptionsRow(
    selectedSortOption: com.example.ankizero.ui.management.SortOption, // Explicitly use ViewModel's SortOption type
    onSortOptionSelect: (com.example.ankizero.ui.management.SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Helper to get display name for ViewModel's SortOption
    fun getDisplayName(option: com.example.ankizero.ui.management.SortOption): String {
        return when (option) {
            com.example.ankizero.ui.management.SortOption.Alphabetical -> "A-Z"
            com.example.ankizero.ui.management.SortOption.Recent -> "Recent"
            com.example.ankizero.ui.management.SortOption.Difficulty -> "Difficulty"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(id = R.string.card_management_sort_by_label), style = MaterialTheme.typography.titleSmall) // Updated

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(getDisplayName(selectedSortOption))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = stringResource(id = R.string.card_management_sort_options_cd)) // Updated
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                com.example.ankizero.ui.management.SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(getDisplayName(option)) },
                        onClick = {
                            onSortOptionSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardListItem(
    card: Flashcard,
    isSelected: Boolean,
    modifier: Modifier = Modifier, // Added modifier
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current // Get haptic feedback instance
    Card(
        modifier = modifier // Use passed modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Perform haptic on long press
                    onItemLongClick()
                }
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceVariant // M3 colors
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(card.frenchWord, style = MaterialTheme.typography.titleMedium)
                Text(card.englishTranslation, style = MaterialTheme.typography.bodyMedium)
            }
            if (isSelected) {
                // Checkbox is a more standard indicator for selection than just color
                Checkbox(
                    checked = true,
                    onCheckedChange = null, // Click handled by row
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}


// Dummy data for preview - ensure it matches Flashcard structure
val previewFlashcardsForScreen = List(5) { index ->
    Flashcard(
        id = index.toLong(),
        frenchWord = "Preview Mot ${index + 1}",
        englishTranslation = "Preview Word ${index + 1}",
        creationDate = LocalDate.now().minusDays(index.toLong()).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
        nextReviewDate = LocalDate.now().minusDays(index.toLong()).atStartOfDay().toEpochSecond(ZoneOffset.UTC), // Added: Default to creation date for preview
        difficulty = (index % 5) + 1
    )
}

@Preview(showBackground = true, name = "Card Management Screen - Light")
@Composable
fun CardManagementScreenPreviewLight() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val repository = CardRepository(application)
    MaterialTheme(colorScheme = lightColorScheme()) {
        CardManagementScreen(application = application, repository = repository) // Uses default VM from factory which has initial data
    }
}

@Preview(showBackground = true, name = "Card Management Screen - Dark")
@Composable
fun CardManagementScreenPreviewDark() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val repository = CardRepository(application)
    MaterialTheme(colorScheme = darkColorScheme()) {
        CardManagementScreen(application = application, repository = repository)
    }
}

// Preview for selected items - This is tricky as it depends on ViewModel state.
// A simple way is to show CardListItem directly.
@Preview(showBackground = true, name = "CardListItem - Selected")
@Composable
fun SelectedCardListItemPreview() {
    MaterialTheme {
        CardListItem(
            card = previewFlashcardsForScreen.first(),
            isSelected = true,
            onItemClick = {},
            onItemLongClick = {}
        )
    }
}
// The SortOption enum definition should be removed from this file if we are strictly using
// the one from the ViewModel's scope, or it should be ensured they are identical
// and potentially moved to a common `ui.model` or `domain` package.
// For now, the SortOptionsRow uses the ViewModel's SortOption via typealias or direct import.
// Removing the local enum SortOption(val displayName: String) if it was at the bottom of the file.
// enum class SortOption(val displayName: String) { ... } // REMOVE THIS LINE if it's a duplicate

/*
TODO: UI Test Scenarios for CardManagementScreen:
1.  **Initial List Display:**
    - Verify that a list of cards is displayed.
    - Check content of a few sample cards (French word, English translation).
2.  **Search Functionality:**
    - Type a search query in the TextField.
    - Verify the list filters correctly (e.g., only cards matching the query are shown).
    - Clear the search query.
    - Verify the full list is restored.
3.  **Sort Functionality:**
    - Open the sort dropdown menu.
    - Select "Alphabetical" sort. Verify list is sorted alphabetically by French word.
    - Select "Recent" sort. Verify list is sorted by creation date (newest first).
    - Select "Difficulty" sort. Verify list is sorted by difficulty.
4.  **Batch Selection and Deletion:**
    - Long-press on a card item to enter selection mode. Verify it's marked selected (e.g., checkbox appears/checked, background changes).
    - Select a few more cards by tapping them.
    - Verify the "Delete" icon button in the TopAppBar becomes visible/active.
    - Tap the "Delete" icon button.
    - Verify a confirmation dialog appears (if one is implemented as part of delete).
    - Confirm deletion.
    - Verify the selected cards are removed from the list.
    - Verify the "Delete" icon button is no longer visible/active.
5.  **Navigation to Create Card:**
    - Tap the FloatingActionButton (FAB).
    - Verify navigation to the CreateCardScreen occurs (check NavController's current route).
6.  **Navigation to Edit Card:**
    - Tap on a card item (when not in selection mode).
    - Verify navigation to the EditCardScreen occurs, passing the correct card ID.
7.  **Empty State / No Search Results:**
    - If the ViewModel provides an empty list of cards, verify an appropriate "No cards" message is shown.
    - If a search yields no results, verify a "No cards match your search" message is shown.
 (Requires Hilt, TestTags on Composables for item interaction and verification)
*/
