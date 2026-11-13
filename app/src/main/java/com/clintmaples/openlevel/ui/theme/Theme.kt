package com.clintmaples.openlevel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF090C11)
val InkRaised = Color(0xFF121822)
val InkCard = Color(0xFF1A2230)
val Line = Color(0xFF314158)
val Cyan = Color(0xFF7AD7FF)
val Foam = Color(0xFFF4F7FB)
val Mist = Color(0xFF93A0B5)
val Amber = Color(0xFFFFC857)
val LevelGreen = Color(0xFF3DDC97)
val Well = Color(0xFF0C121A)
val Bezel = Color(0xFF243044)

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1A1404),
    primaryContainer = Color(0xFF3A2E10),
    onPrimaryContainer = Amber,
    secondary = Cyan,
    onSecondary = Color(0xFF06202A),
    tertiary = LevelGreen,
    onTertiary = Color(0xFF042216),
    background = Ink,
    onBackground = Foam,
    surface = InkRaised,
    onSurface = Foam,
    surfaceVariant = InkCard,
    onSurfaceVariant = Mist,
    outline = Line,
)

private val AppTypography = Typography(
    labelSmall = TextStyle(
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)

@Composable
fun OpenLevelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content,
    )
}
