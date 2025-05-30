package com.example.ankizero.ui.ocr

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas // Added import
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ankizero.ui.shared.ErrorMessage // Import shared components
import com.example.ankizero.ui.shared.LoadingIndicator // Import shared components
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class SimulatedBoundingBox(
    val id: Int,
    val text: String,
    val rect: Rect, // Compose Rect, not Android
    var isSelected: Boolean = false
)

data class Rect(val left: Float, val top: Float, val right: Float, val bottom: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen( // Removed default ViewModel instantiation
    viewModel: OcrViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current // Get haptic feedback instance
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        // Use CameraUtils for the initial check
        mutableStateOf(com.example.ankizero.util.CameraUtils.hasCameraPermission(context))
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.setImageUri(it)
                viewModel.processImage(it)
            }
        }
    )

    // This will hold the ImageCapture use case instance
    val imageCaptureUseCase = remember { mutableStateOf<ImageCapture?>(null) }


    // Simulated bounding boxes (can be removed if ML Kit provides real ones later)
    var boundingBoxes by remember {
        mutableStateOf(
            listOf(
                SimulatedBoundingBox(1, "Bonjour", Rect(0.1f, 0.2f, 0.4f, 0.3f)),
                SimulatedBoundingBox(2, "Monde", Rect(0.5f, 0.4f, 0.8f, 0.5f))
            )
        )
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Scan Text (OCR)") }) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hasCameraPermission) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Camera permission is required to use OCR.", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black)) {
                        CameraPreviewView( // Renamed for clarity
                            modifier = Modifier.fillMaxSize(),
                            onImageCaptureInstance = { imageCaptureUseCase.value = it }
                        )
                        // Overlay Bounding Boxes (visual simulation)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val (viewWidth, viewHeight) = size
                            boundingBoxes.forEach { boxData ->
                                val rect = androidx.compose.ui.geometry.Rect(
                                    left = boxData.rect.left * viewWidth,
                                    top = boxData.rect.top * viewHeight,
                                    right = boxData.rect.right * viewWidth,
                                    bottom = boxData.rect.bottom * viewHeight
                                )
                                drawRect(
                                    color = if (boxData.isSelected) Color.Green else Color.Red,
                                    topLeft = rect.topLeft,
                                    size = rect.size,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                        Box(modifier = Modifier.fillMaxSize().clickable { // Simplified selection
                            boundingBoxes = boundingBoxes.mapIndexed { index, box ->
                                box.copy(isSelected = index == 0 && !box.isSelected)
                            }
                        })
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        imageCaptureUseCase.value?.let { ic ->
                            com.example.ankizero.util.CameraUtils.takePhoto(
                                imageCapture = ic,
                                context = context,
                                executor = ContextCompat.getMainExecutor(context), // Or a custom executor
                                onImageCaptured = { uri ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Haptic on success
                                    viewModel.setImageUri(uri)
                                    viewModel.processImage(uri)
                                },
                                onError = { exception -> // Renamed error to exception for clarity
                                    com.example.ankizero.util.AppLogger.e("OcrScreen", "Photo capture error", exception) // Use AppLogger
                                    viewModel.setImageCaptureFailed(exception.message ?: "Unknown photo capture error")
                                }
                            )
                        } ?: com.example.ankizero.util.AppLogger.e("OcrScreen", "ImageCapture use case not available.") // Use AppLogger
                    }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Filled.Camera, contentDescription = "Capture photo for OCR", modifier = Modifier.fillMaxSize()) // CD Updated
                    }
                    IconButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = "Import image from gallery for OCR", modifier = Modifier.fillMaxSize()) // CD Updated
                    }
                }
            }

            if (uiState.isLoading) {
                LoadingIndicator(modifier = Modifier.matchParentSize()) // Cover the whole screen
            }

            uiState.errorMessage?.let { message ->
                // This ErrorMessage typically would be a Snackbar or a less intrusive element.
                // For now, placing it at the bottom.
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                    ErrorMessage(message = message, onDismiss = { viewModel.clearError() })
                }
            }
        }
    }

    if (uiState.showEditDialog) {
        RecognizedTextDialog(
            initialText = uiState.detectedText,
            onDismiss = { viewModel.showEditDialog(false) },
            onSave = { editedText -> viewModel.saveRecognizedText(editedText) }
        )
    }
}

