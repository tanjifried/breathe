package com.breathe.presentation.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.ConflictLogEntry
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.repository.SessionRepository
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheRed
import com.breathe.presentation.theme.BreatheYellow
import com.breathe.presentation.ui.common.AdaptiveThreePane
import com.breathe.presentation.ui.common.AdaptiveTwoPane
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LogUiState(
  val entries: List<ConflictLogEntry> = emptyList(),
  val selectedEntry: ConflictLogEntry? = null,
  val isShared: Boolean = false
)

sealed interface LogUiEvent {
  data class SelectEntry(val sessionId: Long) : LogUiEvent
}

@HiltViewModel
class LogViewModel @Inject constructor(
  sessionRepository: SessionRepository
) : ViewModel() {
  private val selectedSessionId = MutableStateFlow<Long?>(null)

  val uiState: StateFlow<LogUiState> = combine(
    sessionRepository.observeConflictLogs(),
    selectedSessionId
  ) { entries, selectedId ->
    val selectedEntry = entries.firstOrNull { it.sessionId == selectedId } ?: entries.firstOrNull()
    LogUiState(
      entries = entries,
      selectedEntry = selectedEntry,
      isShared = selectedEntry?.isShared == true
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogUiState())

  fun onEvent(event: LogUiEvent) {
    when (event) {
      is LogUiEvent.SelectEntry -> selectedSessionId.update { event.sessionId }
    }
  }
}

@Composable
fun ConflictLogScreen(
  onNavigate: (String) -> Unit = {},
  viewModel: LogViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Conflict log",
    subtitle = "A private record of repair attempts and the shape each difficult moment took.",
    showBottomNav = true,
    selectedBottomRoute = Screen.Log.route,
    onNavigate = onNavigate
  ) {
    AdaptiveThreePane(
      first = { paneModifier ->
        LogSummaryCard(modifier = paneModifier, label = "Entries", value = uiState.entries.size.toString())
      },
      second = { paneModifier ->
        LogSummaryCard(modifier = paneModifier, label = "Selected", value = uiState.selectedEntry?.feature?.let(::featureSummaryLabel) ?: "None")
      },
      third = { paneModifier ->
        LogSummaryCard(modifier = paneModifier, label = "Shared", value = if (uiState.isShared) "Yes" else "No")
      }
    )

    BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.54f)) {
      Text("Selected entry", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
      val entry = uiState.selectedEntry
      if (entry == null) {
        Text("No sessions have been logged yet.", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
      } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (entry.feature == SessionFeature.CALM) Icons.Rounded.SelfImprovement else Icons.Rounded.Lock,
            contentDescription = null,
            tint = featureAccent(entry.feature)
          )
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(featureTitle(entry.feature), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
            Text(prettyMoment(entry.startedAt), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = BreatheMutedInk)
          }
        }
        Text(
          text = entry.privateNote ?: "No private note saved for this session.",
          style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
          color = BreatheMutedInk
        )
        AdaptiveTwoPane(
          first = { paneModifier ->
            LogMetricChip(modifier = paneModifier, label = "Duration", value = entry.durationSeconds?.let { "$it sec" } ?: "Open")
          },
          second = { paneModifier ->
            LogMetricChip(modifier = paneModifier, label = "Mood shift", value = buildMoodShift(entry))
          }
        )
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("Entries", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
      if (uiState.entries.isEmpty()) {
        BreatheCard {
          Text(
            text = "Your local log is still empty. Calm and Timeout sessions will appear here.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = BreatheMutedInk
          )
        }
      } else {
        uiState.entries.forEach { entry ->
          LogEntryCard(
            entry = entry,
            selected = uiState.selectedEntry?.sessionId == entry.sessionId,
            onClick = { viewModel.onEvent(LogUiEvent.SelectEntry(entry.sessionId)) }
          )
        }
      }
    }
  }
}

@Composable
private fun LogSummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
  BreatheCard(modifier = modifier.heightIn(min = 104.dp), containerColor = BreatheCardSurface) {
    Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
    Text(
      text = value,
      style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
      color = BreatheInk,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun LogMetricChip(label: String, value: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .background(BreatheCardSurface, RoundedCornerShape(18.dp))
      .padding(horizontal = 14.dp, vertical = 12.dp)
  ) {
    Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
    Text(
      text = value,
      style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
      color = BreatheInk,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun LogEntryCard(entry: ConflictLogEntry, selected: Boolean, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        if (selected) featureAccent(entry.feature).copy(alpha = 0.12f) else BreatheOverlay.copy(alpha = 0.48f),
        RoundedCornerShape(22.dp)
      )
      .border(1.dp, if (selected) featureAccent(entry.feature) else BreatheBorder.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
      .heightIn(min = 80.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(featureTitle(entry.feature), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
      Text(prettyMoment(entry.startedAt), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = BreatheMutedInk)
    }
    Text(
      text = buildMoodShift(entry),
      style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
      color = featureAccent(entry.feature),
      textAlign = TextAlign.End,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun buildMoodShift(entry: ConflictLogEntry): String {
  val before = entry.moodBefore
  val after = entry.moodAfter
  return if (before != null && after != null) {
    val delta = after - before
    if (delta >= 0) "+$delta" else delta.toString()
  } else {
    "n/a"
  }
}

private fun featureAccent(feature: SessionFeature): Color = when (feature) {
  SessionFeature.CALM -> BreatheAccentStrong
  SessionFeature.TIMEOUT -> BreatheRed
}

private fun featureTitle(feature: SessionFeature): String = when (feature) {
  SessionFeature.CALM -> "Calm session"
  SessionFeature.TIMEOUT -> "Structured timeout"
}

private fun featureSummaryLabel(feature: SessionFeature): String = when (feature) {
  SessionFeature.CALM -> "Calm"
  SessionFeature.TIMEOUT -> "Timeout"
}

private fun prettyMoment(raw: String): String = raw.replace('T', ' ').removeSuffix("Z")
