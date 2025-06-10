package com.example.ankizero.ui.card

import androidx.compose.animation.core.AnimationSpec // Add this import
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween // Added for tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush // Added for Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ankizero.ui.theme.DarkGrayNoise
import com.example.ankizero.ui.theme.LightGrayNoise
import com.example.ankizero.ui.theme.MidGrayNoise
import com.example.ankizero.ui.theme.SubtleDarkerGrayNoise
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

@Composable
fun StackedCardsAnimation(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isRevealed by remember { mutableStateOf(false) }
    val animationDurationMillis = 800L

    // Entry Animation
    var hasAppeared by remember { mutableStateOf(false) }
    val animatedEntryAlpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "entryAlpha"
    )
    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    val springSpec: AnimationSpec<Float> = spring( // Explicitly typed
        dampingRatio = Spring.DampingRatioLowBouncy, // Changed for less bounce
        stiffness = Spring.StiffnessMediumLow // Changed for softer animation
    )

    val tiltAngle = -15f
    val horizontalOffsetUnit = 30f

    // Animation for Bottom Card (Card 1) - zIndex 1f
    val bottomCardRotationZ by animateFloatAsState(
        targetValue = if (isRevealed) tiltAngle + 10f else tiltAngle,
        animationSpec = springSpec,
        label = "bottomCardRotationZ"
    )
    val bottomCardTranslationX by animateFloatAsState(
        targetValue = if (isRevealed) horizontalOffsetUnit * 4f else horizontalOffsetUnit * 2f,
        animationSpec = springSpec,
        label = "bottomCardTranslationX"
    )
    val bottomCardTranslationY by animateFloatAsState(
        targetValue = if (isRevealed) 100f else 10f, // Was 150f revealed, 20f initial
        animationSpec = springSpec,
        label = "bottomCardTranslationY"
    )
    val bottomCardScale by animateFloatAsState(
        targetValue = if (isRevealed) 0.7f else 0.9f, // Was 0.9f revealed, 1f initial
        animationSpec = springSpec,
        label = "bottomCardScale"
    )
    val bottomCardAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 0.85f, // Was 0f revealed, 1f initial
        animationSpec = springSpec,
        label = "bottomCardAlpha"
    )

    // Animation for Middle Card (Card 2) - zIndex 2f
    val middleCardRotationZ by animateFloatAsState(
        targetValue = if (isRevealed) tiltAngle - 10f else tiltAngle,
        animationSpec = springSpec,
        label = "middleCardRotationZ"
    )
    val middleCardTranslationX by animateFloatAsState(
        targetValue = if (isRevealed) horizontalOffsetUnit * 3f else horizontalOffsetUnit * 1f,
        animationSpec = springSpec,
        label = "middleCardTranslationX"
    )
    val middleCardTranslationY by animateFloatAsState(
        targetValue = if (isRevealed) 50f else 5f, // Was 75f revealed, 10f initial
        animationSpec = springSpec,
        label = "middleCardTranslationY"
    )
    val middleCardScale by animateFloatAsState(
        targetValue = if (isRevealed) 0.8f else 0.95f, // Was 0.95f revealed, 1f initial
        animationSpec = springSpec,
        label = "middleCardScale"
    )
    val middleCardAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 0.9f, // Was 0f revealed, 1f initial
        animationSpec = springSpec,
        label = "middleCardAlpha"
    )

    // Animation for Top Card (Card 3 - becomes the main card) - zIndex 3f
    val topCardRotationZ by animateFloatAsState(
        targetValue = if (isRevealed) 0f else tiltAngle, // Was 0f revealed, 0f initial
        animationSpec = springSpec,
        label = "topCardRotationZ"
    )
    val topCardTranslationX by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 0f,
        animationSpec = springSpec,
        label = "topCardTranslationX"
    )
    val topCardTranslationY by animateFloatAsState(
        targetValue = if (isRevealed) -15f else 0f, // Was -10f revealed, 0f initial
        animationSpec = springSpec,
        label = "topCardTranslationY"
    )
    val topCardScale by animateFloatAsState(
        targetValue = if (isRevealed) 1.05f else 1f,
        animationSpec = springSpec,
        label = "topCardScale"
    )
    val topCardAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 1f,
        animationSpec = springSpec,
        label = "topCardAlpha"
    )

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            delay(animationDurationMillis)
            onClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = animatedEntryAlpha } // Apply entry alpha to the main content box
            .clickable(
                enabled = !isRevealed && hasAppeared, // Only clickable if not revealed and entry animation done
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!isRevealed) {
                        isRevealed = true
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bottom Card
        Card(
            modifier = Modifier
                .size(width = 150.dp, height = 220.dp)
                .align(Alignment.Center)
                .zIndex(1f)
                .graphicsLayer {
                    rotationZ = bottomCardRotationZ
                    translationX = bottomCardTranslationX
                    translationY = bottomCardTranslationY
                    scaleX = bottomCardScale
                    scaleY = bottomCardScale
                    alpha = bottomCardAlpha
                },
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // CHANGED FROM matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                LightGrayNoise,
                                MidGrayNoise,
                                DarkGrayNoise,
                                MidGrayNoise
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            )
        }

        // Middle Card
        Card(
            modifier = Modifier
                .size(width = 150.dp, height = 220.dp)
                .align(Alignment.Center)
                .zIndex(2f)
                .graphicsLayer {
                    rotationZ = middleCardRotationZ
                    translationX = middleCardTranslationX
                    translationY = middleCardTranslationY
                    scaleX = middleCardScale
                    scaleY = middleCardScale
                    alpha = middleCardAlpha
                },
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // CHANGED FROM matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MidGrayNoise,
                                LightGrayNoise,
                                SubtleDarkerGrayNoise,
                                MidGrayNoise,
                                LightGrayNoise
                            ),
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    )
            )
        }

        // Top Card (becomes the main card)
        Card(
            modifier = Modifier
                .size(width = 150.dp, height = 220.dp)
                .align(Alignment.Center)
                .zIndex(3f)
                .graphicsLayer {
                    rotationZ = topCardRotationZ
                    translationX = topCardTranslationX
                    translationY = topCardTranslationY
                    scaleX = topCardScale
                    scaleY = topCardScale
                    alpha = topCardAlpha
                },
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // CHANGED FROM matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                DarkGrayNoise,
                                MidGrayNoise,
                                LightGrayNoise,
                                SubtleDarkerGrayNoise,
                                DarkGrayNoise,
                                MidGrayNoise
                            )
                            // Default horizontal gradient
                        )
                    )
            )
        }
    }
}
