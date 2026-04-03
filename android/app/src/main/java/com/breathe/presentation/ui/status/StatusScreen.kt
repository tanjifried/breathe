package com.breathe.presentation.ui.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.StatusRepository
import com.breathe.domain.usecase.SetStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatusUiState(
  val selectedStatus: StatusLevel? = null
)

sealed interface StatusUiEvent {
  data class SelectStatus(val status: StatusLevel) : StatusUiEvent
}

@HiltViewModel
class StatusViewModel @Inject constructor(
  statusRepository: StatusRepository,
  private val setStatusUseCase: SetStatusUseCase
) : ViewModel() {
  val uiState: StateFlow<StatusUiState> = statusRepository.observeOwnStatus()
    .map { StatusUiState(selectedStatus = it) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatusUiState())

  fun onEvent(event: StatusUiEvent) {
    when (event) {
      is StatusUiEvent.SelectStatus -> viewModelScope.launch { setStatusUseCase(event.status) }
    }
  }
}

@Composable
fun StatusScreen(viewModel: StatusViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Current status: ${uiState.selectedStatus?.name ?: "unset"}")
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(onClick = { viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.GREEN)) }) { Text("Green") }
      Button(onClick = { viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.YELLOW)) }) { Text("Yellow") }
      Button(onClick = { viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.RED)) }) { Text("Red") }
    }
  }
}
