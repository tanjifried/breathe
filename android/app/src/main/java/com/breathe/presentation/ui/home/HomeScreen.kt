package com.breathe.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.QuickUpdate
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.QuickUpdateRepository
import com.breathe.domain.repository.StatusRepository
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.ui.common.ActionTile
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.HeroCard
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
  val wsConnected: Boolean = false,
  val recentUpdate: QuickUpdate? = null
)

sealed interface HomeUiEvent {
  data object NoOp : HomeUiEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
  statusRepository: StatusRepository,
  quickUpdateRepository: QuickUpdateRepository
) : ViewModel() {
  val uiState: StateFlow<HomeUiState> = combine(
    statusRepository.observeOwnStatus(),
    statusRepository.observePartnerStatus(),
    statusRepository.observeWsConnection(),
    quickUpdateRepository.observeRecentUpdates(limit = 1)
  ) { own, partner, connected, updates ->
    HomeUiState(
      ownStatus = own,
      partnerStatus = partner,
      wsConnected = connected,
      recentUpdate = updates.firstOrNull()
    )
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
    subtitle = "A softer command center for slowing things down before conflict starts steering the room."
  ) {
    HeroCard(
      eyebrow = "Live state",
      title = if (uiState.wsConnected) "You are linked in real time." else "Offline-first mode is protecting the essentials.",
      body = "Use the next step that matches the nervous-system state, not the argument urgency."
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatusPill(
          label = "You: ${uiState.ownStatus?.name ?: "Unset"}",
          status = uiState.ownStatus,
          modifier = Modifier.weight(1f)
        )
        StatusPill(
          label = "Partner: ${uiState.partnerStatus?.name ?: "Unknown"}",
          status = uiState.partnerStatus,
          modifier = Modifier.weight(1f)
        )
      }
    }

    BreatheCard {
      SectionTitle("Quick pulse")
      MiniStat("Connection", if (uiState.wsConnected) "Realtime linked" else "Local actions stay available")
      Text(
        uiState.recentUpdate?.let {
          val sender = if (it.isOwn) "You" else "Partner"
          buildString {
            append(sender)
            append(": ")
            append(it.message)
            if (!it.note.isNullOrBlank()) {
              append(" • ")
              append(it.note)
            }
          }
        } ?: "No quick update yet. A small signal can prevent a bigger misunderstanding later."
      )
    }

    BreatheCard {
      SectionTitle("Regulation tools")
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ActionTile(
          title = "Status",
          body = "Signal green, yellow, or red before content gets sharper.",
          onClick = { onNavigate(Screen.Status.route) },
          modifier = Modifier.weight(1f)
        )
        ActionTile(
          title = "Calm",
          body = "Drop your shoulders and reset before replying.",
          onClick = { onNavigate(Screen.Calm.route) },
          modifier = Modifier.weight(1f)
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ActionTile(
          title = "Timeout",
          body = "Use a firmer boundary when the red zone is already here.",
          onClick = { onNavigate(Screen.Timeout.route) },
          modifier = Modifier.weight(1f)
        )
        ActionTile(
          title = "Updates",
          body = "Send a low-pressure signal like on my way or quiet time.",
          onClick = { onNavigate(Screen.Updates.route) },
          modifier = Modifier.weight(1f)
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ActionTile(
          title = "Voice",
          body = "Prepare warm words while calm so they are ready later.",
          onClick = { onNavigate(Screen.Voice.route) },
          modifier = Modifier.weight(1f)
        )
        ActionTile(
          title = "Insights",
          body = "Review patterns without turning the relationship into a score.",
          onClick = { onNavigate(Screen.Insights.route) },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}
