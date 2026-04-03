package com.breathe.presentation.ui.calm

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
import com.breathe.domain.model.CalmSession
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.usecase.SendPeaceUseCase
import com.breathe.domain.usecase.StartSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalmUiState(
  val secondsRemaining: Int = 0,
  val voiceTrack: String? = null,
  val sessionId: Long? = null
)

sealed interface CalmUiEvent {
  data object StartSession : CalmUiEvent
  data object SendPeace : CalmUiEvent
}

@HiltViewModel
class CalmViewModel @Inject constructor(
  sessionRepository: SessionRepository,
  private val startSessionUseCase: StartSessionUseCase,
  private val sendPeaceUseCase: SendPeaceUseCase
) : ViewModel() {
  val uiState: StateFlow<CalmUiState> = sessionRepository.observeActiveCalmSession()
    .map { session -> session.toUiState() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalmUiState())

  fun onEvent(event: CalmUiEvent) {
    when (event) {
      CalmUiEvent.StartSession -> viewModelScope.launch { startSessionUseCase(SessionFeature.CALM) }
      CalmUiEvent.SendPeace -> viewModelScope.launch { sendPeaceUseCase() }
    }
  }

  private fun CalmSession?.toUiState(): CalmUiState = CalmUiState(
    secondsRemaining = this?.secondsRemaining ?: 0,
    voiceTrack = this?.voiceTrack,
    sessionId = this?.sessionId
  )
}

@Composable
fun CalmScreen(viewModel: CalmViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Calm session")
    Text("Seconds remaining: ${uiState.secondsRemaining}")
    Text("Voice track: ${uiState.voiceTrack ?: "Not selected"}")
    Button(onClick = { viewModel.onEvent(CalmUiEvent.StartSession) }) { Text("Start calm session") }
    Button(onClick = { viewModel.onEvent(CalmUiEvent.SendPeace) }) { Text("Send peace") }
  }
}
