package com.example.smartflame.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = InteractiveTeal,
    onPrimary = DarkBackground,
    secondary = TextSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = ElevatedCardBackground,
    onSurface = TextPrimary,
    error = EmergencyRed,
    onError = TextPrimary,
    outline = CardBorder
)

@Composable
fun SmartFlameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // This is an emergency reporting app utilizing the dark design system tokens.
    // We enforce the dark theme palette to ensure consistent critical UI visibility.
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
