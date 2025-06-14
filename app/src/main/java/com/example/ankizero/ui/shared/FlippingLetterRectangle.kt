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
import androidx.compose.ui.graphics.Brush
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

    // Define colors for the brushed metal effect
    val metalColorStops = arrayOf(
        0.0f to Color(0xFFCCCCCC), // Lightest highlight
        0.2f to Color(0xFFAAAAAA), // Mid-tone
        0.4f to Color(0xFF888888), // Darker shadow
        0.6f to Color(0xFFAAAAAA), // Mid-tone
        0.8f to Color(0xFFCCCCCC), // Lightest highlight
        1.0f to Color(0xFFBBBBBB)  // Slightly less bright highlight at the edge
    )
    val brushedMetalGradient = Brush.linearGradient(
        colorStops = metalColorStops,
        //startY = 0f, endY = Float.POSITIVE_INFINITY // A vertical gradient that stretches
        // Or, for a more horizontal sheen:
        // startX = 0f, endX = Float.POSITIVE_INFINITY
        // Let's try a subtle diagonal sheen for a bit more dynamism
        // start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) - this might be too much
        // For a tall rectangle, a vertical gradient usually works well to simulate light from above/below
    )

    // Let's refine the gradient for a vertical brushed look on a tall rectangle
    val brushedMetalVerticalGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE0E0E0), // Top highlight
            Color(0xFFB0B0B0), // Mid
            Color(0xFF909090), // Darker part
            Color(0xFFA0A0A0), // Mid-dark
            Color(0xFFC0C0C0), // Lighter part
            Color(0xFFD0D0D0)  // Bottom highlight
        )
    )

    Box(
        modifier = modifier
            .width(36.dp)
            .height(72.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12 * density // Recommended by Material Design guidelines
            }
            .background(brushedMetalVerticalGradient, RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF666666), RoundedCornerShape(6.dp)),
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
