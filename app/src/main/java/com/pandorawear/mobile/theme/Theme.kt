package com.pandorawear.mobile.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,

    primaryContainer = BluePrimaryDark,
    onPrimaryContainer = Color.White,

    secondary = BlueSecondary,
    onSecondary = Color.White,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceDarkLow,
    onSurfaceVariant = TextSecondary,

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
