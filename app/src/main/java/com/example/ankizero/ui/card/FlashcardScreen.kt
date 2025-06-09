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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner // Added for Lifecycle
import androidx.compose.ui.platform.testTag // Added for testTag
import androidx.compose.material.icons.Icons // Added for Icons
import androidx.compose.material.icons.filled.Style // Added for Style icon (example for folded cards)
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.app.Application
import com.example.ankizero.AnkiZeroApplication
import com.example.ankizero.data.database.AppDatabase
import com.example.ankizero.data.repository.FlashcardRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Preferred
import androidx.compose.runtime.DisposableEffect // Added for Lifecycle
import androidx.lifecycle.Lifecycle // Added for Lifecycle
import androidx.lifecycle.LifecycleEventObserver // Added for Lifecycle
import androidx.lifecycle.LifecycleOwner // Added for Lifecycle

// If collectAsStateWithLifecycle causes issues for the worker, fallback to:
// import androidx.compose.runtime.collectAsState

@Composable
fun FlashcardScreen(
    // viewModel: FlashcardViewModel = viewModel(factory = FlashcardViewModelFactory()) // Original
    // Updated to provide application and repository to the factory
    application: Application = LocalContext.current.applicationContext as Application,
    repository: FlashcardRepository
) {
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    // repository is now a parameter, no need to fetch from applicationContext here for the screen itself
    val viewModel: FlashcardViewModel = viewModel(factory = FlashcardViewModelFactory(applicationContext, repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Add Lifecycle observer to call viewModel.onResume()
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

    val currentCard = uiState.currentCard // Moved after DisposableEffect
    val reviewMode = uiState.reviewMode // Get reviewMode

    // Conditional UI based on reviewMode and currentCard
    when {
        // Case 1: Cards are loaded for review (either normal or manual)
        currentCard != null -> {
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
        // Case 2: No cards due (initial state) and not in manual review mode yet
        reviewMode == ReviewMode.NONE && uiState.isDeckEmpty -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.startManualReview() } // Click to start manual review
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Style, // Placeholder for folded cards stack
                        contentDescription = "No cards due. Click to review all.",
                        modifier = Modifier.size(100.dp) // Adjust size as needed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No cards currently due.")
                    Text("Tap to review all cards.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        // Case 3: Manual review started but deck is empty, or normal review resulted in empty (already covered by currentCard != null)
        // This state specifically catches if manual review was initiated but no cards exist at all.
        reviewMode == ReviewMode.MANUAL && uiState.isDeckEmpty -> {
             Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No cards in your deck to review.")
            }
        }
        // Fallback for any other unhandled empty states (e.g. NORMAL mode, isDeckEmpty=true)
        uiState.isDeckEmpty -> {
             Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // This text might be the same as for NONE, but context is slightly different
                Text("No cards available for review in the current mode.")
            }
        }
        // Loading state or other intermediate states (optional)
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // Show a loading spinner
            }
        }
    }
}
        // Original structure:
        // if (uiState.isDeckEmpty || currentCard == null) {
        //     Box(
        //         modifier = Modifier.fillMaxSize(),
        //         contentAlignment = Alignment.Center
        //     ) {
        //         Text("No cards due for review.")
        //     }
        // } else {
        //     Column(
        //         modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            //         .fillMaxSize()
            //         .padding(16.dp),
            //     horizontalAlignment = Alignment.CenterHorizontally,
            //     verticalArrangement = Arrangement.SpaceAround
            // ) {
            //     FlashcardView(...)
            //     Text(uiState.progressText, ...)
            //     Row(...) { /* Buttons */ }
            // }
            // }
// } // This closing brace was removed due to the when expression replacing the if/else

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
    val animatedRotationY by animateFloatAsState(
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
                this.rotationY = animatedRotationY
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
            if (animatedRotationY <= 90f || animatedRotationY >= 270f) { // Show front when mostly front-facing
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
    // For previews, we need to provide Application and a Repository.
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val repository = applicationContext.repository

    MaterialTheme {
        FlashcardScreen(
            application = applicationContext,
            repository = repository
            // viewModel is created inside FlashcardScreen using the factory
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
            // viewModel is created inside FlashcardScreen using the factory
        )
    }
}

@Preview(showBackground = true, name = "Flashcard Screen - Empty State")
@Composable
fun FlashcardScreenEmptyPreview() {
    val applicationContext = LocalContext.current.applicationContext as AnkiZeroApplication
    val repository = applicationContext.repository // Use a real or fake empty repository for previews
    // To truly test the empty state, the ViewModel created within FlashcardScreen
    // should reflect an empty state when given this repository.

    MaterialTheme {
        FlashcardScreen(
            application = applicationContext,
            repository = repository
            // viewModel is created inside FlashcardScreen using the factory
        )
        // If the above doesn't show the empty state correctly due to ViewModel complexities
        // with preview data, then the direct Text approach might be a fallback for visual check:
        // Box(
        // modifier = Modifier.fillMaxSize(),
        // contentAlignment = Alignment.Center
        // ) {
        // Text("No cards due for review.")
        // }
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
