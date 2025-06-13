package com.example.ankizero.ui.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ankizero.ui.theme.AnkiZeroTheme

@Composable
fun PaperGrid(
    modifier: Modifier = Modifier,
    lineColor: Color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.2f),
    gridSize: Dp = 20.dp,
    strokeWidth: Dp = 0.5.dp,
    // Future enhancement: isDiagonal: Boolean = false
) {
    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidth.toPx()
        val gridSizePx = gridSize.toPx()

        // Vertical lines
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidthPx
            )
            x += gridSizePx
        }

        // Horizontal lines
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidthPx
            )
            y += gridSizePx
        }
    }
}

@Preview(showBackground = true, name = "PaperGrid Light")
@Composable
fun PaperGridPreviewLight() {
    AnkiZeroTheme {
        PaperGrid(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, name = "PaperGrid Dark")
@Composable
fun PaperGridPreviewDark() {
    AnkiZeroTheme(darkTheme = true) {
        PaperGrid(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, name = "PaperGrid Custom")
@Composable
fun PaperGridCustomPreview() {
    AnkiZeroTheme {
        PaperGrid(
            modifier = Modifier.fillMaxSize(),
            lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            gridSize = 30.dp,
            strokeWidth = 1.dp
        )
    }
}
