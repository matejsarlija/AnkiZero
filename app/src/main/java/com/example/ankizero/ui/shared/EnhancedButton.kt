package com.example.ankizero.ui.shared

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding // Added for previews
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ankizero.ui.theme.AnkiZeroTheme
import com.example.ankizero.ui.theme.RetryRed // For preview
import com.example.ankizero.ui.theme.SuccessGreen // For preview
import com.example.ankizero.ui.theme.SuccessGreenDark // For preview

@Composable
fun EnhancedButton(
    onClick: () -> Unit,
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Haptic feedback
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .pointerInput(Unit) {
                // Using detectTapGestures might be more conventional for press state
                // but keeping detectHorizontalDragGestures as per original for now.
                detectHorizontalDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false },
                    onHorizontalDrag = { _, _ -> } // Consume drag events
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Preview(showBackground = true, name = "EnhancedButton Light")
@Composable
fun EnhancedButtonPreviewLight() {
    AnkiZeroTheme {
        EnhancedButton(
            onClick = { },
            text = "Click Me",
            containerColor = SuccessGreen,
            contentColor = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, name = "EnhancedButton Dark")
@Composable
fun EnhancedButtonPreviewDark() {
    AnkiZeroTheme(darkTheme = true) {
        EnhancedButton(
            onClick = { },
            text = "Click Me Dark",
            containerColor = SuccessGreenDark,
            contentColor = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, name = "EnhancedButton Disabled Light")
@Composable
fun EnhancedButtonDisabledPreviewLight() {
    AnkiZeroTheme {
        EnhancedButton(
            onClick = { },
            text = "Disabled",
            containerColor = RetryRed,
            contentColor = Color.White,
            enabled = false,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, name = "EnhancedButton Disabled Dark")
@Composable
fun EnhancedButtonDisabledPreviewDark() {
    AnkiZeroTheme(darkTheme = true) {
        EnhancedButton(
            onClick = { },
            text = "Disabled Dark",
            // Using a theme color for disabled state in dark mode for consistency
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            enabled = false,
            modifier = Modifier.padding(8.dp)
        )
    }
}
