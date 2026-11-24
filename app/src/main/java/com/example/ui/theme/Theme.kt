package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlockBlastColorScheme =
  darkColorScheme(
    primary = CrownGold,
    onPrimary = Color(0xFF281900),
    primaryContainer = RoyalBlueMid,
    onPrimaryContainer = ScoreWhite,
    secondary = Color(0xFF16C3FF),
    onSecondary = Color(0xFF002133),
    tertiary = ActionGreen,
    onTertiary = Color.White,
    background = RoyalBlueMid,
    onBackground = ScoreWhite,
    surface = DialogSurface,
    onSurface = ScoreWhite,
    surfaceVariant = DialogCardSurface,
    onSurfaceVariant = HudMuted,
    outline = BoardFrameBorder,
  )

@Composable
fun BlockBlastTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = BlockBlastColorScheme,
    typography = BlockBlastTypography,
    content = content,
  )
}
