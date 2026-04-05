package com.breathe.presentation.ui.common

import com.breathe.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.breathe.domain.model.StatusLevel
import com.breathe.presentation.navigation.Screen
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

private data class BottomNavDestination(
  val route: String,
  val label: String,
  val icon: ImageVector
)

private val bottomNavDestinations = listOf(
  BottomNavDestination(Screen.Home.route, "Home", Icons.Rounded.Home),
  BottomNavDestination(Screen.Log.route, "Log", Icons.Rounded.EditNote),
  BottomNavDestination(Screen.Insights.route, "Insights", Icons.Rounded.AutoGraph),
  BottomNavDestination(Screen.Status.route, "Status", Icons.Rounded.Favorite)
)

private val SectionSpacing = 24.dp
private val CardSpacing = 24.dp
private val HeroPadding = 28.dp

@Composable
fun AppScreen(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  showBottomNav: Boolean = false,
  selectedBottomRoute: String? = null,
  onNavigate: (String) -> Unit = {},
  content: @Composable ColumnScope.() -> Unit
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = BreatheCanvas,
    topBar = { BreatheTopBar() },
    bottomBar = {
      if (showBottomNav) {
        BreatheBottomNav(selectedBottomRoute = selectedBottomRoute, onNavigate = onNavigate)
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(BreatheCanvas, BreatheCanvasSoft, BreatheOverlay)
          )
        )
        .verticalScroll(rememberScrollState())
        .padding(innerPadding)
        .padding(horizontal = 20.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineLarge,
          color = BreatheInk
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyLarge,
          color = BreatheMutedInk
        )
      }

      content()

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun BreatheTopBar() {
  Surface(
    color = BreatheCanvas.copy(alpha = 0.86f),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
        modifier = Modifier.width(76.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(BreatheOverlay, CircleShape)
            .border(1.dp, BreatheBorder.copy(alpha = 0.45f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "B",
            color = BreatheAccentStrong,
            style = MaterialTheme.typography.titleMedium,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Text(
        text = "Breathe",
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.headlineMedium,
        color = BreatheAccentStrong,
        fontStyle = FontStyle.Italic,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Box(
        modifier = Modifier.width(76.dp),
        contentAlignment = Alignment.CenterEnd
      ) {
        VersionPill()
      }
    }
  }
}

@Composable
private fun VersionPill() {
  Surface(
    color = BreatheOverlay.copy(alpha = 0.92f),
    shape = RoundedCornerShape(999.dp)
  ) {
    Text(
      text = "v${BuildConfig.VERSION_NAME}",
      modifier = Modifier
        .border(1.dp, BreatheBorder.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
        .padding(horizontal = 12.dp, vertical = 6.dp),
      style = MaterialTheme.typography.labelLarge,
      color = BreatheAccentStrong,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun BreatheBottomNav(selectedBottomRoute: String?, onNavigate: (String) -> Unit) {
  Surface(
    color = BreatheCanvas.copy(alpha = 0.88f),
    shape = RoundedCornerShape(topStart = 42.dp, topEnd = 42.dp),
    shadowElevation = 18.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 22.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      bottomNavDestinations.forEach { item ->
        val isSelected = item.route == selectedBottomRoute
        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(999.dp))
            .background(
              if (isSelected) BreatheOverlay else Color.Transparent,
              RoundedCornerShape(999.dp)
            )
            .heightIn(min = 56.dp)
            .clickable { onNavigate(item.route) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) BreatheAccentStrong else BreatheAccent.copy(alpha = 0.82f)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = item.label,
            color = if (isSelected) BreatheAccentStrong else BreatheAccent.copy(alpha = 0.82f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
fun AdaptiveTwoPane(
  modifier: Modifier = Modifier,
  breakpoint: Dp = 340.dp,
  spacing: Dp = 16.dp,
  first: @Composable (Modifier) -> Unit,
  second: @Composable (Modifier) -> Unit
) {
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    if (maxWidth < breakpoint) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
      ) {
        first(Modifier.fillMaxWidth())
        second(Modifier.fillMaxWidth())
      }
    } else {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        first(Modifier.weight(1f))
        second(Modifier.weight(1f))
      }
    }
  }
}

@Composable
fun AdaptiveThreePane(
  modifier: Modifier = Modifier,
  breakpoint: Dp = 680.dp,
  spacing: Dp = 16.dp,
  first: @Composable (Modifier) -> Unit,
  second: @Composable (Modifier) -> Unit,
  third: @Composable (Modifier) -> Unit
) {
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    if (maxWidth < breakpoint) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
      ) {
        first(Modifier.fillMaxWidth())
        second(Modifier.fillMaxWidth())
        third(Modifier.fillMaxWidth())
      }
    } else {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        first(Modifier.weight(1f))
        second(Modifier.weight(1f))
        third(Modifier.weight(1f))
      }
    }
  }
}

@Composable
fun BreatheCard(
  modifier: Modifier = Modifier,
  containerColor: Color = BreatheCardSurface.copy(alpha = 0.96f),
  contentPadding: PaddingValues = PaddingValues(CardSpacing),
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, BreatheBorder.copy(alpha = 0.45f), RoundedCornerShape(30.dp)),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    shape = RoundedCornerShape(30.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Column(
      modifier = Modifier.padding(contentPadding),
      verticalArrangement = Arrangement.spacedBy(12.dp)
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
  BreatheCard(
    modifier = modifier,
    containerColor = BreatheCardSurface,
    contentPadding = PaddingValues(HeroPadding)
  ) {
    Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = accent)
    Text(title, style = MaterialTheme.typography.headlineMedium, color = BreatheInk)
    Text(body, style = MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
    content()
  }
}

@Composable
fun PrimaryActionButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null
) {
  val shape = RoundedCornerShape(999.dp)
  val brush = if (enabled) {
    Brush.horizontalGradient(listOf(BreatheAccentStrong, BreatheAccent))
  } else {
    Brush.horizontalGradient(listOf(BreatheBorder.copy(alpha = 0.75f), BreatheBorder.copy(alpha = 0.75f)))
  }

  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 52.dp),
    shape = shape,
    colors = ButtonDefaults.buttonColors(
      containerColor = Color.Transparent,
      contentColor = BreatheCanvas,
      disabledContainerColor = Color.Transparent,
      disabledContentColor = BreatheMutedInk
    ),
    contentPadding = PaddingValues(0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(brush = brush, shape = shape)
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(10.dp))
      }
      Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
fun SecondaryActionButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null
) {
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 52.dp),
    colors = ButtonDefaults.outlinedButtonColors(contentColor = BreatheAccentStrong),
    border = androidx.compose.foundation.BorderStroke(1.dp, BreatheBorder),
    shape = RoundedCornerShape(999.dp),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.size(10.dp))
    }
    Text(text, style = MaterialTheme.typography.labelLarge)
  }
}

@Composable
fun StatusPill(label: String, status: StatusLevel?, modifier: Modifier = Modifier) {
  val (bg, fg) = when (status) {
    StatusLevel.GREEN -> BreatheGreen.copy(alpha = 0.16f) to Color(0xFF245739)
    StatusLevel.YELLOW -> BreatheYellow.copy(alpha = 0.23f) to Color(0xFF6B4F18)
    StatusLevel.RED -> BreatheRed.copy(alpha = 0.18f) to Color(0xFF6F2D2D)
    null -> BreatheAccent.copy(alpha = 0.10f) to BreatheMutedInk
  }

  Row(
    modifier = modifier
      .background(bg, RoundedCornerShape(999.dp))
      .border(1.dp, BreatheBorder.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
      .padding(horizontal = 14.dp, vertical = 9.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(fg, CircleShape)
    )
    Text(label, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
    Spacer(modifier = Modifier.height(6.dp))
    Text(value, style = MaterialTheme.typography.titleMedium, color = BreatheInk, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
fun SectionTitle(text: String) {
  Text(text, style = MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
}
