package com.breathe.presentation.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.VoicePrompt
import com.breathe.domain.repository.VoiceRepository
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.theme.BreatheAccent
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheRed
import com.breathe.presentation.theme.BreatheYellow
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.PrimaryActionButton
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
fun VoiceStudioScreen(
  onNavigate: (String) -> Unit = {},
  viewModel: VoiceViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Voice Studio",
    subtitle = "Prepare warm, regulating phrases while calm so your future self does not have to invent them in the middle of stress.",
    showBottomNav = true,
    selectedBottomRoute = null,
    onNavigate = onNavigate
  ) {
    BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.52f)) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {
        Box(
          modifier = Modifier
            .size(188.dp)
            .background(BreatheAccent.copy(alpha = 0.08f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(154.dp)
              .background(
                brush = Brush.linearGradient(
                  colors = if (uiState.isRecording) {
                    listOf(BreatheRed.copy(alpha = 0.25f), BreatheCardSurface, BreatheYellow.copy(alpha = 0.18f))
                  } else {
                    listOf(BreatheCardSurface, BreatheAccent.copy(alpha = 0.18f), BreatheOverlay)
                  }
                ),
                shape = CircleShape
              )
              .border(1.dp, BreatheBorder.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (uiState.isRecording) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
              contentDescription = null,
              tint = if (uiState.isRecording) BreatheRed else BreatheAccentStrong,
              modifier = Modifier.size(56.dp)
            )
          }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (uiState.isRecording) "Recording a care phrase" else "Current take",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = BreatheInk
          )
          Text(
            text = uiState.currentPromptLabel ?: "Choose a slot below to preview the prompt you want to record.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = BreatheMutedInk
          )
        }
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      VoiceInfoCard(
        modifier = Modifier.weight(1f),
        label = "Recording",
        value = if (uiState.isRecording) "In progress" else "Idle",
        icon = if (uiState.isRecording) Icons.Rounded.GraphicEq else Icons.Rounded.RadioButtonChecked
      )
      VoiceInfoCard(
        modifier = Modifier.weight(1f),
        label = "Slot",
        value = uiState.currentSlot?.toString() ?: "None",
        icon = Icons.Rounded.Mic
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = "Prompt slots",
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
        color = BreatheAccentStrong
      )
      uiState.prompts.forEach { prompt ->
        VoicePromptCard(
          prompt = prompt,
          selected = uiState.currentSlot == prompt.slot,
          onClick = { viewModel.onEvent(VoiceUiEvent.SelectSlot(prompt.slot)) }
        )
      }
    }

    PrimaryActionButton(
      text = if (uiState.isRecording) "Stop recording" else "Start recording",
      onClick = { viewModel.onEvent(VoiceUiEvent.ToggleRecording) },
      enabled = uiState.currentSlot != null,
      icon = if (uiState.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic
    )
  }
}

@Composable
private fun VoiceInfoCard(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  BreatheCard(modifier = modifier, containerColor = BreatheCardSurface) {
    Icon(imageVector = icon, contentDescription = null, tint = BreatheAccentStrong)
    Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
    Text(value, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
  }
}

@Composable
private fun VoicePromptCard(prompt: VoicePrompt, selected: Boolean, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        if (selected) BreatheAccentStrong.copy(alpha = 0.12f) else BreatheOverlay.copy(alpha = 0.48f),
        RoundedCornerShape(22.dp)
      )
      .border(1.dp, if (selected) BreatheAccentStrong else BreatheBorder.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 18.dp, vertical = 18.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text("Slot ${prompt.slot}", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
      Text(prompt.label, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
    }

    Box(
      modifier = Modifier
        .size(38.dp)
        .background(if (selected) BreatheAccentStrong.copy(alpha = 0.18f) else BreatheYellow.copy(alpha = 0.12f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = Icons.Rounded.Mic, contentDescription = null, tint = if (selected) BreatheAccentStrong else BreatheMutedInk)
    }
  }
}
