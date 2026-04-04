package com.breathe.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.QuickUpdate
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.QuickUpdateRepository
import com.breathe.domain.repository.StatusRepository
import com.breathe.presentation.navigation.Screen
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
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.SectionTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
  val ownStatus: StatusLevel? = null,
  val partnerStatus: StatusLevel? = null,
  val wsConnected: Boolean = false,
  val recentUpdates: List<QuickUpdate> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
  statusRepository: StatusRepository,
  quickUpdateRepository: QuickUpdateRepository
) : ViewModel() {
  val uiState: StateFlow<HomeUiState> = combine(
    statusRepository.observeOwnStatus(),
    statusRepository.observePartnerStatus(),
    statusRepository.observeWsConnection(),
    quickUpdateRepository.observeRecentUpdates(limit = 2)
  ) { own, partner, connected, updates ->
    HomeUiState(
      ownStatus = own,
      partnerStatus = partner,
      wsConnected = connected,
      recentUpdates = updates
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

@Composable
fun HomeScreen(
  onNavigate: (String) -> Unit,
  viewModel: HomeViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Breathe",
    subtitle = "A softer command center for slowing things down before conflict starts steering the room.",
    showBottomNav = true,
    selectedBottomRoute = Screen.Home.route,
    onNavigate = onNavigate
  ) {
    HomeLiveStateCard(uiState = uiState)

    BreatheCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        SectionTitle("Regulation tools")
        Text(
          text = "IMMEDIATE ACTIONS",
          style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
          color = BreatheMutedInk
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ToolGridButton(
          title = "Status",
          icon = Icons.Rounded.Favorite,
          onClick = { onNavigate(Screen.Status.route) },
          modifier = Modifier.weight(1f)
        )
        ToolGridButton(
          title = "Calm",
          icon = Icons.Rounded.SelfImprovement,
          onClick = { onNavigate(Screen.Calm.route) },
          modifier = Modifier.weight(1f)
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ToolGridButton(
          title = "Timeout",
          icon = Icons.Rounded.Timer,
          onClick = { onNavigate(Screen.Timeout.route) },
          modifier = Modifier.weight(1f)
        )
        ToolGridButton(
          title = "Voice",
          icon = Icons.Rounded.Mic,
          onClick = { onNavigate(Screen.Voice.route) },
          modifier = Modifier.weight(1f)
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ToolGridButton(
          title = "Log",
          icon = Icons.Rounded.EditNote,
          onClick = { onNavigate(Screen.Log.route) },
          modifier = Modifier.weight(1f)
        )
        ToolGridButton(
          title = "Insights",
          icon = Icons.Rounded.AutoGraph,
          onClick = { onNavigate(Screen.Insights.route) },
          modifier = Modifier.weight(1f)
        )
      }
    }

    BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.16f)) {
      Icon(
        imageVector = Icons.Rounded.FormatQuote,
        contentDescription = null,
        tint = Color(0xFF775A00),
        modifier = Modifier.size(34.dp)
      )
      Text(
        text = "\"In the pause between stimulus and response lies our growth and our freedom.\"",
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
        color = Color(0xFF5A4300)
      )
      Text(
        text = "- Viktor Frankl",
        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
        color = Color(0xFF775A00)
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      BreatheCard(modifier = Modifier.weight(1f), containerColor = BreatheOverlay.copy(alpha = 0.55f)) {
        Text("Recent harmony", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheAccentStrong)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .background(BreatheCardSurface, RoundedCornerShape(18.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "82%",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = BreatheAccentStrong
          )
        }
      }

      BreatheCard(modifier = Modifier.weight(1f), containerColor = BreatheOverlay.copy(alpha = 0.55f)) {
        Text("Daily intent", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheAccentStrong)
        Text(
          text = "Active listening without fixing.",
          style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
          color = BreatheMutedInk
        )
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(BreatheAccent.copy(alpha = 0.55f), CircleShape)
              .border(2.dp, BreatheCanvas, CircleShape)
          )
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(BreatheYellow.copy(alpha = 0.6f), CircleShape)
              .border(2.dp, BreatheCanvas, CircleShape)
          )
        }
      }
    }
  }
}

