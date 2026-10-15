package com.mycollege.schedule.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private fun schemeOf(c: MaColors) = if (c.isDark) darkColorScheme(
    primary = MaBrand.primary,
    onPrimary = Color.White,
    secondary = c.accent,
    background = c.bg,
    onBackground = c.fg,
    surface = c.surface,
    onSurface = c.fg,
    surfaceVariant = c.sunk,
    onSurfaceVariant = c.soft,
    outline = c.borderStrong,
    outlineVariant = c.border,
    error = MaBrand.red,
) else lightColorScheme(
    primary = MaBrand.primary,
    onPrimary = Color.White,
    secondary = c.accent,
    background = c.bg,
    onBackground = c.fg,
    surface = c.surface,
    onSurface = c.fg,
    surfaceVariant = c.sunk,
    onSurfaceVariant = c.soft,
    outline = c.borderStrong,
    outlineVariant = c.border,
    error = MaBrand.red,
)

val LocalAppDarkTheme = staticCompositionLocalOf { false }

/**
 * Тема приложения: токены «Моя Академия» для светлой и тёмной темы.
 * Динамические цвета Material You отключены — у приложения свой фирменный набор.
 */
@Composable
fun ScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkMaColors else LightMaColors
    CompositionLocalProvider(LocalMaColors provides colors) {
        MaterialTheme(
            colorScheme = schemeOf(colors),
            typography = Typography,
            content = content
        )
    }
}
