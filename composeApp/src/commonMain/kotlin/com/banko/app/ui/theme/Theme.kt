package com.banko.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Darkmode_Primary,
    secondary = Darkmode_Secondary,
    tertiary = Darkmode_PrimaryVariant,
    background = Dark_Background,
    onBackground = Dark_On_Background,
)

private val LightColorScheme = lightColorScheme(
    primary = Brightmode_Primary,
    secondary = Brightmode_Secondary,
    tertiary = Brightmode_PrimaryVariant,
    background = Light_Background,
    onBackground = Light_On_Background,
)

@Composable
fun BankoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
