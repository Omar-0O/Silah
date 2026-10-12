package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sila 4dp/8dp consistent spacing grid system.
 */
data class SilaSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,       // Hairpin gaps, micro borders
    val xs: Dp = 4.dp,        // Text-to-icon gaps, tag insets
    val sm: Dp = 8.dp,        // Compact item spacing, icon padding
    val md: Dp = 12.dp,       // Standard internal card padding, grid spacing
    val lg: Dp = 16.dp,       // Screen horizontal margins, standard card padding
    val xl: Dp = 20.dp,       // Section gaps, hero inner padding
    val xxl: Dp = 24.dp,      // Sheet headers, major dialog padding
    val xxxl: Dp = 32.dp,     // Empty state illustrations, screen bottom anchors
    val section: Dp = 40.dp   // Major vertical flow separation
)

val LocalSilaSpacing = staticCompositionLocalOf { SilaSpacing() }

val androidx.compose.material3.MaterialTheme.spacing: SilaSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSilaSpacing.current
