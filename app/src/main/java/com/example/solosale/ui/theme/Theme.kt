package com.example.solosale.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GeometricPrimary,
    onPrimary = Color.White,
    primaryContainer = GeometricPrimaryContainer,
    onPrimaryContainer = GeometricOnPrimaryContainer,
    secondary = GeometricSecondary,
    onSecondary = Color.White,
    secondaryContainer = GeometricSecondaryContainer,
    onSecondaryContainer = GeometricOnSecondaryContainer,
    tertiary = GeometricWarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = GeometricWarningContainer,
    onTertiaryContainer = Color(0xFF2C1500),
    background = GeometricBackground,
    onBackground = GeometricTextPrimary,
    surface = GeometricSurface,
    onSurface = GeometricTextPrimary,
    surfaceVariant = GeometricSurfaceVariant,
    onSurfaceVariant = GeometricTextSecondary,
    outline = GeometricBorder,
    outlineVariant = GeometricBorderLight,
    error = GeometricDueRed,
    onError = Color.White,
    errorContainer = GeometricDueRedContainer,
    onErrorContainer = GeometricDueRedOnContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGeometricPrimary,
    onPrimary = Color(0xFF003062),
    primaryContainer = DarkGeometricPrimaryContainer,
    onPrimaryContainer = DarkGeometricOnPrimaryContainer,
    secondary = Color(0xFF81D5E3),
    onSecondary = Color(0xFF00363E),
    secondaryContainer = Color(0xFF004E59),
    onSecondaryContainer = Color(0xFF9EEFFD),
    background = DarkGeometricBackground,
    onBackground = DarkGeometricTextPrimary,
    surface = DarkGeometricSurface,
    onSurface = DarkGeometricTextPrimary,
    surfaceVariant = DarkGeometricSurfaceVariant,
    onSurfaceVariant = DarkGeometricTextSecondary,
    outline = DarkGeometricBorder,
    outlineVariant = Color(0xFF32353E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun SoloSaleTheme(
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
