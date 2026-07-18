package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = CardLowestWhite,
    primaryContainer = PrimaryContainerGreen,
    onPrimaryContainer = OnPrimaryContainerGreen,
    secondary = SlateSecondary,
    onSecondary = CardLowestWhite,
    background = BackgroundLight,
    onBackground = DarkSlateNavy,
    surface = CardLowestWhite,
    onSurface = DarkSlateNavy,
    outline = OutlineBorder
)

private val ColorDarkSurface = androidx.compose.ui.graphics.Color(0xFF132235)

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryGreen,
    onPrimary = DarkSlateNavy,
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = CardLowestWhite,
    secondary = SlateSecondary,
    onSecondary = CardLowestWhite,
    background = DarkSlateNavy,
    onBackground = BackgroundLight,
    surface = ColorDarkSurface,
    onSurface = BackgroundLight,
    outline = SlateSecondary
)

@Composable
fun KonexMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
