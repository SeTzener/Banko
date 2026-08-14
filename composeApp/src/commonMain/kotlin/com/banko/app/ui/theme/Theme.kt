package com.banko.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import banko.composeapp.generated.resources.Lobster_Regular
import banko.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

private val DarkColorScheme = darkColorScheme(
    primary = Darkmode_Primary,
    secondary = Darkmode_Secondary,
    tertiary = Darkmode_PrimaryVariant,
    surface = Dark_Surface,
    onSurface = Dark_On_Surface,
    onSurfaceVariant = Light_Surface
)

private val LightColorScheme = lightColorScheme(
    primary = Brightmode_Primary,
    secondary = Brightmode_Secondary,
    tertiary = Brightmode_PrimaryVariant,
    surface = Light_Surface,
    onSurface = Light_On_Surface,
    onSurfaceVariant = Dark_Surface
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
    val typography = Typography.copy(
        titleLarge = typography.titleLarge.copy(
            fontFamily = FontFamily(
                Font(Res.font.Lobster_Regular)
            )
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
