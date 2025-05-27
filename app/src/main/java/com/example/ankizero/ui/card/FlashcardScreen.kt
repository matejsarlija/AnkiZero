package com.example.ankizero.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ankizero.data.entity.Flashcard
import kotlin.math.abs

@Composable
fun FlashcardScreen(
    viewModel: FlashcardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentCard = uiState.currentCard
    val progress = uiState.progress
    val total = uiState.total
    val isEmpty = currentCard == null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isEmpty) {
            Text("No cards due!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        } else {
            Flashcard3D(
                card = currentCard,
                flipped = uiState.flipped,
                onFlip = { viewModel.flipCard() },
                onSwipeLeft = { viewModel.nextCard() },
                onSwipeRight = { viewModel.prevCard() },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.6f)
            )
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { viewModel.rateNo() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("No") }
                    Button(
                        onClick = { viewModel.rateMemorized() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) { Text("Memorized") }
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { if (total > 0) progress / total.toFloat() else 0f },
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                Text("${progress + 1} / $total", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun Flashcard3D(
    card: Flashcard?,
    flipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationY by remember { mutableFloatStateOf(0f) }
    val animatedRotationY by animateFloatAsState(targetValue = if (flipped) 180f else 0f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .graphicsLayer {
                rotationY = animatedRotationY
                cameraDistance = 16 * density
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (rotationY > 100f) onSwipeLeft()
                        else if (rotationY < -100f) onSwipeRight()
                        rotationY = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        rotationY += dragAmount / 3
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onFlip() })
            },
        contentAlignment = Alignment.Center
    ) {
        if (animatedRotationY <= 90f) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(card?.french ?: "", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(card?.english ?: "", fontSize = 24.sp, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(8.dp))
                Text("Notes: ${card?.notes ?: ""}", fontSize = 14.sp)
            }
        }
    }
}
