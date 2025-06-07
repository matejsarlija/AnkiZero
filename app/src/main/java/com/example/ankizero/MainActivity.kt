package com.example.ankizero

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote // Changed from List
import androidx.compose.material.icons.filled.Style // Changed from Home
// Removed unused icons like Notifications, AddAPhoto, rounded.CameraAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text // Renamed M3Text to Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button // For Test Crash button
import androidx.compose.foundation.layout.Column // For layout with Test Crash button
import com.example.ankizero.util.AppLogger // For logging the crash simulation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy // Added
import androidx.navigation.NavGraph.Companion.findStartDestination // Added
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ankizero.util.AnalyticsHelper // Added for screen view logging
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository
import com.example.ankizero.ui.theme.AnkiZeroTheme
// Import screen composables
import com.example.ankizero.ui.card.FlashcardScreen
import com.example.ankizero.ui.management.CardManagementScreen
import com.example.ankizero.ui.management.CreateCardScreen
import com.example.ankizero.ui.management.EditCardScreen
// Import the constant for channel ID
//import com.example.ankizero.util.workers.STUDY_REMINDERS_CHANNEL_ID
//import com.example.ankizero.ui.navigation.Screen // Already present but good to ensure

// Using AppBottomNavItem from Navigation.kt (assumed to be updated with correct icons)
// If Navigation.kt's BottomNavItem is not updated, this might cause issues.
// For this diff, I'm assuming Navigation.kt's BottomNavItem was:
// sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String)
// object Flashcards : BottomNavItem(Screen.Flashcards, Icons.Filled.Style, "Review")
// etc.
// If it still has Int for iconResId, this part needs to be reconciled.
// For this diff, I'll use the locally defined AppBottomNavItem as per my previous step if Navigation.kt isn't updated.

// Re-defining AppBottomNavItem here to ensure correct icons are used for this diff.
sealed class AppBottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Flashcards : AppBottomNavItem(Screen.Flashcards, Icons.Filled.Style, "Review")
    object Management : AppBottomNavItem(Screen.Management, Icons.Filled.EditNote, "Manage")
    object OcrScan : AppBottomNavItem(Screen.OcrScan, Icons.Filled.CameraAlt, "Scan")
}

val appScreenBottomNavItems = listOf( // Renamed to avoid conflict if Navigation.kt also has 'appBottomNavItems'
    AppBottomNavItem.Flashcards,
    AppBottomNavItem.Management,
    AppBottomNavItem.OcrScan
)

val LocalFlashcardRepository = staticCompositionLocalOf<FlashcardRepository> {
    error("No FlashcardRepository provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Initialize Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG) // Example

        // TODO: Initialize Firebase Analytics
        // In class scope: private lateinit var firebaseAnalytics: FirebaseAnalytics
        // In onCreate: firebaseAnalytics = Firebase.analytics

        // createNotificationChannel() // Moved to AnkiZeroApplication

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FlashcardRepository(database.flashCardDao())

        setContent {
            AnkiZeroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnkiZeroApp(applicationContext, repository) // Pass applicationContext
                }
            }
        }

        // Conceptual Deep Link Handling
        val destinationRoute = intent.getStringExtra("destination_route")
        if (destinationRoute != null) {
            com.example.ankizero.util.AppLogger.i("MainActivity", "Deep link intent received for route: $destinationRoute")
            // Actual navigation would require NavController to be available and NavHost set up.
            // This might be handled by passing intent to AnkiZeroApp or using a LaunchedEffect in NavHost.
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Added for Scaffold if not already present
@Composable
fun AnkiZeroApp(applicationContext: Context, repository: FlashcardRepository) { // Added applicationContext
    val navController = rememberNavController()

    // Screen View Logging
    LaunchedEffect(navController) { // Use LaunchedEffect to add listener once
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val screenName = destination.route ?: "unknown_screen"
            AnalyticsHelper.logScreenView(applicationContext, screenName)
        }
    }

    CompositionLocalProvider(LocalFlashcardRepository provides repository) { // Wrap NavHost
        Scaffold(
            topBar = { // Adding a simple TopAppBar for the test crash button if needed, or place button directly in Column
                if (BuildConfig.DEBUG) { // Only show Test Crash button in debug builds
                // This is not ideal for a real topBar, just for quick access
                // A better place might be a debug drawer or specific debug screen
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        AppLogger.w("TestCrash", "Simulating a native crash via an unhandled exception.")
                        throw RuntimeException("Test Crash from AnkiZero App via Button")
                    }) {
                        Text("Test Crash (DEBUG ONLY)")
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                appScreenBottomNavItems.forEach { screen -> // Use the local appScreenBottomNavItems
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            if (screen.route == Screen.OcrScan) {
                                com.example.ankizero.util.AppLogger.d("Navigation", "OCR Scan clicked - Placeholder")
                                // Potentially show a Snackbar or Toast for placeholder
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Flashcards, // Use constant from Navigation.kt
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Flashcards) { // Use constant
                val application = LocalContext.current.applicationContext as Application
                val viewModel: com.example.ankizero.ui.card.FlashcardViewModel = viewModel(
                    factory = com.example.ankizero.ui.card.FlashcardViewModelFactory(application)
                )
                FlashcardScreen(viewModel = viewModel)
            }

            composable(Screen.Management) { // Use constant
                val application = LocalContext.current.applicationContext as Application
                // Get repository from CompositionLocal
                val flashcardRepository = LocalFlashcardRepository.current
                val viewModel: com.example.ankizero.ui.management.CardManagementViewModel = viewModel(
                    factory = com.example.ankizero.ui.management.CardManagementViewModelFactory(application, flashcardRepository) // Pass repository
                )
                CardManagementScreen(
                    viewModel = viewModel,
                    // Pass navigation actions using NavController and Screen constants
                    onNavigateToCreateCard = { navController.navigate(Screen.CreateCard) },
                    onNavigateToEditCard = { cardId ->
                        navController.navigate(Screen.getEditCardRouteWithArg(cardId))
                    }
                )
            }

            composable(Screen.OcrScan) { // Use constant for placeholder
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("OCR Scanning Screen (Placeholder)")
                }
            }

            composable(Screen.EditCardRoute, // Use constant
                arguments = listOf(navArgument(Screen.editCardArg) { type = NavType.LongType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getLong(Screen.editCardArg) ?: -1L
                // Card fetching logic from original file is complex and relies on ViewModel state.
                // For EditCardScreen, it expects a `cardToEdit` object.
                // The original logic was:
                // val cardViewModel: com.example.ankizero.ui.management.CardManagementViewModel = viewModel(factory = ...)
                // val card = cardViewModel.uiState.collectAsState().value.cards.find { c -> c.id == cardId }
                // This direct collection and find might be problematic if the list isn't ready.
                // For now, we'll pass a placeholder as per subtask, but this needs proper ViewModel handling.
                EditCardScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateCard) { // Use constant
                // Original had:
                // val viewModel: com.example.ankizero.ui.management.CardManagementViewModel = viewModel(...)
                // CreateCardScreen(onSave = { nc -> viewModel.createCard(nc) {navController.popBackStack()} }, onCancel = {...})
                // My CreateCardScreen uses onNavigateBack.
                CreateCardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Removed "notifications" route as it's not in the main scope of this task
        }
    }
}}

// Removed local NavItem data class as AppBottomNavItem is used.
// data class NavItem(val route: String, val icon: ImageVector, val label: String)

// Removed createNotificationChannel method as it's now in AnkiZeroApplication.kt