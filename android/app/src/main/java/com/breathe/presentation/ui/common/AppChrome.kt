package com.breathe.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.breathe.domain.model.StatusLevel
import com.breathe.presentation.theme.BreatheAccent
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCanvas
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
  content: @Composable ColumnScope.() -> Unit
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
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Breathe",
          style = MaterialTheme.typography.labelLarge,
          color = BreatheAccentStrong,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .background(BreatheCardSurface.copy(alpha = 0.88f), RoundedCornerShape(999.dp))
            .border(1.dp, BreatheBorder.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        )
        Text(
          text = title,
          style = MaterialTheme.typography.headlineLarge,
          color = BreatheInk,
          textAlign = TextAlign.Center
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyLarge,
          color = BreatheMutedInk,
          textAlign = TextAlign.Center
        )
      }

      content()

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Composable
fun BreatheCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, BreatheBorder.copy(alpha = 0.55f), RoundedCornerShape(28.dp)),
    colors = CardDefaults.cardColors(containerColor = BreatheCardSurface.copy(alpha = 0.94f)),
    shape = RoundedCornerShape(28.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      content()
    }
  }
}

@Composable
fun HeroCard(
  eyebrow: String,
  title: String,
  body: String,
  modifier: Modifier = Modifier,
  accent: Color = BreatheAccentStrong,
  content: @Composable ColumnScope.() -> Unit = {}
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    shape = RoundedCornerShape(32.dp)
  ) {
    Column(
      modifier = Modifier
        .background(
          Brush.linearGradient(
            colors = listOf(BreatheCardSurface, accent.copy(alpha = 0.14f))
          ),
          RoundedCornerShape(32.dp)
        )
        .border(1.dp, BreatheBorder.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = accent)
      Text(title, style = MaterialTheme.typography.headlineMedium, color = BreatheInk)
      Text(body, style = MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
      content()
    }
  }
}

@Composable
fun ActionTile(
  title: String,
  body: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  accent: Color = BreatheAccentStrong
) {
  Button(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(
      containerColor = accent.copy(alpha = 0.14f),
      contentColor = BreatheInk
    ),
    shape = RoundedCornerShape(24.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Text(body, style = MaterialTheme.typography.bodySmall, color = BreatheMutedInk)
    }
  }
}

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas),
    shape = RoundedCornerShape(999.dp)
  ) {
    Text(text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 4.dp))
  }
}

@Composable
fun SecondaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.fillMaxWidth(),
    colors = ButtonDefaults.outlinedButtonColors(contentColor = BreatheAccentStrong),
    border = androidx.compose.foundation.BorderStroke(1.dp, BreatheBorder),
    shape = RoundedCornerShape(999.dp)
  ) {
    Text(text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 4.dp))
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
