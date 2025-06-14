package com.example.ankizero.ui.theme

import androidx.compose.ui.graphics.Color

// Define the HighlightColors data class
data class HighlightColors(val font: Color, val background: Color)

// Define the light theme background color directly
val frenchCharBackgroundLight = Color(0xFFE1BEE7)

val frenchCharacterColors: Map<Char, HighlightColors> = mapOf(
    'è' to HighlightColors(font = Color.Blue, background = frenchCharBackgroundLight),
    'é' to HighlightColors(font = Color.Yellow, background = frenchCharBackgroundLight),
    'à' to HighlightColors(font = Color.Red, background = frenchCharBackgroundLight)
    // Add more characters here if they were defined previously,
    // using frenchCharBackgroundLight for background and new/placeholder font colors.
)
