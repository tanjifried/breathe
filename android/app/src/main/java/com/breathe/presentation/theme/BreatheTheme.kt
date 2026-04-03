package com.breathe.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
  primary = BreatheBlue,
  secondary = BreatheMint,
  surface = BreatheSoft,
  onPrimary = BreatheSoft,
  onSurface = BreatheInk
)

private val DarkColors = darkColorScheme(
  primary = BreatheMint,
  secondary = BreatheBlue
)

@Composable
fun BreatheTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = LightColors,
    typography = BreatheTypography,
    content = content
  )
}
