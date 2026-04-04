package com.breathe.presentation.ui.timeout

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.model.TimeoutLock
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.usecase.EndSessionUseCase
import com.breathe.domain.usecase.StartSessionUseCase
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.HeroCard
import com.breathe.presentation.ui.common.MiniStat
import com.breathe.presentation.ui.common.PrimaryActionButton
import com.breathe.presentation.ui.common.SectionTitle
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

data class TimeoutUiState(
  val sessionId: Long? = null,
  val secondsRemaining: Int = 0,
  val isLocked: Boolean = false,
  val unlocksAt: String? = null
)

sealed interface TimeoutUiEvent {
  data object StartTimeout : TimeoutUiEvent
}

@HiltViewModel
class TimeoutViewModel @Inject constructor(
  sessionRepository: SessionRepository,
  private val startSessionUseCase: StartSessionUseCase,
  private val endSessionUseCase: EndSessionUseCase
) : ViewModel() {
  private val _uiState = MutableStateFlow(TimeoutUiState())
  val uiState: StateFlow<TimeoutUiState> = _uiState.asStateFlow()
  private var countdownJob: Job? = null

  init {
    viewModelScope.launch {
      sessionRepository.observeTimeoutLock().collect { lock ->
        countdownJob?.cancel()
        _uiState.value = lock.toUiState()
        if (lock.isLocked) {
          startCountdown(lock.sessionId, lock.secondsRemaining)
        }
      }
    }
  }

  fun onEvent(event: TimeoutUiEvent) {
    when (event) {
      TimeoutUiEvent.StartTimeout -> viewModelScope.launch { startSessionUseCase(SessionFeature.TIMEOUT) }
    }
  }

  private fun startCountdown(sessionId: Long?, initialSeconds: Int) {
    countdownJob = viewModelScope.launch {
      var remaining = initialSeconds
      while (remaining > 0) {
        delay(1_000)
        remaining -= 1
        _uiState.update {
          if (sessionId == null || it.sessionId == sessionId || it.sessionId == null) {
            it.copy(secondsRemaining = remaining.coerceAtLeast(0), isLocked = remaining > 0)
          } else {
            it
          }
        }
      }
      sessionId?.let { endSessionUseCase(it) }
    }
  }

  private fun TimeoutLock.toUiState(): TimeoutUiState = TimeoutUiState(
    sessionId = sessionId,
    secondsRemaining = secondsRemaining,
    isLocked = isLocked,
    unlocksAt = unlocksAt
  )
}

@Composable
fun TimeoutScreen(viewModel: TimeoutViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Structured timeout",
    subtitle = "A hard pause for your body first, problem-solving later."
  ) {
    HeroCard(
      eyebrow = "Protective boundary",
      title = if (uiState.isLocked) formatTimeout(uiState.secondsRemaining) else "No timeout is active.",
      body = if (uiState.isLocked) {
        "Containment is the goal here. Let the timer hold the line so you do not have to negotiate it mid-activation."
      } else {
        "Use timeout when the body is too activated for repair and you need a real re-entry boundary."
      }
    )

    BreatheCard {
      SectionTitle("Lock state")
      MiniStat("Locked", if (uiState.isLocked) "Yes" else "No")
      MiniStat("Time remaining", formatTimeout(uiState.secondsRemaining))
      MiniStat("Unlocks at", uiState.unlocksAt ?: "Not active")
      if (!uiState.isLocked && uiState.sessionId != null) {
        Text("The re-entry window is open. Keep the first message short, slow, and concrete.")
      }
    }

    BreatheCard {
      SectionTitle("Actions")
      PrimaryActionButton(
        text = if (uiState.isLocked) "Timeout in progress" else "Start timeout",
        onClick = { viewModel.onEvent(TimeoutUiEvent.StartTimeout) },
        enabled = !uiState.isLocked
      )
    }
  }
}

private fun formatTimeout(seconds: Int): String {
  val safeSeconds = seconds.coerceAtLeast(0)
  val minutes = safeSeconds / 60
  val remainder = safeSeconds % 60
  return "%02d:%02d".format(minutes, remainder)
}
