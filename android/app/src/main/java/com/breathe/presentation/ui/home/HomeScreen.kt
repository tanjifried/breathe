package com.breathe.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.breathe.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
  val ownStatus: StatusLevel? = null,
  val partnerStatus: StatusLevel? = null,
  val wsConnected: Boolean = false
)

sealed interface HomeUiEvent {
  data object NoOp : HomeUiEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
  statusRepository: StatusRepository
) : ViewModel() {
  val uiState: StateFlow<HomeUiState> = combine(
    statusRepository.observeOwnStatus(),
    statusRepository.observePartnerStatus(),
    statusRepository.observeWsConnection()
  ) { own, partner, connected ->
    HomeUiState(ownStatus = own, partnerStatus = partner, wsConnected = connected)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

  fun onEvent(event: HomeUiEvent) = Unit
}

@Composable
fun HomeScreen(
  onNavigate: (String) -> Unit,
  viewModel: HomeViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Breathe")
    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Own status: ${uiState.ownStatus?.name ?: "unset"}")
        Text("Partner status: ${uiState.partnerStatus?.name ?: "unknown"}")
        Text("WebSocket: ${if (uiState.wsConnected) "connected" else "offline"}")
      }
    }

    Button(onClick = { onNavigate(Screen.Status.route) }) { Text("Status") }
    Button(onClick = { onNavigate(Screen.Calm.route) }) { Text("Calm") }
    Button(onClick = { onNavigate(Screen.Timeout.route) }) { Text("Timeout") }
    Button(onClick = { onNavigate(Screen.Voice.route) }) { Text("Voice Studio") }
    Button(onClick = { onNavigate(Screen.Log.route) }) { Text("Conflict Log") }
    Button(onClick = { onNavigate(Screen.Insights.route) }) { Text("Insights") }
  }
}
