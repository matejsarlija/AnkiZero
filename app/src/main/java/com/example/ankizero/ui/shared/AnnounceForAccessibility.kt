package com.example.ankizero.ui.shared

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityEventCompat

@Composable
fun AnnounceForAccessibility(message: String) {
    val context = LocalContext.current
    val view = LocalView.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        view.announceForAccessibility(message)
    } else {
        ViewCompat.sendAccessibilityEvent(view, AccessibilityEventCompat.TYPE_ANNOUNCEMENT)
    }
}
