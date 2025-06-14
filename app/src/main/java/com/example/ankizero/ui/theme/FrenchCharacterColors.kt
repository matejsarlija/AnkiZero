package com.example.ankizero.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.ankizero.R // Added import

// Define the light theme background color directly
val frenchCharBackgroundLight = Color(0xFFE1BEE7)

val frenchCharacterColors: Map<Char, Color> = mapOf(
    'è' to frenchCharBackgroundLight,
    'é' to frenchCharBackgroundLight,
    'à' to frenchCharBackgroundLight
    // Add more characters here if they exist, using frenchCharBackgroundLight
)
