package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemeStyle

fun buildColorScheme(themeStyle: ThemeStyle, isDark: Boolean): ColorScheme {
    return when (themeStyle) {
        ThemeStyle.SLATE_CYAN -> {
            if (isDark) {
                darkColorScheme(
                    primary = CyanPrimary,
                    onPrimary = Color.Black,
                    primaryContainer = SlateSurfaceVariant,
                    onPrimaryContainer = CyanAccent,
                    secondary = CyanSecondary,
                    onSecondary = Color.Black,
                    background = SlateDarkBg,
                    onBackground = Color(0xFFF1F5F9),
                    surface = SlateSurface,
                    onSurface = Color(0xFFF8FAFC),
                    surfaceVariant = SlateSurfaceVariant,
                    onSurfaceVariant = Color(0xFFCBD5E1),
                    error = Color(0xFFF87171)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF0284C7),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFE0F2FE),
                    onPrimaryContainer = Color(0xFF0369A1),
                    secondary = Color(0xFF0EA5E9),
                    onSecondary = Color.White,
                    background = LightBg,
                    onBackground = LightText,
                    surface = LightSurface,
                    onSurface = LightText,
                    surfaceVariant = LightSurfaceVariant,
                    onSurfaceVariant = LightTextSecondary,
                    error = Color(0xFFDC2626)
                )
            }
        }
        ThemeStyle.EMERALD_VAULT -> {
            if (isDark) {
                darkColorScheme(
                    primary = EmeraldPrimary,
                    onPrimary = Color.Black,
                    primaryContainer = EmeraldSurfaceVariant,
                    onPrimaryContainer = EmeraldSecondary,
                    secondary = EmeraldSecondary,
                    onSecondary = Color.Black,
                    background = EmeraldDarkBg,
                    onBackground = Color(0xFFECFDF5),
                    surface = EmeraldSurface,
                    onSurface = Color(0xFFF0FDF4),
                    surfaceVariant = EmeraldSurfaceVariant,
                    onSurfaceVariant = Color(0xFFA7F3D0)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF059669),
                    onPrimary = Color.White,
                    background = LightBg,
                    surface = LightSurface,
                    surfaceVariant = Color(0xFFD1FAE5)
                )
            }
        }
        ThemeStyle.MIDNIGHT_BLUE -> {
            if (isDark) {
                darkColorScheme(
                    primary = MidnightPrimary,
                    onPrimary = Color.White,
                    secondary = MidnightSecondary,
                    background = MidnightDarkBg,
                    surface = MidnightSurface,
                    surfaceVariant = MidnightSurfaceVariant,
                    onSurface = Color(0xFFF8FAFC)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF2563EB),
                    onPrimary = Color.White,
                    background = LightBg,
                    surface = LightSurface,
                    surfaceVariant = Color(0xFFDBEAFE)
                )
            }
        }
        ThemeStyle.SUNSET_AMBER -> {
            if (isDark) {
                darkColorScheme(
                    primary = AmberPrimary,
                    onPrimary = Color.Black,
                    secondary = AmberSecondary,
                    background = AmberDarkBg,
                    surface = AmberSurface,
                    surfaceVariant = AmberSurfaceVariant,
                    onSurface = Color(0xFFFEF3C7)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFD97706),
                    onPrimary = Color.White,
                    background = LightBg,
                    surface = LightSurface,
                    surfaceVariant = Color(0xFFFEF3C7)
                )
            }
        }
        ThemeStyle.AMOLED_DARK -> {
            darkColorScheme(
                primary = AmoledPrimary,
                onPrimary = Color.White,
                secondary = AmoledSecondary,
                background = AmoledDarkBg,
                surface = AmoledSurface,
                surfaceVariant = AmoledSurfaceVariant,
                onSurface = Color.White,
                onBackground = Color.White
            )
        }
    }
}

@Composable
fun ClrAppTheme(
    themeStyle: ThemeStyle = ThemeStyle.SLATE_CYAN,
    darkModeSetting: String = "DARK",
    content: @Composable () -> Unit
) {
    val isDark = when (darkModeSetting) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = buildColorScheme(themeStyle, isDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
