package com.breathe.presentation.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.VoicePrompt
import com.breathe.domain.repository.VoiceRepository
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.MiniStat
import com.breathe.presentation.ui.common.SectionTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class VoiceUiState(
  val prompts: List<VoicePrompt> = emptyList(),
  val isRecording: Boolean = false,
  val currentSlot: Int? = null,
  val currentPromptLabel: String? = null
)

sealed interface VoiceUiEvent {
  data class SelectSlot(val slot: Int) : VoiceUiEvent
  data object ToggleRecording : VoiceUiEvent
}

@HiltViewModel
class VoiceViewModel @Inject constructor(
  voiceRepository: VoiceRepository
) : ViewModel() {
  private val selectedSlot = MutableStateFlow<Int?>(null)
  private val recording = MutableStateFlow(false)

  val uiState: StateFlow<VoiceUiState> = combine(
    voiceRepository.observePrompts(),
    selectedSlot,
    recording
  ) { prompts, slot, isRecording ->
    val selectedPrompt = prompts.firstOrNull { it.slot == slot }
    VoiceUiState(
      prompts = prompts,
      isRecording = isRecording,
      currentSlot = slot,
      currentPromptLabel = selectedPrompt?.label
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VoiceUiState())

  fun onEvent(event: VoiceUiEvent) {
    when (event) {
      is VoiceUiEvent.SelectSlot -> {
        selectedSlot.update { event.slot }
        recording.update { false }
      }

      VoiceUiEvent.ToggleRecording -> {
        if (uiState.value.currentSlot != null) {
          recording.update { !it }
        }
      }
    }
  }
}

@Composable
fun VoiceStudioScreen(viewModel: VoiceViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Voice Studio",
    subtitle = "Prepare warm, regulating phrases while calm so your future self does not have to invent them in the middle of stress."
  ) {
    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Current take")
        MiniStat("Recording", if (uiState.isRecording) "In progress" else "Idle")
        MiniStat("Slot", uiState.currentSlot?.toString() ?: "None")
        Text(uiState.currentPromptLabel ?: "Choose a slot below to preview the prompt you want to record.")
        Button(
          onClick = { viewModel.onEvent(VoiceUiEvent.ToggleRecording) },
          enabled = uiState.currentSlot != null,
          colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
        ) {
          Text(if (uiState.isRecording) "Stop recording" else "Start recording")
        }
      }
    }

    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Prompt slots")
        uiState.prompts.forEach { prompt ->
          Button(
            onClick = { viewModel.onEvent(VoiceUiEvent.SelectSlot(prompt.slot)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
          ) {
            Text("Slot ${prompt.slot}: ${prompt.label}")
          }
        }
      }
    }
  }
}
