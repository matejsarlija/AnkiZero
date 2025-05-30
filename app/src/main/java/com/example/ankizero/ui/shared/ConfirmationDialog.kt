package com.example.ankizero.ui.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Using TextButton for dialog actions is common
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme // For preview theming

@Composable
fun ConfirmationDialog(
    showDialog: Boolean, // Added to explicitly control dialog visibility if needed by convention
    title: String,
    message: String, // Renamed from 'text' to 'message'
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Confirm", // Added with default
    dismissButtonText: String = "Cancel"   // Added with default
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Confirmation Dialog Light")
@Composable
fun ConfirmationDialogPreviewLight() {
    MaterialTheme { // Apply MaterialTheme for preview
        ConfirmationDialog(
            showDialog = true,
            title = "Confirm Action",
            message = "Are you sure you want to perform this action? This cannot be undone.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Confirmation Dialog Dark")
@Composable
fun ConfirmationDialogPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) { // Example of dark theme
        ConfirmationDialog(
            showDialog = true,
            title = "Delete Item",
            message = "Do you really want to delete 'Sample Item'? This is permanent.",
            onConfirm = {},
            onDismiss = {},
            confirmButtonText = "Delete",
            dismissButtonText = "Keep"
        )
    }
}

// Preview for when the dialog is not shown (renders nothing)
@Preview(showBackground = true, name = "Confirmation Dialog Hidden")
@Composable
fun ConfirmationDialogHiddenPreview() {
    MaterialTheme {
        ConfirmationDialog(
            showDialog = false,
            title = "Confirm Action",
            message = "This dialog should not be visible.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
