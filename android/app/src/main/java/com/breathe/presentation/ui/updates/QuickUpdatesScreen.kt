package com.breathe.presentation.ui.updates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.QuickUpdate
import com.breathe.domain.usecase.GetQuickUpdatesUseCase
import com.breathe.domain.usecase.SendQuickUpdateUseCase
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.HeroCard
import com.breathe.presentation.ui.common.PrimaryActionButton
import com.breathe.presentation.ui.common.SectionTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickUpdatePreset(
  val key: String,
  val label: String,
  val body: String
)

private val quickUpdatePresets = listOf(
  QuickUpdatePreset("on_my_way", "On my way", "A small logistical signal with no pressure."),
  QuickUpdatePreset("eating_now", "Eating now", "A practical heads-up that removes guesswork."),
  QuickUpdatePreset("quiet_time", "Quiet time", "A gentle cue that the next contact should stay light."),
  QuickUpdatePreset("thinking_of_you", "Thinking of you", "Connection without asking for a long exchange.")
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
fun QuickUpdatesScreen(viewModel: QuickUpdatesViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Quick updates",
    subtitle = "Low-pressure pings for everyday logistics and gentle reassurance."
  ) {
    HeroCard(
      eyebrow = "Micro-connection",
      title = "Keep each other informed without opening a heavy thread.",
      body = "These updates are intentionally small. They reduce uncertainty without demanding a long conversation."
    )

    BreatheCard {
      SectionTitle("Choose a gentle signal")
      quickUpdatePresets.forEach { preset ->
        val selected = preset.key == uiState.selectedPresetKey
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              if (selected) BreatheAccentStrong.copy(alpha = 0.12f) else BreatheCardSurface,
              RoundedCornerShape(22.dp)
            )
            .border(1.dp, BreatheBorder, RoundedCornerShape(22.dp))
            .clickable { viewModel.onEvent(QuickUpdatesUiEvent.SelectPreset(preset.key)) }
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(preset.label)
          Text(preset.body, color = BreatheMutedInk)
        }
      }
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.note,
        onValueChange = { viewModel.onEvent(QuickUpdatesUiEvent.NoteChanged(it)) },
        label = { Text("Optional note") },
        placeholder = { Text("Add a little context if it helps.") },
        shape = RoundedCornerShape(20.dp)
      )
      PrimaryActionButton(
        text = if (uiState.isSending) "Sending..." else "Send quick update",
        onClick = { viewModel.onEvent(QuickUpdatesUiEvent.Send) },
        enabled = !uiState.isSending
      )
    }

    BreatheCard {
      SectionTitle("Recent updates")
      if (uiState.entries.isEmpty()) {
        Text("No quick updates yet.")
      } else {
        uiState.entries.forEach { entry ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(BreatheCardSurface, RoundedCornerShape(22.dp))
              .border(1.dp, BreatheBorder.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(if (entry.isOwn) "You · ${entry.message}" else "Partner · ${entry.message}")
            if (!entry.note.isNullOrBlank()) {
              Text(entry.note, color = BreatheMutedInk)
            }
            Text(entry.createdAt, color = BreatheMutedInk)
          }
        }
      }
    }
  }
}
