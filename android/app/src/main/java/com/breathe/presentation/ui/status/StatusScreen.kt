package com.breathe.presentation.ui.status

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.StatusRepository
import com.breathe.domain.usecase.SetStatusUseCase
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.HeroCard
import com.breathe.presentation.ui.common.MiniStat
import com.breathe.presentation.ui.common.PrimaryActionButton
import com.breathe.presentation.ui.common.SectionTitle
import com.breathe.presentation.ui.common.SecondaryActionButton
import com.breathe.presentation.ui.common.StatusPill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatusUiState(
  val selectedStatus: StatusLevel? = null,
  val partnerStatus: StatusLevel? = null,
  val wsConnected: Boolean = false
)

sealed interface StatusUiEvent {
  data class SelectStatus(val status: StatusLevel) : StatusUiEvent
}

@HiltViewModel
class StatusViewModel @Inject constructor(
  statusRepository: StatusRepository,
  private val setStatusUseCase: SetStatusUseCase
) : ViewModel() {
  val uiState: StateFlow<StatusUiState> = combine(
    statusRepository.observeOwnStatus(),
    statusRepository.observePartnerStatus(),
    statusRepository.observeWsConnection()
  ) { own, partner, connected ->
    StatusUiState(selectedStatus = own, partnerStatus = partner, wsConnected = connected)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatusUiState())

  fun onEvent(event: StatusUiEvent) {
    when (event) {
      is StatusUiEvent.SelectStatus -> viewModelScope.launch { setStatusUseCase(event.status) }
    }
  }
}

@Composable
fun StatusScreen(viewModel: StatusViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Status check-in",
    subtitle = "Signal your nervous-system state early so the next step can fit the moment."
  ) {
    HeroCard(
      eyebrow = "Yellow-zone support",
      title = "Clarity first, blame later.",
      body = "Choosing a state is information. It does not mean someone is failing."
    ) {
      StatusPill("You: ${uiState.selectedStatus?.name ?: "Unset"}", uiState.selectedStatus)
      StatusPill("Partner: ${uiState.partnerStatus?.name ?: "Unknown"}", uiState.partnerStatus)
      MiniStat("Sync", if (uiState.wsConnected) "Realtime linked" else "Will sync when connection returns")
    }

    BreatheCard {
      SectionTitle("Choose the closest state")
      PrimaryActionButton(text = "Green · Open and steady", onClick = {
        viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.GREEN))
      })
      SecondaryActionButton(text = "Yellow · Tension is rising", onClick = {
        viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.YELLOW))
      })
      SecondaryActionButton(text = "Red · Pause content and regulate", onClick = {
        viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.RED))
      })
    }

    BreatheCard {
      SectionTitle("Meaning")
      Text(
        when (uiState.selectedStatus) {
          StatusLevel.GREEN -> "Green means open, steady, and able to stay in contact."
          StatusLevel.YELLOW -> "Yellow means tension is rising and slower, safer wording is needed."
          StatusLevel.RED -> "Red means stop pushing content and move into calm or timeout."
          null -> "Choose the closest status first. The goal is clarity, not perfection."
        }
      )
    }
  }
}
