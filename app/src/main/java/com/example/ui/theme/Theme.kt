package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BankGoldAccent,
    onPrimary = BankNavyDark,
    primaryContainer = BankNavyLight,
    onPrimaryContainer = Color.White,
    secondary = BankTealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0E3846),
    onSecondaryContainer = Color(0xFFA2EBF2),
    tertiary = BankCUPColor,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = BankNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = BankNavyLight,
    onPrimaryContainer = Color.White,
    secondary = BankTealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3F2F5),
    onSecondaryContainer = Color(0xFF00363C),
    tertiary = BankGoldAccent,
    onTertiary = BankNavyDark,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = BankError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
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
