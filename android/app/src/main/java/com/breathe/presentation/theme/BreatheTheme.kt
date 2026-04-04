package com.breathe.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
  primary = BreatheAccent,
  secondary = BreatheGreen,
  tertiary = BreatheYellow,
  background = BreatheCanvas,
  surface = BreatheCardSurface,
  surfaceVariant = BreatheOverlay,
  onPrimary = BreatheCanvas,
  onSurface = BreatheInk
)

@Composable
fun BreatheTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = LightColors,
    typography = BreatheTypography,
    content = content
  )
}
