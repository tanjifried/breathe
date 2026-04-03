package com.breathe.presentation.ui.timeout

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
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.model.TimeoutLock
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.usecase.StartSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimeoutUiState(
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
  private val startSessionUseCase: StartSessionUseCase
) : ViewModel() {
  val uiState: StateFlow<TimeoutUiState> = sessionRepository.observeTimeoutLock()
    .map { lock -> lock.toUiState() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeoutUiState())

  fun onEvent(event: TimeoutUiEvent) {
    when (event) {
      TimeoutUiEvent.StartTimeout -> viewModelScope.launch { startSessionUseCase(SessionFeature.TIMEOUT) }
    }
  }

  private fun TimeoutLock.toUiState(): TimeoutUiState = TimeoutUiState(
    secondsRemaining = secondsRemaining,
    isLocked = isLocked,
    unlocksAt = unlocksAt
  )
}

@Composable
fun TimeoutScreen(viewModel: TimeoutViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Structured timeout")
    Text("Locked: ${uiState.isLocked}")
    Text("Seconds remaining: ${uiState.secondsRemaining}")
    Text("Unlocks at: ${uiState.unlocksAt ?: "Not active"}")
    Button(onClick = { viewModel.onEvent(TimeoutUiEvent.StartTimeout) }) { Text("Start timeout") }
  }
}
