package com.example.ankizero.ui.card
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag // Added for testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Preferred

// If collectAsStateWithLifecycle causes issues for the worker, fallback to:
// import androidx.compose.runtime.collectAsState

@Composable
fun FlashcardScreen(
    viewModel: FlashcardViewModel = viewModel(factory = FlashcardViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentCard = uiState.currentCard

    if (uiState.isDeckEmpty || currentCard == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No cards due for review.")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            FlashcardView(
                modifier = Modifier.testTag("FlashcardView"), // Added testTag
                frontText = currentCard.frenchWord,
                backText = currentCard.englishTranslation,
                onSwipeLeft = { viewModel.showNextCard(moveForward = true) },
                onSwipeRight = { viewModel.showNextCard(moveForward = false) }
            )

            Text(uiState.progressText, modifier = Modifier.testTag("ProgressText")) // Added testTag

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val haptic = LocalHapticFeedback.current
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.processCardRating(isMemorized = false)
                    },
                    modifier = Modifier.testTag("NoButton") // Added testTag
                ) {
                    Text("No")
                }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.processCardRating(isMemorized = true)
                    },
                    modifier = Modifier.testTag("MemorizedButton") // Added testTag
                ) {
                    Text("Memorized")
                }
            }
        }
    }
}

/*
TODO: UI Test Scenarios for FlashcardScreen:
1.  **Initial Card Display:**
    - Verify that the front of the first due card is displayed (e.g., check for specific French word).
    - Verify progress text is correct (e.g., "Card 1/X").
2.  **Flip Animation and Content:**
    - Tap on the card.
    - Verify the card flips (animation itself is hard to verify in unit UI tests, focus on state change).
    - Verify the back of the card is displayed (e.g., corresponding English translation).
    - Tap again, verify it flips back to the front.
3.  **"Memorized" Button:**
    - Click the "Memorized" button.
    - Verify the current card changes (moves to the next card or finishes deck).
    - Verify progress text updates.
    - (Advanced) If using a test ViewModel/repository, verify SRS logic was triggered.
4.  **"No" Button:**
    - Click the "No" button.
    - Verify the current card changes.
    - Verify progress text updates.
    - (Advanced) Verify SRS logic was triggered.
5.  **Swipe Gestures:**
    - Swipe left on the card.
    - Verify the current card changes (moves to the next card).
    - Swipe right on the card.
    - Verify the current card changes (moves to the previous card).
6.  **Deck Completion:**
    - Navigate through all cards until the deck is empty/finished.
    - Verify the "Deck finished!" or "No cards due" message is displayed.
    - Verify control buttons might be hidden or disabled.
7.  **Empty State:**
    - If the ViewModel is initialized with no due cards.
    - Verify "No cards due for review." message is displayed.
8.  **Configuration Changes:** (e.g., screen rotation)
    - Verify current card and flip state are preserved.
    (Requires Hilt, TestTags on Composables, and potentially Espresso Idling Resources for real data)
*/

@Composable
fun FlashcardView(
    modifier: Modifier = Modifier, // Added modifier parameter
    frontText: String,
    backText: String,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var flipped by remember { mutableStateOf(false) }
    val rotationY by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600), // Slightly longer for smoother feel
        label = "rotationY"
    )

    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = modifier // Use passed modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(1.6f)
            .graphicsLayer {
                this.rotationY = rotationY
                // Improve perspective for 3D effect
                this.cameraDistance = 12f * density
                // Apply horizontal translation based on drag for swipe feedback
                this.translationX = dragOffsetX
            }
            .clickable { flipped = !flipped }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragOffsetX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount
                    },
                    onDragEnd = {
                        val swipeThreshold = 150f // Increased threshold for a more deliberate swipe
                        when {
                            dragOffsetX > swipeThreshold -> onSwipeRight() // Swiped right (show previous)
                            dragOffsetX < -swipeThreshold -> onSwipeLeft()  // Swiped left (show next)
                        }
                        dragOffsetX = 0f // Reset offset smoothly or immediately
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium // Using Material3 shapes
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Content changes based on the flip animation progress
            if (rotationY <= 90f || rotationY >= 270f) { // Show front when mostly front-facing
                Text(
                    text = frontText,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer { rotationY = 0f }
                        .testTag("CardFrontText") // Added testTag
                )
            } else { // Show back when mostly back-facing
                Text(
                    text = backText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer { this.rotationY = 180f }
                        .testTag("CardBackText") // Added testTag
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Flashcard Screen - Light")
@Composable
fun FlashcardScreenPreview() {
    MaterialTheme {
        FlashcardScreen(
            // Provide a preview ViewModel if necessary, or rely on default factory
            // For simplicity, we can use the default factory if it provides sample data
            viewModel = FlashcardViewModel() // Uses default data from ViewModel init
        )
    }
}

@Preview(showBackground = true, name = "Flashcard Screen - Dark")
@Composable
fun FlashcardScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        FlashcardScreen(viewModel = FlashcardViewModel())
    }
}

@Preview(showBackground = true, name = "Flashcard Screen - Empty State")
@Composable
fun FlashcardScreenEmptyPreview() {
    val emptyViewModel = FlashcardViewModel()
    // Simulate empty state for preview - this is a bit of a hack for preview
    // In a real scenario, the ViewModel would be initialized with no due cards
    emptyViewModel.uiState.value.let {
        // This direct modification is not how StateFlow is typically updated,
        // but for preview purposes to force an empty state, it's a workaround.
        // A better way would be to have the ViewModel expose a method to set an empty state for preview.
        // However, the current ViewModel loads data in init.
        // For this preview, we'll just show the "No cards" text directly if the default VM init results in empty.
        // Or, we can create a factory that provides a VM in an empty state.
    }
    // For this preview, let's assume the default FlashcardViewModel() might sometimes be empty
    // or we can just display the empty part of the FlashcardScreen logic:
    MaterialTheme {
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No cards due for review.")
        }
    }
}

@Preview(showBackground = true, name = "Flipped Flashcard View - Light")
@Composable
fun FlippedFlashcardViewPreview() {
    MaterialTheme {
        FlashcardView(
            frontText = "Bonjour (Preview)",
            backText = "Hello (Preview)",
            onSwipeLeft = { },
            onSwipeRight = { }
        )
        // To show it flipped, we'd need to manipulate the internal 'flipped' state,
        // which is tricky for a @Preview of a composable with internal remember {}.
        // A common way is to pass 'initialFlipped: Boolean = false' to FlashcardView for preview purposes.
    }
}
