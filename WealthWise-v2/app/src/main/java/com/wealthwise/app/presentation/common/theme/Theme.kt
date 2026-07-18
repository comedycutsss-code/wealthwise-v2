package com.wealthwise.app.presentation.common.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Design language: "Ledger Ink" — inspired by a physical bound account ledger and fountain-pen
 * bookkeeping rather than generic fintech blue/teal. Ink-navy paper, warm parchment surfaces,
 * a single confident jade for growth/credit and a muted terracotta-red for debit — the two
 * colors a ledger pen would actually use.
 */

// Light — "Parchment"
val ParchmentBg = Color(0xFFF6F1E7)
val ParchmentSurface = Color(0xFFFFFFFF)
val InkNavy = Color(0xFF14213D)
val LedgerJade = Color(0xFF1F6F5C)
val LedgerRust = Color(0xFFB5482C)
val ParchmentOutline = Color(0xFFDDD2BC)

// Dark — "Midnight Ledger"
val MidnightBg = Color(0xFF0F1620)
val MidnightSurface = Color(0xFF17202C)
val JadeGlow = Color(0xFF4FD8B4)
val RustGlow = Color(0xFFE8785A)
val MidnightOutline = Color(0xFF2A3644)

private val LightColors = lightColorScheme(
    primary = LedgerJade,
    onPrimary = Color.White,
    secondary = LedgerRust,
    onSecondary = Color.White,
    background = ParchmentBg,
    onBackground = InkNavy,
    surface = ParchmentSurface,
    onSurface = InkNavy,
    surfaceVariant = Color(0xFFEFE7D6),
    outline = ParchmentOutline,
    error = LedgerRust
)

private val DarkColors = darkColorScheme(
    primary = JadeGlow,
    onPrimary = Color(0xFF00291E),
    secondary = RustGlow,
    onSecondary = Color(0xFF3A1200),
    background = MidnightBg,
    onBackground = Color(0xFFE8EAED),
    surface = MidnightSurface,
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF1E2833),
    outline = MidnightOutline,
    error = RustGlow
)

@Composable
fun WealthWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // opt-in only: dynamic color would override the Ledger Ink identity
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WealthWiseTypography,
        shapes = WealthWiseShapes,
        content = content
    )
}
