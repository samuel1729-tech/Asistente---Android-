package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyanPrimary,
    onPrimary = JarvisBackground,
    primaryContainer = JarvisSurfaceVariant,
    onPrimaryContainer = JarvisCyanPrimary,
    secondary = JarvisCyanSecondary,
    onSecondary = JarvisBackground,
    secondaryContainer = JarvisSurfaceElevated,
    onSecondaryContainer = JarvisCyanSecondary,
    tertiary = JarvisAccentBlue,
    onTertiary = JarvisTextPrimary,
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisCyanDim,
    outlineVariant = JarvisBorder,
    error = JarvisAccentRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always enforce JARVIS HUD theme
    content: @Composable () -> Unit
) {
    val colorScheme = JarvisColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
