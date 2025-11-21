package com.pandorawear.wear.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.pandorawear.mobile.theme.BackgroundDark
import com.pandorawear.mobile.theme.BluePrimary
import com.pandorawear.mobile.theme.BluePrimaryDark
import com.pandorawear.mobile.theme.BlueSecondary
import com.pandorawear.mobile.theme.ErrorRed
import com.pandorawear.mobile.theme.SurfaceDark
import com.pandorawear.mobile.theme.SurfaceDarkLow
import com.pandorawear.mobile.theme.TextPrimary
import com.pandorawear.mobile.theme.TextSecondary

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,

    primaryContainer = BluePrimaryDark,
    onPrimaryContainer = Color.White,

    secondary = BlueSecondary,
    onSecondary = Color.White,

    background = SurfaceDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
