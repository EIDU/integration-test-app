package com.eidu.content.test.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = PrimaryColorDark,
    primaryVariant = AccentColor,
    secondary = AccentColor
)

private val LightColorPalette = lightColors(
    primary = PrimaryColor,
    primaryVariant = AccentColor,
    secondary = AccentColor,
    background = ColorBackground,
    onBackground = OnBackgroundColor
)

@Composable
fun EIDUContentTestAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}