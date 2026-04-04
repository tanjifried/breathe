package com.breathe.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
  primary = BreatheAccent,
  secondary = BreatheGreen,
  tertiary = BreatheYellow,
  background = BreatheCanvas,
  surface = BreatheCardSurface,
  surfaceVariant = BreatheOverlay,
  onPrimary = BreatheCanvas,
  onSurface = BreatheInk,
  onSurfaceVariant = BreatheMutedInk,
  outline = BreatheBorder
)

private val BreatheShapes = Shapes(
  small = RoundedCornerShape(18.dp),
  medium = RoundedCornerShape(26.dp),
  large = RoundedCornerShape(34.dp)
)

@Composable
fun BreatheTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = LightColors,
    typography = BreatheTypography,
    shapes = BreatheShapes,
    content = content
  )
}
