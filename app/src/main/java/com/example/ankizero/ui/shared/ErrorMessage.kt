package com.example.ankizero.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error // Standard error icon
import androidx.compose.material.icons.filled.Close // Standard dismiss icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null // Optional dismiss callback
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Add some vertical padding
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer, // Use error container color
            contentColor = MaterialTheme.colorScheme.onErrorContainer  // Text color on error container
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f) // Allow text to take available space
            )
            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss error"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Error Message Light")
@Composable
fun ErrorMessagePreviewLight() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ErrorMessage(message = "This is a sample error message.")
            Spacer(Modifier.height(10.dp))
            ErrorMessage(message = "This is a dismissible error message.", onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "Error Message Dark")
@Composable
fun ErrorMessagePreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ErrorMessage(message = "This is a sample error message in dark theme.")
            Spacer(Modifier.height(10.dp))
            ErrorMessage(message = "This is a dismissible error message in dark theme.", onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "Error Message Long Text")
@Composable
fun ErrorMessageLongTextPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ErrorMessage(
                message = "This is a very long error message to check how the text wrapping and layout behaves. It should wrap correctly within the card and not overflow, maintaining a good user experience even with extensive error details."
            )
            Spacer(Modifier.height(10.dp))
            ErrorMessage(
                message = "Another very long error message, this one is dismissible. Ensure the close button aligns correctly and the text still wraps properly.",
                onDismiss = {}
            )
        }
    }
}
