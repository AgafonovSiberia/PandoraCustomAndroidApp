package com.pandorawear.mobile.theme

import androidx.compose.ui.graphics.Color

/**
 * Calibrated dark palette to match reference:
 * - clean blue accents
 * - cool dark surfaces
 * - clear container ladder for Material 3
 */

// Clean blue accents (reference-like)
val BluePrimary = Color(0xFF3B6EF2)          // main accent (tabs, highlights)
val BluePrimaryContainer = Color(0xFF2B56D6) // containers (READY pill, engine btn base)

// Supporting neutrals (cool dark)
val BackgroundDark = Color(0xFF0B0F14)
val SurfaceDark = Color(0xFF11161D)
val SurfaceVariantDark = Color(0xFF171D26)

// Material 3 container ladder (cool + distinct steps)
val SurfaceContainerLowest = Color(0xFF0A0E13)
val SurfaceContainerLow = Color(0xFF111721)
val SurfaceContainer = Color(0xFF161D28)
val SurfaceContainerHigh = Color(0xFF1C2431)
val SurfaceContainerHighest = Color(0xFF243041)

// Text tones
val TextPrimary = Color(0xFFE9EDF6)
val TextSecondary = Color(0xFFB7C2D6)

// Outline / dividers
val Outline = Color(0xFF334055)
val OutlineVariant = Color(0xFF253043)

// Status / semantic
val ErrorRed = Color(0xFFFF4D4D)

// Optional subtle “success/online” point if you need later
val GreenOnline = Color(0xFF53D18C)
