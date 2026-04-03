package com.breathe.presentation.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class VoiceUiState(
  val prompts: List<VoicePrompt> = emptyList(),
  val isRecording: Boolean = false,
  val currentSlot: Int? = null
)

sealed interface VoiceUiEvent {
  data class SelectSlot(val slot: Int) : VoiceUiEvent
  data object ToggleRecording : VoiceUiEvent
}

@HiltViewModel
class VoiceViewModel @Inject constructor(
  voiceRepository: VoiceRepository
) : ViewModel() {
  private val baseState = VoiceUiState()

  val uiState: StateFlow<VoiceUiState> = voiceRepository.observePrompts()
    .map { prompts -> baseState.copy(prompts = prompts) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), baseState)

  fun onEvent(event: VoiceUiEvent) = Unit
}

@Composable
fun VoiceStudioScreen(viewModel: VoiceViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Voice Studio")
    Text("Recording: ${uiState.isRecording}")
    Text("Current slot: ${uiState.currentSlot ?: "none"}")
    uiState.prompts.forEach { prompt ->
      Button(onClick = { viewModel.onEvent(VoiceUiEvent.SelectSlot(prompt.slot)) }) {
        Text("Slot ${prompt.slot}: ${prompt.label}")
      }
    }
  }
}
