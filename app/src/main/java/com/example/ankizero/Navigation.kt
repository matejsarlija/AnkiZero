package com.example.ankizero

object Screen {
    const val Flashcards = "flashcards"
    const val Management = "management"
    const val OcrScan = "ocr_scan" // Placeholder
    const val CreateCard = "create_card"
    const val EditCard = "edit_card" // Base route for edit
    fun EditCard.routeWithArg(cardId: Long): String = "$EditCard/$cardId" // Keep this helper
    const val editCardArg = "cardId" // Keep this constant
    val EditCardRoute = "$EditCard/{$editCardArg}" // Keep this route pattern
}

// The BottomNavItem and bottomNavItems list are now managed in MainActivity.kt
// to use ImageVector directly. This section can be removed or commented out
// to avoid confusion and ensure MainActivity.kt is the source of truth for bottom nav items.

/*
sealed class BottomNavItem(val route: String, val iconResId: Int, val label: String) {
    object Flashcards : BottomNavItem(Screen.Flashcards, 0, "Review")
    object Management : BottomNavItem(Screen.Management, 0, "Manage")
    object OcrScan : BottomNavItem(Screen.OcrScan, 0, "Scan")
}

val bottomNavItems = listOf(
    BottomNavItem.Flashcards,
    BottomNavItem.Management,
    BottomNavItem.OcrScan
)
*/
