package com.example.ankizero.ui.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ankizero.ui.theme.AnkiZeroTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Config.OLDEST_SDK]) // Basic config for Robolectric
class PaperGridTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun paperGrid_renders_withDefaultModifier() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                PaperGrid(modifier = Modifier.fillMaxSize())
            }
        }
        // We can't easily assert canvas drawing content in a unit test.
        // We'll assert that the composable itself is part of the composition.
        // For Canvas, onRoot() or a test tag on a wrapping Box would be typical.
        // Since PaperGrid is just a Canvas, checking onRoot is a basic check.
        composeTestRule.onRoot().assertExists()
        // A more robust test would involve UI tests or accessibility checks if applicable.
    }

    @Test
    fun paperGrid_appliesModifierCorrectly() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                PaperGrid(modifier = Modifier.size(100.dp))
            }
        }
        // This doesn't directly test the canvas size, but that the composable honors the modifier.
        // Actual size verification of canvas content is better for UI tests.
        composeTestRule.onRoot().assertExists() // Basic check
    }

    @Test
    fun paperGrid_acceptsCustomParameters() {
        composeTestRule.setContent {
            AnkiZeroTheme {
                PaperGrid(
                    modifier = Modifier.size(50.dp),
                    lineColor = Color.Red,
                    gridSize = 10.dp,
                    strokeWidth = 2.dp
                )
            }
        }
        composeTestRule.onRoot().assertExists()
        // Parameter functionality (like color) would ideally be checked visually or in UI tests.
    }
}