// Removed the local takePhoto function as it's now in CameraUtils.kt

@Composable
fun CameraPreviewView( // Renamed from CameraPreview to avoid conflict if any other exists
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onImageCaptureInstance: (ImageCapture) -> Unit // Callback to pass ImageCapture instance
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageCapture = ImageCapture.Builder().build()
                onImageCaptureInstance(imageCapture) // Pass the instance

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    com.example.ankizero.util.AppLogger.e("OcrScreen.CameraPreviewView", "CameraX binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}

@Composable
fun RecognizedTextDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedText by remember(initialText) { mutableStateOf(initialText) } // Key change to update with initialText

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Recognized Text") },
        text = {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("(Captured Image Preview)") // Image URI could be passed here
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("Recognized Text") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }
        },
        confirmButton = { Button(onClick = { onSave(editedText) }) { Text("Save") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Preview(showBackground = true, name = "OCR Screen - Light (No Camera)")
@Composable
fun OcrScreenPreviewLight() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Text("OCR Screen Preview (Camera needs permission on device)", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true, name = "OCR Screen - Dark (No Camera)")
@Composable
fun OcrScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Text("OCR Screen Preview (Camera needs permission on device)", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true, name = "Recognized Text Dialog - Light")
@Composable
fun RecognizedTextDialogPreviewLight() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        RecognizedTextDialog(initialText = "Sample recognized text.", onDismiss = {}, onSave = {})
    }
}

/*
TODO: UI Test Scenarios for OcrScreen:
1.  **Permission Handling:**
    - Deny camera permission: Verify permission request UI is shown. Click "Grant Permission", allow via system dialog, verify camera preview appears.
    - Grant camera permission initially: Verify camera preview is shown directly.
2.  **Camera Preview Display:**
    - Verify the `CameraPreviewView` is displayed when permission is granted.
    - (Harder to test) Verify live camera feed is active (visual inspection or advanced techniques).
3.  **Simulated Bounding Boxes & Selection (Visual Only):**
    - Verify simulated bounding boxes are drawn over the preview.
    - Tap on a simulated box area (or the whole preview as per current simplified logic).
    - Verify the selected box changes appearance (e.g., border color).
4.  **Capture Photo Button:**
    - Click the "Capture Photo" button.
    - Verify `viewModel.processImage()` is called (requires test ViewModel or mocking).
    - Verify loading indicator is shown (`uiState.isLoading`).
    - On (simulated) success: Verify `RecognizedTextDialog` appears with simulated text.
    - On (simulated) failure: Verify an error message is shown.
5.  **Import from Gallery Button:**
    - Click the "Import from Gallery" button.
    - Verify the gallery launcher is triggered (Espresso Intents or UI Automator needed for this).
    - Simulate image selection.
    - Verify `viewModel.processImage()` is called with the URI.
    - Verify loading indicator and subsequent dialog/error message as with capture.
6.  **RecognizedTextDialog Interaction:**
    - When dialog appears (e.g., after simulated capture):
        - Verify it shows the (simulated) detected text.
        - Edit the text in the `OutlinedTextField`.
        - Click "Save". Verify `viewModel.saveRecognizedText()` is called with the edited text and dialog closes.
        - Click "Cancel". Verify dialog closes without saving.
7.  **Loading Indicator and Error Message Display:**
    - Trigger conditions for `isLoading` (e.g., during `processImage`). Verify `LoadingIndicator` is shown.
    - Trigger conditions for `errorMessage` (e.g., simulated error from `processImage`). Verify `ErrorMessage` is shown and can be dismissed.
 (Requires Hilt, TestTags, Espresso for gallery interaction, potentially mock CameraX/MLKit or use fakes)
*/

@Preview(showBackground = true, name = "Recognized Text Dialog - Dark")
@Composable
fun RecognizedTextDialogPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        RecognizedTextDialog(initialText = "Sample recognized text.", onDismiss = {}, onSave = {})
    }
}
