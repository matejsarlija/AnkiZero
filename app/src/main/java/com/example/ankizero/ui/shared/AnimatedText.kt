package com.example.ankizero.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.ankizero.ui.theme.AnkiZeroTheme
import com.example.ankizero.ui.theme.frenchCharacterColors // Import the map
import kotlinx.coroutines.delay

enum class TextAnimationType {
    TYPEWRITER, // Reveals character by character
    FADE_IN_ALL // Fades in the whole text at once (can use AnimatedFade or simple alpha)
    // Add other types as needed
}

@Composable
fun AnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    animationType: TextAnimationType = TextAnimationType.TYPEWRITER,
    isVisible: Boolean = true, // Controls overall visibility for fade-out or initial hide
    charRevealDelayMillis: Long = 80L,
    style: TextStyle = LocalTextStyle.current,
    testTagPrefix: String = "AnimatedTextChar"
) {
    var revealedLetterCount by remember(text, isVisible, animationType) { mutableStateOf(0) }
    var pulsingCharIndices by remember(text, isVisible, animationType) { mutableStateOf(emptySet<Int>()) } // New state

    LaunchedEffect(text, isVisible, animationType) {
        pulsingCharIndices = emptySet() // Reset pulsing state on relevant changes

        if (isVisible && animationType == TextAnimationType.TYPEWRITER) {
            revealedLetterCount = 0 // Reset for re-animation if text/visibility changes
            for (i in text.indices) {
                delay(charRevealDelayMillis)
                if (isVisible) { // Ensure still visible before updating count
                    revealedLetterCount = i + 1
                } else {
                    break // Stop animation if visibility changes mid-way
                }
            }
            // After typewriter animation completes
            if (isVisible && revealedLetterCount == text.length) {
                val specialIndices = text.mapIndexedNotNull { index, char ->
                    if (frenchCharacterColors.containsKey(char)) index else null
                }.toSet()
                if (specialIndices.isNotEmpty()) {
                    pulsingCharIndices = specialIndices
                }
            }
        } else if (!isVisible && animationType == TextAnimationType.TYPEWRITER) {
            revealedLetterCount = 0 // Hide all characters if overall visibility is false
        } else if (isVisible && animationType == TextAnimationType.FADE_IN_ALL) {
            revealedLetterCount = text.length // Show all characters for other animation types
        } else {
            revealedLetterCount = 0
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center, // Default, can be overridden by modifier
        verticalAlignment = Alignment.CenterVertically // Default, can be overridden by modifier
    ) {
        text.forEachIndexed { index, char ->
            val charIsVisible = when (animationType) {
                TextAnimationType.TYPEWRITER -> index < revealedLetterCount && isVisible
                TextAnimationType.FADE_IN_ALL -> isVisible // For FADE_IN_ALL, AnimatedCharacter will handle its own fade based on this
            }

            val charColor = frenchCharacterColors[char] // Get color from map
            val shouldPulse = pulsingCharIndices.contains(index) // Determine if char should pulse

            AnimatedCharacter(
                char = char,
                isVisible = charIsVisible,
                style = style,
                targetColor = charColor, // Pass the color
                triggerPulse = shouldPulse, // Pass the trigger
                // testTag can be more specific if needed, e.g., passed in
                modifier = Modifier.testTag("$testTagPrefix-$index")
            )
        }
    }
}

@Preview(showBackground = true, name = "AnimatedText Typewriter")
@Composable
fun AnimatedTextTypewriterPreview() {
    AnkiZeroTheme {
        AnimatedText(
            text = "Hello Preview",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Preview(showBackground = true, name = "AnimatedText FadeInAll")
@Composable
fun AnimatedTextFadeInAllPreview() {
    AnkiZeroTheme {
        AnimatedText(
            text = "Fade Me In",
            animationType = TextAnimationType.FADE_IN_ALL,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Preview(showBackground = true, name = "AnimatedText Typewriter Dark")
@Composable
fun AnimatedTextTypewriterDarkPreview() {
    AnkiZeroTheme(darkTheme = true) {
        AnimatedText(
            text = "Dark Mode Text",
            charRevealDelayMillis = 100L,
            style = MaterialTheme.typography.displaySmall.copy(
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.tertiary
            )
        )
    }
}

@Preview(showBackground = true, name = "AnimatedText Initially Hidden")
@Composable
fun AnimatedTextInitiallyHiddenPreview() {
    AnkiZeroTheme {
        AnimatedText(
            text = "Should not be visible",
            isVisible = false,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
