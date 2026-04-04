package com.breathe.presentation.ui.timeout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheRed
import com.breathe.presentation.theme.BreatheYellow
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.PrimaryActionButton
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
fun TimeoutScreen(
  onNavigate: (String) -> Unit = {},
  viewModel: TimeoutViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Structured timeout",
    subtitle = "A hard pause for your body first, problem-solving later.",
    showBottomNav = true,
    selectedBottomRoute = null,
    onNavigate = onNavigate
  ) {
    BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.52f)) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {
        Box(
          modifier = Modifier
            .size(82.dp)
            .background(BreatheRed.copy(alpha = 0.16f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = BreatheRed, modifier = Modifier.size(38.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("CURRENT STATE", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheRed)
          Text("Locked: ${if (uiState.isLocked) "Yes" else "No"}", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheInk)
        }

        TimeoutRing(secondsRemaining = uiState.secondsRemaining)
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      BreatheCard(modifier = Modifier.weight(1f), containerColor = BreatheCardSurface) {
        Icon(imageVector = Icons.Rounded.DoorFront, contentDescription = null, tint = BreatheAccentStrong)
        Text("Re-entry window opens at ${uiState.unlocksAt ?: "--:--"}.", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
        Text("Move slowly. There is no rush to return.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = BreatheMutedInk)
      }

      BreatheCard(modifier = Modifier.weight(1f), containerColor = BreatheYellow.copy(alpha = 0.18f)) {
        Icon(imageVector = Icons.Rounded.SelfImprovement, contentDescription = null, tint = Color(0xFF775A00))
        Text("Focus on the exhale. Let your heart rate find its floor.", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(BreatheCanvas, RoundedCornerShape(999.dp))
            .padding(1.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(0.34f)
              .background(Color(0xFF775A00), RoundedCornerShape(999.dp))
              .padding(vertical = 3.dp)
          )
        }
      }
    }

    BreatheCard(containerColor = BreatheCardSurface) {
      Icon(imageVector = Icons.Rounded.Security, contentDescription = null, tint = BreatheAccentStrong)
      Text("Why we pause", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
      Text(
        "In high-arousal moments, the part of our brain responsible for connection and logic goes offline. This structured timeout is not a punishment, but a sanctuary for your nervous system to regulate.",
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        color = BreatheMutedInk
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = BreatheAccentStrong)
        Text("Safe space for both partners", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = BreatheAccentStrong)
      }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      PrimaryActionButton(
        text = "Start timeout",
        onClick = { viewModel.onEvent(TimeoutUiEvent.StartTimeout) },
        enabled = !uiState.isLocked
      )
      Text(
        text = if (uiState.isLocked) "Timeout already in progress" else "The re-entry window is open.",
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        color = BreatheMutedInk
      )
    }
  }
}

@Composable
private fun TimeoutRing(secondsRemaining: Int) {
  val progress = (secondsRemaining.coerceAtLeast(0) / 1_200f).coerceIn(0f, 1f)

  Box(contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      progress = { 1f },
      modifier = Modifier.size(184.dp),
      color = BreatheBorder.copy(alpha = 0.38f),
      strokeWidth = 10.dp
    )
    CircularProgressIndicator(
      progress = { progress },
      modifier = Modifier.size(184.dp),
      color = BreatheRed,
      strokeWidth = 10.dp
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = formatTimeout(secondsRemaining),
        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
        color = BreatheInk
      )
      Text(
        text = "REMAINING",
        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
        color = BreatheMutedInk
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
