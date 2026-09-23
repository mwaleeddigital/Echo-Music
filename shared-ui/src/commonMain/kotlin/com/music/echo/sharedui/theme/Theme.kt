package com.music.echo.sharedui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NothingRed,
    onPrimary = Color.White,
    primaryContainer = NothingRedDim,
    onPrimaryContainer = Color.White,
    secondary = TextSecondary,
    onSecondary = PureBlack,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    background = PureBlack,
    onBackground = TextPrimary,
    outline = GlassBorder
)

@Composable
fun EchoTheme(
    pureBlack: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (pureBlack) {
        DarkColorScheme.copy(
            surface = PureBlack,
            background = PureBlack
        )
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EchoTypography,
        content = content
    )
}
