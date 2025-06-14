package com.example.ankizero.ui.shared

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.* // Import all from core
import androidx.compose.foundation.background // Added import
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.* // Import all from runtime
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier, // Keep modifier here
    targetBackgroundColor: Color? = null, // New parameter
    targetColor: Color? = null,
    triggerPulse: Boolean = false,
    style: TextStyle = LocalTextStyle.current, // style after specific color/background controls
    animationDelay: Int = 0
) {
    val charAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200, delayMillis = animationDelay, easing = FastOutSlowInEasing),
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

    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(isVisible, triggerPulse) {
        if (isVisible && triggerPulse) {
            // Start the pulse animation
            pulseScale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
            )
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150, delayMillis = 150, easing = FastOutLinearInEasing)
            )
        } else if (!triggerPulse) {
            // Reset pulse scale if trigger is false (e.g. on recomposition if no longer pulsing)
            // Also implicitly handles !isVisible case because of the condition in the `if` block
            pulseScale.snapTo(1f)
        }
    }

    // Determine the text style
    val currentStyle = when {
        targetBackgroundColor != null && targetColor == null -> {
            // Background is set, but no specific font color, use default from style
            style
        }
        targetColor != null -> {
            // Specific font color is set, use it (background might also be set)
            style.copy(color = targetColor)
        }
        else -> {
            // No background, no specific font color, use style as is
            style
        }
    }

    var currentModifier = modifier
    if (targetBackgroundColor != null) {
        currentModifier = currentModifier.then(Modifier.background(targetBackgroundColor))
    }

    Text(
        text = char.toString(),
        style = currentStyle, // Apply the potentially modified style
        modifier = currentModifier
            .graphicsLayer {
                alpha = charAlpha
                scaleX = charScale * pulseScale.value // Combine scales
                scaleY = charScale * pulseScale.value // Combine scales
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
