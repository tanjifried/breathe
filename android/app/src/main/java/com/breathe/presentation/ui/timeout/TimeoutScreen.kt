package com.breathe.presentation.ui.timeout

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
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.model.TimeoutLock
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.usecase.EndSessionUseCase
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
        if (lock.sessionId != null && lock.isLocked) {
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

  private fun startCountdown(sessionId: Long, initialSeconds: Int) {
    countdownJob = viewModelScope.launch {
      var remaining = initialSeconds
      while (remaining > 0) {
        delay(1_000)
        remaining -= 1
        _uiState.update {
          if (it.sessionId == sessionId) {
            it.copy(secondsRemaining = remaining.coerceAtLeast(0), isLocked = remaining > 0)
          } else {
            it
          }
        }
      }
      endSessionUseCase(sessionId)
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
    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("Lock state")
        MiniStat("Locked", if (uiState.isLocked) "Yes" else "No")
        MiniStat("Seconds remaining", uiState.secondsRemaining.toString())
        MiniStat("Unlocks at", uiState.unlocksAt ?: "Not active")
      }
    }

    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Actions")
        Button(
          onClick = { viewModel.onEvent(TimeoutUiEvent.StartTimeout) },
          enabled = !uiState.isLocked,
          colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
        ) {
          Text(if (uiState.isLocked) "Timeout in progress" else "Start timeout")
        }
        if (!uiState.isLocked && uiState.sessionId != null) {
          Text("Re-entry window is open. Move slowly before restarting a timeout.")
        }
      }
    }
  }
}
