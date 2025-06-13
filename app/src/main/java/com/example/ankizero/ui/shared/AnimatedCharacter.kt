package com.example.ankizero.ui.shared

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.ankizero.ui.theme.AnkiZeroTheme

@Composable
fun AnimatedCharacter(
    char: Char,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    animationDelay: Int = 0 // Added for potential staggered animations, though current FlashcardView handles sequence
) {
    val charAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200, delayMillis = animationDelay, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "charAlpha"
    )
    val charScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
            // No explicit delay here, alpha delay should suffice for staggered effect
        ),
        label = "charScale"
    )

    Text(
        text = char.toString(),
        style = style,
        modifier = modifier
            .graphicsLayer {
                alpha = charAlpha
                scaleX = charScale
                scaleY = charScale
            }
    )
}

@Preview(showBackground = true, name = "AnimatedCharacter Visible")
@Composable
fun AnimatedCharacterVisiblePreview() {
    AnkiZeroTheme {
        AnimatedCharacter(
            char = 'A',
            isVisible = true,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Preview(showBackground = true, name = "AnimatedCharacter Hidden")
@Composable
fun AnimatedCharacterHiddenPreview() {
    AnkiZeroTheme {
        AnimatedCharacter(
            char = 'B',
            isVisible = false,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true, name = "AnimatedCharacter Visible Dark")
@Composable
fun AnimatedCharacterVisibleDarkPreview() {
    AnkiZeroTheme(darkTheme = true) {
        AnimatedCharacter(
            char = 'C',
            isVisible = true,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    }
}
