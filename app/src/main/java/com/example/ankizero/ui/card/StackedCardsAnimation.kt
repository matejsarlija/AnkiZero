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

    // Refined Initial State (isRevealed = false) for a neater stack
    // Animation for Bottom Card (Card 1) - Unraveling: moves down and fades
    val bottomCardRotationZ by animateFloatAsState(targetValue = if (isRevealed) 2f else 0f, animationSpec = springSpec, label = "bottomCardRotationZ")
    val bottomCardTranslationX by animateFloatAsState(targetValue = if (isRevealed) 10f else 0f, animationSpec = springSpec, label = "bottomCardTranslationX")
    val bottomCardTranslationY by animateFloatAsState(targetValue = if (isRevealed) 150f else 20f, animationSpec = springSpec, label = "bottomCardTranslationY")
    val bottomCardScale by animateFloatAsState(targetValue = if (isRevealed) 0.9f else 1f, animationSpec = springSpec, label = "bottomCardScale")
    val bottomCardAlpha by animateFloatAsState(targetValue = if (isRevealed) 0f else 1f, animationSpec = springSpec, label = "bottomCardAlpha")

    // Animation for Middle Card (Card 2) - Unraveling: moves down (less than bottom) and fades
    val middleCardRotationZ by animateFloatAsState(targetValue = if (isRevealed) -2f else 0f, animationSpec = springSpec, label = "middleCardRotationZ") // Initial rotation 0f
    val middleCardTranslationX by animateFloatAsState(targetValue = if (isRevealed) -5f else 0f, animationSpec = springSpec, label = "middleCardTranslationX") // Initial X 0f
    val middleCardTranslationY by animateFloatAsState(targetValue = if (isRevealed) 75f else 10f, animationSpec = springSpec, label = "middleCardTranslationY") // Initial Y 10f
    val middleCardScale by animateFloatAsState(targetValue = if (isRevealed) 0.95f else 1f, animationSpec = springSpec, label = "middleCardScale")
    val middleCardAlpha by animateFloatAsState(targetValue = if (isRevealed) 0f else 1f, animationSpec = springSpec, label = "middleCardAlpha")

    // Animation for Top Card (Card 3 - becomes the main card) - Unraveling: lifts slightly and scales up
    val topCardRotationZ by animateFloatAsState(targetValue = if (isRevealed) 0f else 0f, animationSpec = springSpec, label = "topCardRotationZ") // Initial rotation 0f
    val topCardTranslationX by animateFloatAsState(targetValue = if (isRevealed) 0f else 0f, animationSpec = springSpec, label = "topCardTranslationX") // Initial X 0f
    val topCardTranslationY by animateFloatAsState(targetValue = if (isRevealed) -10f else 0f, animationSpec = springSpec, label = "topCardTranslationY") // Initial Y 0f
    val topCardScale by animateFloatAsState(targetValue = if (isRevealed) 1.05f else 1f, animationSpec = springSpec, label = "topCardScale")
    val topCardAlpha by animateFloatAsState(targetValue = if (isRevealed) 1f else 1f, animationSpec = springSpec, label = "topCardAlpha")

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
