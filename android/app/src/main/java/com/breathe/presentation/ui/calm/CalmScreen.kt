package com.breathe.presentation.ui.calm

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.CalmSession
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.usecase.EndSessionUseCase
import com.breathe.domain.usecase.SendPeaceUseCase
import com.breathe.domain.usecase.StartSessionUseCase
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.HeroCard
import com.breathe.presentation.ui.common.MiniStat
import com.breathe.presentation.ui.common.PrimaryActionButton
import com.breathe.presentation.ui.common.SectionTitle
import com.breathe.presentation.ui.common.SecondaryActionButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalmUiState(
  val secondsRemaining: Int = 0,
  val voiceTrack: String? = null,
  val sessionId: Long? = null,
  val isActive: Boolean = false,
  val lastSignalMessage: String? = null
)

sealed interface CalmUiEvent {
  data object StartSession : CalmUiEvent
  data object CompleteSession : CalmUiEvent
  data object SendPeace : CalmUiEvent
}

@HiltViewModel
class CalmViewModel @Inject constructor(
  sessionRepository: SessionRepository,
  private val startSessionUseCase: StartSessionUseCase,
  private val endSessionUseCase: EndSessionUseCase,
  private val sendPeaceUseCase: SendPeaceUseCase
) : ViewModel() {
  private val _uiState = MutableStateFlow(CalmUiState())
  val uiState: StateFlow<CalmUiState> = _uiState.asStateFlow()
  private var countdownJob: Job? = null

  init {
    viewModelScope.launch {
      sessionRepository.observeActiveCalmSession().collect { session ->
        countdownJob?.cancel()
        if (session == null) {
          _uiState.value = CalmUiState(lastSignalMessage = _uiState.value.lastSignalMessage)
        } else {
          _uiState.value = session.toUiState(lastSignalMessage = _uiState.value.lastSignalMessage)
          startCountdown(session.sessionId, session.secondsRemaining)
        }
      }
    }
  }

  fun onEvent(event: CalmUiEvent) {
    when (event) {
      CalmUiEvent.StartSession -> viewModelScope.launch {
        _uiState.update { it.copy(lastSignalMessage = null) }
        startSessionUseCase(SessionFeature.CALM)
      }

      CalmUiEvent.CompleteSession -> viewModelScope.launch {
        _uiState.value.sessionId?.let { endSessionUseCase(it) }
      }

      CalmUiEvent.SendPeace -> viewModelScope.launch {
        sendPeaceUseCase()
        _uiState.update { it.copy(lastSignalMessage = "A gentle peace signal was sent to your partner.") }
      }
    }
  }

  private fun startCountdown(sessionId: Long?, initialSeconds: Int) {
    if (sessionId == null) {
      return
    }

    countdownJob = viewModelScope.launch {
      var remaining = initialSeconds
      while (remaining > 0) {
        delay(1_000)
        remaining -= 1
        _uiState.update {
          if (it.sessionId == sessionId) {
            it.copy(secondsRemaining = remaining.coerceAtLeast(0))
          } else {
            it
          }
        }
      }
      endSessionUseCase(sessionId)
    }
  }

  private fun CalmSession?.toUiState(lastSignalMessage: String?): CalmUiState = CalmUiState(
    secondsRemaining = this?.secondsRemaining ?: 0,
    voiceTrack = this?.voiceTrack,
    sessionId = this?.sessionId,
    isActive = this?.sessionId != null && (this.secondsRemaining > 0),
    lastSignalMessage = lastSignalMessage
  )
}

@Composable
fun CalmScreen(viewModel: CalmViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Calm session",
    subtitle = "A soft reset before words get faster than your nervous system can handle."
  ) {
    HeroCard(
      eyebrow = "Breathing room",
      title = if (uiState.isActive) formatMinutes(uiState.secondsRemaining) else "Start with one slower breath.",
      body = if (uiState.isActive) {
        "Stay with the pause. The goal is body-level de-escalation before problem solving."
      } else {
        "Calm is gentler than timeout. Use it when repair still feels possible with a little more steadiness."
      }
    )

    BreatheCard {
      SectionTitle("Session state")
      MiniStat("Time remaining", formatMinutes(uiState.secondsRemaining))
      MiniStat("Voice track", uiState.voiceTrack ?: "Warm voice guidance can be added next")
      uiState.lastSignalMessage?.let { Text(it) }
    }

    BreatheCard {
      SectionTitle("Actions")
      PrimaryActionButton(
        text = if (uiState.isActive) "Calm session is active" else "Start calm session",
        onClick = { viewModel.onEvent(CalmUiEvent.StartSession) },
        enabled = !uiState.isActive
      )
      if (uiState.isActive) {
        SecondaryActionButton(
          text = "Complete session",
          onClick = { viewModel.onEvent(CalmUiEvent.CompleteSession) }
        )
      }
      SecondaryActionButton(
        text = "Send peace",
        onClick = { viewModel.onEvent(CalmUiEvent.SendPeace) }
      )
    }
  }
}

private fun formatMinutes(seconds: Int): String {
  val safeSeconds = seconds.coerceAtLeast(0)
  val minutes = safeSeconds / 60
  val remainder = safeSeconds % 60
  return "%02d:%02d".format(minutes, remainder)
}
