package com.breathe.presentation.ui.updates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.QuickUpdate
import com.breathe.domain.usecase.GetQuickUpdatesUseCase
import com.breathe.domain.usecase.SendQuickUpdateUseCase
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheYellow
import com.breathe.presentation.ui.common.AdaptiveTwoPane
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.PrimaryActionButton
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuickUpdatePreset(
  val key: String,
  val label: String,
  val icon: ImageVector
)

private val quickUpdatePresets = listOf(
  QuickUpdatePreset("on_my_way", "On my way", Icons.AutoMirrored.Rounded.DirectionsRun),
  QuickUpdatePreset("eating_now", "Eating now", Icons.Rounded.Restaurant),
  QuickUpdatePreset("at_work", "At work", Icons.Rounded.LaptopMac),
  QuickUpdatePreset("home_safe", "Home safe", Icons.Rounded.Home),
  QuickUpdatePreset("quiet_time", "Quiet time", Icons.Rounded.SelfImprovement),
  QuickUpdatePreset("resting", "Resting", Icons.Rounded.Bedtime)
)

data class QuickUpdatesUiState(
  val selectedPresetKey: String = quickUpdatePresets.first().key,
  val note: String = "",
  val entries: List<QuickUpdate> = emptyList(),
  val isSending: Boolean = false
) {
  val selectedPreset: QuickUpdatePreset
    get() = quickUpdatePresets.firstOrNull { it.key == selectedPresetKey } ?: quickUpdatePresets.first()
}

sealed interface QuickUpdatesUiEvent {
  data class SelectPreset(val presetKey: String) : QuickUpdatesUiEvent
  data class NoteChanged(val value: String) : QuickUpdatesUiEvent
  data object Send : QuickUpdatesUiEvent
  data object Refresh : QuickUpdatesUiEvent
}

@HiltViewModel
class QuickUpdatesViewModel @Inject constructor(
  private val getQuickUpdatesUseCase: GetQuickUpdatesUseCase,
  private val sendQuickUpdateUseCase: SendQuickUpdateUseCase
) : ViewModel() {
  private val localState = MutableStateFlow(QuickUpdatesUiState())
  val uiState: StateFlow<QuickUpdatesUiState> = combine(
    localState,
    getQuickUpdatesUseCase()
  ) { local, entries ->
    local.copy(entries = entries)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuickUpdatesUiState())

  init {
    onEvent(QuickUpdatesUiEvent.Refresh)
  }

  fun onEvent(event: QuickUpdatesUiEvent) {
    when (event) {
      is QuickUpdatesUiEvent.SelectPreset -> localState.update { it.copy(selectedPresetKey = event.presetKey) }
      is QuickUpdatesUiEvent.NoteChanged -> localState.update { it.copy(note = event.value.take(160)) }
      QuickUpdatesUiEvent.Refresh -> viewModelScope.launch { getQuickUpdatesUseCase.refresh() }
      QuickUpdatesUiEvent.Send -> sendSelectedUpdate()
    }
  }

  private fun sendSelectedUpdate() {
    val snapshot = localState.value
    val preset = snapshot.selectedPreset

    viewModelScope.launch {
      localState.update { it.copy(isSending = true) }
      runCatching {
        sendQuickUpdateUseCase(
          presetKey = preset.key,
          message = preset.label,
          note = snapshot.note.trim().takeIf { it.isNotEmpty() }
        )
      }
      localState.update { it.copy(isSending = false, note = "") }
    }
  }
}

