package com.breathe.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
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
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.StatusRepository
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.MiniStat
import com.breathe.presentation.ui.common.SectionTitle
import com.breathe.presentation.ui.common.StatusPill
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

  AppScreen(
    title = "Breathe",
    subtitle = "A calmer surface for checking in before conflict becomes momentum."
  ) {
    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("Live state")
        StatusPill(label = "You: ${uiState.ownStatus?.name ?: "Unset"}", status = uiState.ownStatus)
        StatusPill(label = "Partner: ${uiState.partnerStatus?.name ?: "Unknown"}", status = uiState.partnerStatus)
        MiniStat("Connection", if (uiState.wsConnected) "Realtime linked" else "Offline-first mode")
      }
    }

    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Regulation tools")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          Button(
            onClick = { onNavigate(Screen.Status.route) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
          ) { Text("Status") }
          Button(
            onClick = { onNavigate(Screen.Calm.route) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
          ) { Text("Calm") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          Button(
            onClick = { onNavigate(Screen.Timeout.route) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
          ) { Text("Timeout") }
          Button(
            onClick = { onNavigate(Screen.Voice.route) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
          ) { Text("Voice") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          Button(
            onClick = { onNavigate(Screen.Log.route) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
          ) { Text("Log") }
          Button(
            onClick = { onNavigate(Screen.Insights.route) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
          ) { Text("Insights") }
        }
      }
    }
  }
}
