package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekBlueLight,
    onPrimary = SleekBlueDark,
    primaryContainer = SleekBlueActiveContainer,
    onPrimaryContainer = SleekBlueLight,
    secondary = SleekOrangeLight,
    onSecondary = SleekBg,
    background = SleekBg,
    onBackground = SleekTextPrimary,
    surface = SleekSurface,
    onSurface = SleekTextPrimary,
    surfaceVariant = SleekSurface,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekBorder,
    error = SleekError
)

private val LightColorScheme = lightColorScheme(
    primary = SleekBlueLight,
    onPrimary = SleekBlueDark,
    primaryContainer = SleekBlueActiveContainer,
    onPrimaryContainer = SleekBlueLight,
    secondary = SleekOrangeLight,
    onSecondary = SleekBg,
    background = SleekBg,
    onBackground = SleekTextPrimary,
    surface = SleekSurface,
    onSurface = SleekTextPrimary,
    surfaceVariant = SleekSurface,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekBorder,
    error = SleekError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce our unified Sleek Interface theme!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // Enforce our custom industrial dark theme!

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
