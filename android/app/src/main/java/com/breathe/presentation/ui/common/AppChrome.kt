package com.breathe.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.breathe.domain.model.StatusLevel
import com.breathe.presentation.theme.BreatheAccent
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.theme.BreatheCanvasSoft
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheGreen
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheRed
import com.breathe.presentation.theme.BreatheYellow

@Composable
fun AppScreen(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScopeWithSpacing.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(BreatheCanvas, BreatheOverlay)
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
      Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
      ColumnScopeWithSpacing.content()
    }
  }
}

object ColumnScopeWithSpacing

@Composable
fun BreatheCard(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, BreatheBorder.copy(alpha = 0.55f), RoundedCornerShape(22.dp)),
    colors = CardDefaults.cardColors(containerColor = BreatheCardSurface),
    shape = RoundedCornerShape(22.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Box(modifier = Modifier.padding(18.dp)) {
      content()
    }
  }
}

@Composable
fun StatusPill(label: String, status: StatusLevel?, modifier: Modifier = Modifier) {
  val (bg, fg) = when (status) {
    StatusLevel.GREEN -> BreatheGreen.copy(alpha = 0.15f) to Color(0xFF245739)
    StatusLevel.YELLOW -> BreatheYellow.copy(alpha = 0.20f) to Color(0xFF6B4F18)
    StatusLevel.RED -> BreatheRed.copy(alpha = 0.15f) to Color(0xFF6F2D2D)
    null -> BreatheAccent.copy(alpha = 0.08f) to BreatheMutedInk
  }

  Row(
    modifier = modifier
      .background(bg, RoundedCornerShape(999.dp))
      .border(1.dp, BreatheBorder.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(9.dp)
        .background(fg, CircleShape)
    )
    Text(label, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(label, style = MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
    Spacer(modifier = Modifier.height(4.dp))
    Text(value, style = MaterialTheme.typography.titleMedium, color = BreatheInk, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
fun SectionTitle(text: String) {
  Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = BreatheAccentStrong)
}
