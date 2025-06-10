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
import androidx.compose.ui.graphics.Brush // Added for Brush
import androidx.compose.ui.graphics.graphicsLayer
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
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium // Changed from StiffnessLow
    )

    // Animation for Bottom Card (Card 1)
    val bottomCardRotationZ by animateFloatAsState(targetValue = if (isRevealed) 0f else 0f, animationSpec = springSpec, label = "bottomCardRotationZ")
    val bottomCardTranslationX by animateFloatAsState(targetValue = if (isRevealed) 300f else 0f, animationSpec = springSpec, label = "bottomCardTranslationX")
    val bottomCardTranslationY by animateFloatAsState(targetValue = if (isRevealed) 0f else 20f, animationSpec = springSpec, label = "bottomCardTranslationY")
    val bottomCardScale by animateFloatAsState(targetValue = if (isRevealed) 0.8f else 1f, animationSpec = springSpec, label = "bottomCardScale")
    val bottomCardAlpha by animateFloatAsState(targetValue = if (isRevealed) 0f else 1f, animationSpec = springSpec, label = "bottomCardAlpha")

    // Animation for Middle Card (Card 2)
    val middleCardRotationZ by animateFloatAsState(targetValue = if (isRevealed) 0f else -7f, animationSpec = springSpec, label = "middleCardRotationZ")
    val middleCardTranslationX by animateFloatAsState(targetValue = if (isRevealed) -300f else -15f, animationSpec = springSpec, label = "middleCardTranslationX")
    val middleCardTranslationY by animateFloatAsState(targetValue = if (isRevealed) 0f else -10f, animationSpec = springSpec, label = "middleCardTranslationY")
    val middleCardScale by animateFloatAsState(targetValue = if (isRevealed) 0.8f else 1f, animationSpec = springSpec, label = "middleCardScale")
    val middleCardAlpha by animateFloatAsState(targetValue = if (isRevealed) 0f else 1f, animationSpec = springSpec, label = "middleCardAlpha")

    // Animation for Top Card (Card 3 - becomes the main card)
    val topCardRotationZ by animateFloatAsState(targetValue = if (isRevealed) 0f else -14f, animationSpec = springSpec, label = "topCardRotationZ")
    val topCardTranslationX by animateFloatAsState(targetValue = if (isRevealed) 0f else 10f, animationSpec = springSpec, label = "topCardTranslationX")
    val topCardTranslationY by animateFloatAsState(targetValue = if (isRevealed) 0f else -40f, animationSpec = springSpec, label = "topCardTranslationY")
    val topCardScale by animateFloatAsState(targetValue = if (isRevealed) 1.1f else 1f, animationSpec = springSpec, label = "topCardScale")
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
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
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
                                MaterialTheme.colorScheme.secondaryContainer, // Different gradient for variety
                                MaterialTheme.colorScheme.primaryContainer
                            )
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
                                MaterialTheme.colorScheme.tertiaryContainer, // Yet another gradient
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            )
        }
    }
}
