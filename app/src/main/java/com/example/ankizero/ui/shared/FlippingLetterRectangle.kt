package com.example.ankizero.ui.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ankizero.ui.theme.AnkiZeroTheme

@Composable
fun FlippingLetterRectangle(
    char: Char?, // If null, show empty rectangle
    reveal: Boolean,
    animationDelayMs: Int = 0, // Default to 0 if not specified
    modifier: Modifier = Modifier
) {
    val targetRotation = if (reveal) 0f else 90f
    val rotationY by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 350, delayMillis = animationDelayMs),
        label = "rotationY"
    )
    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .width(36.dp)
            .height(72.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12 * density // Recommended by Material Design guidelines
            }
            .background(Color.LightGray, RoundedCornerShape(6.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (rotationY < 90f && char != null) {
            Text(char.toString(), style = MaterialTheme.typography.headlineMedium)
        }
        // else: empty rectangle for null chars or when the hidden side is facing
    }
}

@Preview(showBackground = true, name = "FlippingLetterRectangle Revealed")
@Composable
fun FlippingLetterRectangleRevealedPreview() {
    AnkiZeroTheme {
        FlippingLetterRectangle(
            char = 'A',
            reveal = true,
            animationDelayMs = 0
        )
    }
}

@Preview(showBackground = true, name = "FlippingLetterRectangle Hidden")
@Composable
fun FlippingLetterRectangleHiddenPreview() {
    AnkiZeroTheme {
        FlippingLetterRectangle(
            char = 'B',
            reveal = false,
            animationDelayMs = 0
        )
    }
}

@Preview(showBackground = true, name = "FlippingLetterRectangle Empty Revealed")
@Composable
fun FlippingLetterRectangleEmptyRevealedPreview() {
    AnkiZeroTheme {
        FlippingLetterRectangle(
            char = null, // Empty rectangle
            reveal = true,
            animationDelayMs = 0
        )
    }
}

@Preview(showBackground = true, name = "FlippingLetterRectangle Empty Hidden")
@Composable
fun FlippingLetterRectangleEmptyHiddenPreview() {
    AnkiZeroTheme {
        FlippingLetterRectangle(
            char = null, // Empty rectangle
            reveal = false,
            animationDelayMs = 0
        )
    }
}

@Preview(showBackground = true, name = "FlippingLetterRectangle Dark Revealed")
@Composable
fun FlippingLetterRectangleDarkPreview() {
    AnkiZeroTheme(darkTheme = true) {
        FlippingLetterRectangle(
            char = 'C',
            reveal = true,
            animationDelayMs = 0
        )
    }
}
