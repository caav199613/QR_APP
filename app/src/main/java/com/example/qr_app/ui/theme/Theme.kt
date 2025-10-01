package com.example.qr_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores personalizados
private val LightColors = lightColorScheme(
    primary = Color(0xFF0D47A1),
    secondary = Color(0xFF1976D2),
    tertiary = Color(0xFF42A5F5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0D47A1),
    secondary = Color(0xFF1976D2),
    tertiary = Color(0xFF42A5F5)
)

// Tipografía por defecto
private val AppTypography = Typography()

@Composable
fun BusAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
