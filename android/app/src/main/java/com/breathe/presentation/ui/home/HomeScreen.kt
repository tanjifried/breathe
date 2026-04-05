package com.breathe.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.breathe.presentation.ui.common.AdaptiveTwoPane
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
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "IMMEDIATE ACTIONS",
          style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
          color = BreatheMutedInk
        )
        SectionTitle("Regulation tools")
      }

      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
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
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
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
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
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
    }

    BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.16f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
      ) {
        Icon(
          imageVector = Icons.Rounded.FormatQuote,
          contentDescription = null,
          tint = Color(0xFF775A00),
          modifier = Modifier.padding(top = 2.dp).size(32.dp)
        )
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "In the pause between stimulus and response lies our growth and our freedom.",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(lineHeight = 40.sp),
            color = Color(0xFF5A4300),
            fontStyle = FontStyle.Italic
          )
          Text(
            text = "- Viktor Frankl",
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = Color(0xFF775A00)
          )
        }
      }
    }

    AdaptiveTwoPane(
      first = { paneModifier ->
        BreatheCard(modifier = paneModifier, containerColor = BreatheOverlay.copy(alpha = 0.55f)) {
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
      },
      second = { paneModifier ->
        BreatheCard(modifier = paneModifier, containerColor = BreatheOverlay.copy(alpha = 0.55f)) {
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
    )
  }
}

@Composable
private fun HomeLiveStateCard(uiState: HomeUiState) {
  BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.74f)) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val compact = maxWidth < 380.dp

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LIVE STATE",
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = BreatheAccentStrong.copy(alpha = 0.66f)
          )
          StatusConnectionIndicator(connected = uiState.wsConnected, compact = compact)
        }
        Text(
          text = "Emotional Weather",
          style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
          color = BreatheInk
        )
      }
    }

    AdaptiveTwoPane(
      first = { paneModifier ->
        WeatherStatusCard(
          modifier = paneModifier,
          heading = "You",
          status = uiState.ownStatus,
          primary = true
        )
      },
      second = { paneModifier ->
        WeatherStatusCard(
          modifier = paneModifier,
          heading = "Partner",
          status = uiState.partnerStatus,
          primary = false
        )
      }
    )

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
          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(item.dotColor, CircleShape)
            )
            Text(
              text = item.label,
              modifier = Modifier.weight(1f),
              style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
              color = BreatheInk,
              fontWeight = FontWeight.Medium,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
          Text(
            text = item.time,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = BreatheMutedInk,
            textAlign = TextAlign.End
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

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val compact = maxWidth < 172.dp
    val narrowChip = maxWidth < 140.dp
    val iconSize = if (compact) 56.dp else 64.dp

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 176.dp)
        .background(BreatheCanvas.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
        .padding(horizontal = 16.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = heading.uppercase(),
        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
        color = BreatheAccentStrong.copy(alpha = 0.64f)
      )

      Box(
        modifier = Modifier
          .size(iconSize)
          .background(accent.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
      }

      Text(
        text = homeStatusLabel(status),
        modifier = Modifier
          .fillMaxWidth(if (narrowChip) 1f else 0.86f)
          .background(chipBackground, RoundedCornerShape(999.dp))
          .padding(horizontal = 14.dp, vertical = 8.dp),
        style = if (narrowChip) {
          androidx.compose.material3.MaterialTheme.typography.labelMedium
        } else {
          androidx.compose.material3.MaterialTheme.typography.labelLarge
        },
        color = chipText,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = if (narrowChip || compact) 2 else 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun StatusConnectionIndicator(connected: Boolean, compact: Boolean) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(if (connected) BreatheAccentStrong else BreatheBorder, CircleShape)
    )
    Text(
      text = if (compact) {
        if (connected) "Linked" else "Offline"
      } else {
        if (connected) "Realtime linked" else "Offline-first mode"
      },
      style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
      color = BreatheAccentStrong.copy(alpha = 0.72f),
      textAlign = TextAlign.End,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
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
      .heightIn(min = 116.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .background(BreatheCardSurface, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = title, tint = BreatheAccentStrong)
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = title,
      color = BreatheInk,
      style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
      textAlign = TextAlign.Center,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
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
