package com.example.ui.theme

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

private val SafeRideLightColorScheme = lightColorScheme(
    primary = SafeRideBlue,
    onPrimary = Color.White,
    primaryContainer = SafeRideBlueContainer,
    onPrimaryContainer = SafeRideOnBlueContainer,
    secondary = SafeRideIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF312E81),
    tertiary = SafeRideTeal,
    onTertiary = Color.White,
    background = SlateBackground,
    onBackground = SlateTextPrimary,
    surface = SlateCard,
    onSurface = SlateTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder,
    error = StatusDanger,
    onError = Color.White
)

private val SafeRideDarkAdminColorScheme = darkColorScheme(
    primary = SafeRideBlueLight,
    onPrimary = Color.White,
    primaryContainer = AdminDarkSurfaceVariant,
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = SafeRideIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3730A3),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = SafeRideTeal,
    onTertiary = Color.White,
    background = AdminDarkBg,
    onBackground = AdminDarkText,
    surface = AdminDarkSurface,
    onSurface = AdminDarkText,
    surfaceVariant = AdminDarkSurfaceVariant,
    onSurfaceVariant = AdminDarkTextMuted,
    outline = AdminDarkBorder,
    error = StatusDanger,
    onError = Color.White
)

@Composable
fun SafeRideTheme(
    isAdminTheme: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isAdminTheme -> SafeRideDarkAdminColorScheme
        darkTheme -> SafeRideDarkAdminColorScheme
        else -> SafeRideLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SafeRideTheme(isAdminTheme = false, darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
