package com.breathe.presentation.ui.calm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.breathe.domain.model.CalmSession
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.usecase.EndSessionUseCase
import com.breathe.domain.usecase.SendPeaceUseCase
import com.breathe.domain.usecase.StartSessionUseCase
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.MiniStat
import com.breathe.presentation.ui.common.SectionTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
  val isActive: Boolean = false
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
          _uiState.value = CalmUiState()
        } else {
          _uiState.value = session.toUiState()
          startCountdown(session.sessionId, session.secondsRemaining)
        }
      }
    }
  }

  fun onEvent(event: CalmUiEvent) {
    when (event) {
      CalmUiEvent.StartSession -> viewModelScope.launch { startSessionUseCase(SessionFeature.CALM) }
      CalmUiEvent.CompleteSession -> viewModelScope.launch {
        _uiState.value.sessionId?.let { endSessionUseCase(it) }
      }
      CalmUiEvent.SendPeace -> viewModelScope.launch { sendPeaceUseCase() }
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

  private fun CalmSession?.toUiState(): CalmUiState = CalmUiState(
    secondsRemaining = this?.secondsRemaining ?: 0,
    voiceTrack = this?.voiceTrack,
    sessionId = this?.sessionId,
    isActive = this?.sessionId != null && (this.secondsRemaining > 0)
  )
}

@Composable
fun CalmScreen(viewModel: CalmViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Calm session",
    subtitle = "A soft reset before words get faster than your nervous system can handle."
  ) {
    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("Session state")
        MiniStat("Seconds remaining", uiState.secondsRemaining.toString())
        MiniStat("Voice track", uiState.voiceTrack ?: "Not selected")
      }
    }

    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Actions")
        Button(
          onClick = { viewModel.onEvent(CalmUiEvent.StartSession) },
          enabled = !uiState.isActive,
          colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
        ) {
          Text(if (uiState.isActive) "Calm session active" else "Start calm session")
        }
        if (uiState.isActive) {
          Button(onClick = { viewModel.onEvent(CalmUiEvent.CompleteSession) }) { Text("Complete calm session") }
        }
        Button(onClick = { viewModel.onEvent(CalmUiEvent.SendPeace) }) { Text("Send peace") }
      }
    }
  }
}