@Composable
fun QuickUpdatesScreen(
  onNavigate: (String) -> Unit = {},
  viewModel: QuickUpdatesViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val latest = uiState.entries.firstOrNull()

  AppScreen(
    title = "Quick Updates",
    subtitle = "Let your partner know where you are or how you're feeling with a gentle touch.",
    showBottomNav = true,
    selectedBottomRoute = null,
    onNavigate = onNavigate
  ) {
    AdaptiveTwoPane(
      first = { paneModifier ->
        BreatheCard(modifier = paneModifier, containerColor = BreatheOverlay.copy(alpha = 0.56f)) {
          Text("LAST SHARED", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheAccentStrong.copy(alpha = 0.6f))
          Text(
            text = latest?.let {
              buildString {
                append('"')
                append(if (!it.note.isNullOrBlank()) it.note else it.message)
                append('"')
              }
            } ?: "\"No gentle update yet.\"",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = BreatheInk,
            fontStyle = FontStyle.Italic
          )
          Text(
            text = latest?.createdAt?.let(::relativeTime) ?: "just now",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = BreatheMutedInk
          )
        }
      },
      second = { paneModifier ->
        BreatheCard(modifier = paneModifier, containerColor = BreatheAccentStrong.copy(alpha = 0.12f)) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .background(BreatheCardSurface, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Rounded.Favorite, contentDescription = null, tint = BreatheAccentStrong, modifier = Modifier.size(28.dp))
            }
            Text("Connected", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheAccentStrong)
          }
        }
      }
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(
        text = "CHOOSE A RITUAL UPDATE",
        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
        color = BreatheAccentStrong.copy(alpha = 0.6f)
      )

      quickUpdatePresets.chunked(2).forEach { rowPresets ->
        AdaptiveTwoPane(
          first = { paneModifier ->
            val preset = rowPresets[0]
            PresetCard(
              preset = preset,
              selected = uiState.selectedPresetKey == preset.key,
              onClick = { viewModel.onEvent(QuickUpdatesUiEvent.SelectPreset(preset.key)) },
              modifier = paneModifier
            )
          },
          second = { paneModifier ->
            val preset = rowPresets[1]
            PresetCard(
              preset = preset,
              selected = uiState.selectedPresetKey == preset.key,
              onClick = { viewModel.onEvent(QuickUpdatesUiEvent.SelectPreset(preset.key)) },
              modifier = paneModifier
            )
          }
        )
      }
    }

    BreatheCard(containerColor = BreatheCardSurface.copy(alpha = 0.88f)) {
      Text("OR SHARE SOMETHING UNIQUE", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheAccentStrong.copy(alpha = 0.6f))
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.note,
        onValueChange = { viewModel.onEvent(QuickUpdatesUiEvent.NoteChanged(it)) },
        label = { Text("Write a short note") },
        placeholder = { Text("Taking a deep breath before the meeting.") },
        shape = RoundedCornerShape(999.dp)
      )
      PrimaryActionButton(
        text = if (uiState.isSending) "Sending..." else "Send Update",
        onClick = { viewModel.onEvent(QuickUpdatesUiEvent.Send) },
        enabled = !uiState.isSending,
        icon = Icons.AutoMirrored.Rounded.Send
      )
    }

    BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.18f)) {
      Text(
        text = "\"Communication is the bridge between two hearts.\"",
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
        color = BreatheInk,
        fontStyle = FontStyle.Italic
      )
    }
  }
}

@Composable
private fun PresetCard(
  preset: QuickUpdatePreset,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .background(
        if (selected) BreatheAccentStrong.copy(alpha = 0.14f) else BreatheOverlay.copy(alpha = 0.56f),
        RoundedCornerShape(22.dp)
      )
      .border(1.dp, if (selected) BreatheAccentStrong else BreatheBorder.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
      .heightIn(min = 116.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .padding(16.dp),
    verticalArrangement = Arrangement.Center
  ) {
    Icon(imageVector = preset.icon, contentDescription = null, tint = BreatheAccentStrong)
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
    Text(
      text = preset.label,
      style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
      color = BreatheInk,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun relativeTime(iso: String): String {
  val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return "just now"
  val minutes = Duration.between(instant, Instant.now()).toMinutes().coerceAtLeast(0)
  return when {
    minutes < 1 -> "just now"
    minutes < 60 -> "$minutes minutes ago"
    else -> "${minutes / 60} hours ago"
  }
}
