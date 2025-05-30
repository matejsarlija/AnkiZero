package com.example.ankizero.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) { // Added modifier parameter
    Box(
        modifier = modifier.fillMaxSize(), // Default to fillMaxSize if no modifier is passed that constraints size
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true, name = "Loading Indicator Default")
@Composable
fun LoadingIndicatorPreview() {
    MaterialTheme {
        LoadingIndicator()
    }
}

@Preview(showBackground = true, name = "Loading Indicator Sized")
@Composable
fun LoadingIndicatorSizedPreview() {
    MaterialTheme {
        LoadingIndicator(modifier = Modifier.size(100.dp)) // Example with a size modifier
    }
}
