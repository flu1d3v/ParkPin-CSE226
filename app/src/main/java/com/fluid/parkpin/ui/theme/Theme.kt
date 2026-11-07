package com.fluid.parkpin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = TealDark,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = TealAccent,
    onSecondary = TealDark,
    background = NearBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

@Composable
fun ParkPinTheme(content: @Composable () -> Unit) {
    // Dark-only theme with a single teal accent color.
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ParkPinTypography,
        content = content
    )
}