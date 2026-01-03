package com.pandorawear.mobile.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = BluePrimary,
    onPrimary = Color.White,

    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = Color.White,

    // Secondary (keep close to primary, but slightly softer)
    secondary = Color(0xFF7FA2FF),
    onSecondary = Color(0xFF0B0F14),

    secondaryContainer = Color(0xFF223560),
    onSecondaryContainer = TextPrimary,

    // Background / surface
    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,

    // Material 3 elevated containers (critical for “air”)
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,

    // Outlines
    outline = Outline,
    outlineVariant = OutlineVariant,

    // Error
    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun PandoraWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
