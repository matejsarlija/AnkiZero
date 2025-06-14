package com.example.ankizero.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ankizero.ui.theme.AnkiZeroTheme

@Composable
fun FlippingWordRow(
    word: String,
    reveal: Boolean,
    modifier: Modifier = Modifier,
    letterAnimationDelayMs: Int = 80 // Base delay for cascading effect
) {
    // Build list: [null, ...letters..., null]
    val chars = listOf<Char?>(null) + word.toList() + listOf<Char?>(null)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        chars.forEachIndexed { index, char ->
            FlippingLetterRectangle(
                char = char,
                reveal = reveal,
                animationDelayMs = index * letterAnimationDelayMs,
                // Add a small padding between letter rectangles if desired
                modifier = Modifier.padding(horizontal = 1.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "FlippingWordRow Revealed")
@Composable
fun FlippingWordRowRevealedPreview() {
    AnkiZeroTheme {
        FlippingWordRow(word = "HELLO", reveal = true)
    }
}

@Preview(showBackground = true, name = "FlippingWordRow Hidden")
@Composable
fun FlippingWordRowHiddenPreview() {
    AnkiZeroTheme {
        FlippingWordRow(word = "WORLD", reveal = false)
    }
}

@Preview(showBackground = true, name = "FlippingWordRow Short Word")
@Composable
fun FlippingWordRowShortWordPreview() {
    AnkiZeroTheme {
        FlippingWordRow(word = "HI", reveal = true)
    }
}

@Preview(showBackground = true, name = "FlippingWordRow Dark Theme")
@Composable
fun FlippingWordRowDarkPreview() {
    AnkiZeroTheme(darkTheme = true) {
        FlippingWordRow(word = "DARK", reveal = true)
    }
}
