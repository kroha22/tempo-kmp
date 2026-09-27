package io.github.portfolio.tempo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val Ink = Color(0xFF293B3E)
internal val Muted = Color(0xFF667573)
internal val Green = Color(0xFF287765)
internal val GreenSoft = Color(0xFFE7F3EC)
internal val Sun = Color(0xFFFFC84F)
internal val Paper = Color(0xFFFFFAEC)
internal val ChildPaper = Color(0xFFFFF3CF)
internal val Purple = Color(0xFF73569A)

@Composable
internal fun TempoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Green,
            onPrimary = Color.White,
            secondary = Sun,
            onSecondary = Ink,
            background = Paper,
            onBackground = Ink,
            surface = Color.White,
            onSurface = Ink,
            outline = Color(0xFFD6DFDA),
        ),
        typography = Typography(),
        content = content,
    )
}
