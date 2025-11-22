package com.pandorawear.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val DarkColorScheme = ColorScheme().copy(
    primary = BluePrimary,
    onPrimary = Color.White,

    secondary = BlueSecondary,
    onSecondary = Color.White,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceDarkHigh,

    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = Typography,
        colorScheme = DarkColorScheme,
        content = content
    )
}
