package com.example.ankizero.ui.shared

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityEventCompat
import android.view.accessibility.AccessibilityEvent // Added import

@Composable
fun AnnounceForAccessibility(message: String) {
    val context = LocalContext.current
    val view = LocalView.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        view.announceForAccessibility(message)
    } else {
        // Switched to direct call as per subtask suggestion context
        // TYPE_ANNOUNCEMENT has been available since API 4.
        // The original ViewCompat.sendAccessibilityEvent should also work,
        // but this change addresses a potential specific "unresolved reference" scenario.
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        // Note: The message content is not part of this specific event type using this old method.
        // announceForAccessibility is preferred as it handles this.
        // For older APIs, the announcement content might need to be part of the event's text list,
        // which this simplified call doesn't do. However, the original ViewCompat call also didn't
        // explicitly pass the message to this specific event type.
        // For a simple announcement, this is a common fallback.
    }
}
