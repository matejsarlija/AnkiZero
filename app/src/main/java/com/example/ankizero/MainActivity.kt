package com.example.ankizero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository
import com.example.ankizero.ui.theme.AnkiZeroTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FlashcardRepository(database.flashcardDao())

        setContent {
            AnkiZeroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnkiZeroApp(repository)
                }
            }
        }
    }
}

@Composable
fun AnkiZeroApp(repository: FlashcardRepository) {
    val navController = rememberNavController()
    val navItems = listOf(
        NavItem("flashcard", Icons.Default.Home, "Review"),
        NavItem("management", Icons.Default.List, "Cards"),
        NavItem("ocr", Icons.Default.CameraAlt, "OCR"),
        NavItem("notifications", Icons.Default.Notifications, "Notifications")
    )
    val currentRoute = navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry?.destination?.route)

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute.value == item.route,
                        onClick = { navController.navigate(item.route) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.semantics { contentDescription = item.label }
                            )
                        },
                        label = { M3Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "flashcard",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Flashcard review screen
            composable("flashcard") {
                // Use viewModel() for proper lifecycle scoping
                val viewModel: com.example.ankizero.ui.card.FlashcardViewModel = viewModel(
                    factory = com.example.ankizero.ui.card.FlashcardViewModelFactory(repository)
                )
                com.example.ankizero.ui.card.FlashcardScreen(
                    viewModel = viewModel,
                    onEdit = { navController.navigate("edit/$it") }
                )
            }

            // Card management screen
            composable("management") {
                val viewModel: com.example.ankizero.ui.management.CardManagementViewModel = viewModel(
                    factory = com.example.ankizero.ui.management.CardManagementViewModelFactory(repository)
                )
                com.example.ankizero.ui.management.CardManagementScreen(
                    viewModel = viewModel,
                    onEdit = { navController.navigate("edit/$it") },
                    onCreate = { navController.navigate("create") }
                )
            }

            // OCR screen
            composable("ocr") {
                // TODO: Implement OcrScreen composable with repository and camera functionality
            }

            // Card edit screen
            composable(
                route = "edit/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) {
                val cardId = it.arguments?.getLong("cardId") ?: -1L
                val viewModel: com.example.ankizero.ui.management.CardManagementViewModel = viewModel(
                    factory = com.example.ankizero.ui.management.CardManagementViewModelFactory(repository)
                )
                val card = viewModel.uiState.collectAsState().value.cards.find { c -> c.id == cardId }
                if (card != null) {
                    com.example.ankizero.ui.management.EditCardScreen(
                        card = card,
                        onSave = { updated ->
                            // Save logic (update in repository)
                            viewModelScope.launch {
                                repository.updateFlashcard(updated)
                                navController.popBackStack()
                                viewModel.loadCards()
                            }
                        },
                        onCancel = { navController.popBackStack() }
                    )
                } else {
                    androidx.compose.material3.Text("Card not found.")
                }
            }

            // Card creation screen
            composable("create") {
                val viewModel: com.example.ankizero.ui.management.CardManagementViewModel = viewModel(
                    factory = com.example.ankizero.ui.management.CardManagementViewModelFactory(repository)
                )
                com.example.ankizero.ui.management.CreateCardScreen(
                    onSave = { newCard ->
                        viewModelScope.launch {
                            repository.insertFlashcard(newCard)
                            navController.popBackStack()
                            viewModel.loadCards()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            // Notifications screen (optional)
            composable("notifications") {
                // TODO: Implement NotificationsScreen composable with repository
            }
        }
    }
}

data class NavItem(val route: String, val icon: ImageVector, val label: String)