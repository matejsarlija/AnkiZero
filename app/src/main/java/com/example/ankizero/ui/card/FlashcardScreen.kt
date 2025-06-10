package com.example.ankizero.ui.card
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Style
import com.example.ankizero.ui.card.StackedCardsAnimation
import com.example.ankizero.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.app.Application
import com.example.ankizero.AnkiZeroApplication
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.abs

@Composable
fun FlashcardScreen(
    application: Application = LocalContext.current.applicationContext as Application,
    repository: FlashcardRepository
) {
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val viewModel: FlashcardViewModel = viewModel(factory = FlashcardViewModelFactory(applicationContext, repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundGradient")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backgroundOffset"
    )

    val isDark = isSystemInDarkTheme()
    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A2E).copy(alpha = 0.95f),
                Color(0xFF16213E).copy(alpha = 0.90f),
                Color(0xFF0F3460).copy(alpha = 0.85f)
            ),
            startY = backgroundOffset * 300f,
            endY = (backgroundOffset + 1f) * 300f
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8F9FA),
                Color(0xFFE9ECEF).copy(alpha = 0.8f),
                Color(0xFFDEE2E6).copy(alpha = 0.6f)
            ),
            startY = backgroundOffset * 200f,
            endY = (backgroundOffset + 1f) * 200f
        )
    }

    // Add Lifecycle observer
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentCard = uiState.currentCard
    val reviewMode = uiState.reviewMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        when {
            currentCard != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Enhanced progress indicator
                    EnhancedProgressIndicator(
                        progressText = uiState.progressText,
                        modifier = Modifier.testTag("ProgressText")
                    )

                    FlashcardView(
                        modifier = Modifier.testTag("FlashcardView"),
                        frontText = currentCard.frenchWord,
                        backText = currentCard.englishTranslation,
                        onSwipeLeft = { viewModel.showNextCard(moveForward = true) },
                        onSwipeRight = { viewModel.showNextCard(moveForward = false) }
                    )

                    EnhancedButtonRow(
                        onNoClick = { viewModel.processCardRating(isMemorized = false) },
                        onMemorizedClick = { viewModel.processCardRating(isMemorized = true) }
                    )
                }
            }
            reviewMode == ReviewMode.NONE && uiState.isDeckEmpty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StackedCardsAnimation(
                        onClick = { viewModel.startManualReview() }
                    )
                }
            }
            reviewMode == ReviewMode.MANUAL && uiState.isDeckEmpty -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No cards in your deck to review.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            uiState.isDeckEmpty -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No cards available for review in the current mode.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedProgressIndicator(
    progressText: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF2D3748).copy(alpha = 0.9f)
            else Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = progressText,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isDark) Color(0xFF90CDF4) else Color(0xFF2B6CB0)
        )
    }
}

@Composable
fun EnhancedButtonRow(
    onNoClick: () -> Unit,
    onMemorizedClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // No Button - Enhanced with ripple effect
        EnhancedButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onNoClick()
            },
            text = "No",
            containerColor = if (isDark) Color(0xFF742A2A) else Color(0xFFFC8181),
            contentColor = if (isDark) Color(0xFFFC8181) else Color.White,
            modifier = Modifier
                .weight(1f)
                .testTag("NoButton")
        )

        // Memorized Button - Enhanced with success colors
        EnhancedButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onMemorizedClick()
            },
            text = "Memorized",
            containerColor = if (isDark) Color(0xFF2F855A) else Color(0xFF48BB78),
            contentColor = Color.White,
            modifier = Modifier
                .weight(1f)
                .testTag("MemorizedButton")
        )
    }
}

@Composable
fun EnhancedButton(
    onClick: () -> Unit,
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false },
                    onHorizontalDrag = { _, _ -> }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
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

@Composable
fun FlashcardView(
    modifier: Modifier = Modifier,
    frontText: String,
    backText: String,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var flipped by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val isDark = isSystemInDarkTheme()

    // Enhanced flip animation with spring
    val animatedRotationY by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotationY"
    )

    // Dynamic card colors based on drag
    val dragProgress = (abs(dragOffsetX) / 300f).coerceIn(0f, 1f)
    val cardColor by animateColorAsState(
        targetValue = when {
            dragOffsetX > 50f -> if (isDark) Color(0xFF2F855A).copy(alpha = 0.3f) else Color(0xFF48BB78).copy(alpha = 0.2f)
            dragOffsetX < -50f -> if (isDark) Color(0xFF742A2A).copy(alpha = 0.3f) else Color(0xFFFC8181).copy(alpha = 0.2f)
            else -> if (isDark) MidGrayNoise.copy(alpha = 0.9f) else Color(0xFFF5F5F5) // Slightly less white
        },
        animationSpec = tween(200),
        label = "cardColor"
    )

    // Enhanced shadow and elevation
    val cardElevation by animateFloatAsState(
        targetValue = if (abs(dragOffsetX) > 50f) 12f else 8f,
        animationSpec = tween(200),
        label = "cardElevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth(0.88f)
            .aspectRatio(1.5f)
            .shadow(
                elevation = cardElevation.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.15f)
            )
            .graphicsLayer {
                this.rotationY = animatedRotationY
                this.cameraDistance = 16f * density
                this.translationX = dragOffsetX * 0.8f // Slightly dampened movement
                this.scaleX = 1f - (dragProgress * 0.05f) // Subtle scale effect
                this.scaleY = 1f - (dragProgress * 0.05f)
            }
            .clickable {
                flipped = !flipped
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragOffsetX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount
                    },
                    onDragEnd = {
                        val swipeThreshold = 120f
                        when {
                            dragOffsetX > swipeThreshold -> onSwipeRight()
                            dragOffsetX < -swipeThreshold -> onSwipeLeft()
                        }
                        dragOffsetX = 0f
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Subtle gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (isDark) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.3f)
                            ),
                            radius = 800f
                        )
                    )
            )

            // Card content
            if (animatedRotationY <= 90f || animatedRotationY >= 270f) {
                // Front side
                Text(
                    text = frontText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(24.dp)
                        .graphicsLayer { rotationY = 0f }
                        .testTag("CardFrontText")
                )
            } else {
                // Back side
                Text(
                    text = backText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(24.dp)
                        .graphicsLayer { rotationY = 180f }
                        .testTag("CardBackText")
                )
            }
        }
    }
}

// Preview functions remain the same but with enhanced visual appeal
@Preview(showBackground = true, name = "Flashcard Screen - Light")
@Composable
fun FlashcardScreenPreview() {
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val repository = applicationContext.repository

    MaterialTheme {
        FlashcardScreen(
            application = applicationContext,
            repository = repository
        )
    }
}

@Preview(showBackground = true, name = "Flashcard Screen - Dark")
@Composable
fun FlashcardScreenDarkPreview() {
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val repository = applicationContext.repository

    MaterialTheme(colorScheme = darkColorScheme()) {
        FlashcardScreen(
            application = applicationContext,
            repository = repository
        )
    }
}

@Preview(showBackground = true, name = "Enhanced Flashcard View")
@Composable
fun EnhancedFlashcardViewPreview() {
    MaterialTheme {
        FlashcardView(
            frontText = "Bonjour",
            backText = "Hello",
            onSwipeLeft = { },
            onSwipeRight = { }
        )
    }
}