@Composable
private fun HomeLiveStateCard(uiState: HomeUiState) {
  BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.74f)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "LIVE STATE",
          style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
          color = BreatheAccentStrong.copy(alpha = 0.66f)
        )
        Text(
          text = "Emotional Weather",
          style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
          color = BreatheInk
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(if (uiState.wsConnected) BreatheAccentStrong else BreatheBorder, CircleShape)
        )
        Text(
          text = if (uiState.wsConnected) "Realtime linked" else "Offline-first mode",
          style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
          color = BreatheAccentStrong.copy(alpha = 0.72f)
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      WeatherStatusCard(
        modifier = Modifier.weight(1f),
        heading = "You",
        status = uiState.ownStatus,
        primary = true
      )
      WeatherStatusCard(
        modifier = Modifier.weight(1f),
        heading = "Partner",
        status = uiState.partnerStatus,
        primary = false
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, BreatheBorder.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
        .background(BreatheCardSurface.copy(alpha = 0.54f), RoundedCornerShape(20.dp))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "QUICK UPDATES",
        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
        color = BreatheAccentStrong.copy(alpha = 0.58f)
      )

      buildDisplayUpdates(uiState.recentUpdates).forEach { item ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(BreatheCanvas.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(item.dotColor, CircleShape)
            )
            Text(
              text = item.label,
              style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
              color = BreatheInk,
              fontWeight = FontWeight.Medium
            )
          }
          Text(
            text = item.time,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = BreatheMutedInk
          )
        }
      }
    }
  }
}

@Composable
private fun WeatherStatusCard(
  heading: String,
  status: StatusLevel?,
  primary: Boolean,
  modifier: Modifier = Modifier
) {
  val icon = when (status) {
    StatusLevel.GREEN -> Icons.Rounded.Spa
    StatusLevel.YELLOW -> Icons.Rounded.Favorite
    StatusLevel.RED -> Icons.Rounded.Timer
    null -> if (primary) Icons.Rounded.Spa else Icons.Rounded.Favorite
  }
  val accent = when (status) {
    StatusLevel.GREEN -> BreatheGreen
    StatusLevel.YELLOW -> BreatheYellow
    StatusLevel.RED -> BreatheRed
    null -> if (primary) BreatheAccentStrong else BreatheYellow
  }
  val chipBackground = when (status) {
    StatusLevel.GREEN -> BreatheAccentStrong
    StatusLevel.YELLOW -> BreatheYellow.copy(alpha = 0.35f)
    StatusLevel.RED -> BreatheRed.copy(alpha = 0.18f)
    null -> BreatheBorder.copy(alpha = 0.45f)
  }
  val chipText = when (status) {
    StatusLevel.GREEN -> BreatheCanvas
    StatusLevel.YELLOW -> Color(0xFF5A4300)
    StatusLevel.RED -> Color(0xFF6F2D2D)
    null -> BreatheMutedInk
  }

  Column(
    modifier = modifier
      .background(BreatheCanvas.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
      .padding(horizontal = 16.dp, vertical = 18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = heading.uppercase(),
      style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
      color = BreatheAccentStrong.copy(alpha = 0.64f)
    )

    Box(
      modifier = Modifier
        .size(64.dp)
        .background(accent.copy(alpha = 0.14f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
    }

    Text(
      text = homeStatusLabel(status),
      modifier = Modifier
        .background(chipBackground, RoundedCornerShape(999.dp))
        .padding(horizontal = 14.dp, vertical = 8.dp),
      style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
      color = chipText,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun ToolGridButton(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .background(BreatheOverlay.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .background(BreatheCardSurface, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = title, tint = BreatheAccentStrong)
    }
    Text(title, color = BreatheMutedInk, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
  }
}

private data class HomeUpdateRow(
  val label: String,
  val time: String,
  val dotColor: Color
)

private fun buildDisplayUpdates(updates: List<QuickUpdate>): List<HomeUpdateRow> {
  val rows = updates.map { update ->
    val sender = if (update.isOwn) "You" else "Partner"
    HomeUpdateRow(
      label = buildString {
        append(sender)
        append(": ")
        append(update.message)
        if (!update.note.isNullOrBlank()) {
          append(" - ")
          append(update.note)
        }
      },
      time = relativeTime(update.createdAt),
      dotColor = if (update.isOwn) BreatheAccentStrong else BreatheYellow
    )
  }.toMutableList()

  while (rows.size < 2) {
    rows += HomeUpdateRow(
      label = if (rows.isEmpty()) {
        "You: No quick update yet"
      } else {
        "Partner: No reply yet"
      },
      time = "now",
      dotColor = BreatheBorder
    )
  }

  return rows.take(2)
}

private fun homeStatusLabel(status: StatusLevel?): String = when (status) {
  StatusLevel.GREEN -> "Open & Steady"
  StatusLevel.YELLOW -> "Tension Rising"
  StatusLevel.RED -> "Pause Needed"
  null -> "Unset"
}

private fun relativeTime(iso: String): String {
  val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return "now"
  val minutes = Duration.between(instant, Instant.now()).toMinutes().coerceAtLeast(0)
  return when {
    minutes < 1 -> "now"
    minutes < 60 -> "${minutes}m"
    else -> "${minutes / 60}h"
  }
}
