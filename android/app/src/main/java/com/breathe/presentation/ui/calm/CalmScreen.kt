package com.breathe.presentation.ui.calm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.CalmSession
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.usecase.EndSessionUseCase
import com.breathe.domain.usecase.SendPeaceUseCase
import com.breathe.domain.usecase.StartSessionUseCase
import com.breathe.presentation.theme.BreatheAccent
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheGreen
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheYellow
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.PrimaryActionButton
import com.breathe.presentation.ui.common.SecondaryActionButton
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

data class CalmUiState(
  val secondsRemaining: Int = 0,
  val voiceTrack: String? = null,
  val sessionId: Long? = null,
  val isActive: Boolean = false,
  val lastSignalMessage: String? = null
)

sealed interface CalmUiEvent {
  data object StartSession : CalmUiEvent
  data object CompleteSession : CalmUiEvent
  data object SendPeace : CalmUiEvent
}

@HiltViewModel
class CalmViewModel @Inject constructor(
  sessionRepository: SessionRepository,
  private val startSessionUseCase: StartSessionUseCase,
  private val endSessionUseCase: EndSessionUseCase,
  private val sendPeaceUseCase: SendPeaceUseCase
) : ViewModel() {
  private val _uiState = MutableStateFlow(CalmUiState())
  val uiState: StateFlow<CalmUiState> = _uiState.asStateFlow()
  private var countdownJob: Job? = null

  init {
    viewModelScope.launch {
      sessionRepository.observeActiveCalmSession().collect { session ->
        countdownJob?.cancel()
        if (session == null) {
          _uiState.value = CalmUiState(lastSignalMessage = _uiState.value.lastSignalMessage)
        } else {
          _uiState.value = session.toUiState(lastSignalMessage = _uiState.value.lastSignalMessage)
          startCountdown(session.sessionId, session.secondsRemaining)
        }
      }
    }
  }

  fun onEvent(event: CalmUiEvent) {
    when (event) {
      CalmUiEvent.StartSession -> viewModelScope.launch {
        _uiState.update { it.copy(lastSignalMessage = null) }
        startSessionUseCase(SessionFeature.CALM)
      }

      CalmUiEvent.CompleteSession -> viewModelScope.launch {
        _uiState.value.sessionId?.let { endSessionUseCase(it) }
      }

      CalmUiEvent.SendPeace -> viewModelScope.launch {
        sendPeaceUseCase()
        _uiState.update { it.copy(lastSignalMessage = "A gentle peace signal was sent to your partner.") }
      }
    }
  }

  private fun startCountdown(sessionId: Long?, initialSeconds: Int) {
    if (sessionId == null) return

    countdownJob = viewModelScope.launch {
      var remaining = initialSeconds
      while (remaining > 0) {
        delay(1_000)
        remaining -= 1
        _uiState.update {
          if (it.sessionId == sessionId) it.copy(secondsRemaining = remaining.coerceAtLeast(0)) else it
        }
      }
      endSessionUseCase(sessionId)
    }
  }

  private fun CalmSession?.toUiState(lastSignalMessage: String?): CalmUiState = CalmUiState(
    secondsRemaining = this?.secondsRemaining ?: 0,
    voiceTrack = this?.voiceTrack,
    sessionId = this?.sessionId,
    isActive = this?.sessionId != null && this.secondsRemaining > 0,
    lastSignalMessage = lastSignalMessage
  )
}

@Composable
fun CalmScreen(
  onNavigate: (String) -> Unit = {},
  viewModel: CalmViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Calm session",
    subtitle = "A soft reset before words get faster than your nervous system can handle.",
    showBottomNav = true,
    selectedBottomRoute = null,
    onNavigate = onNavigate
  ) {
    BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.48f)) {
      Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
        CalmOrb(secondsRemaining = uiState.secondsRemaining)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "SESSION DETAILS",
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = BreatheMutedInk
          )
          SessionInfoRow(icon = Icons.Rounded.Mic, label = "Voice track", value = uiState.voiceTrack ?: "Not selected")
          SessionInfoRow(icon = Icons.Rounded.Timer, label = "Seconds remaining", value = uiState.secondsRemaining.toString())
        }
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      PrimaryActionButton(
        text = if (uiState.isActive) "Calm session active" else "Start calm session",
        onClick = { viewModel.onEvent(CalmUiEvent.StartSession) },
        enabled = !uiState.isActive
      )

      if (uiState.isActive) {
        SecondaryActionButton(
          text = "Complete calm session",
          onClick = { viewModel.onEvent(CalmUiEvent.CompleteSession) }
        )
      }

      SecondaryActionButton(
        text = "Send peace",
        onClick = { viewModel.onEvent(CalmUiEvent.SendPeace) },
        icon = Icons.Rounded.Favorite
      )
    }

    uiState.lastSignalMessage?.let { message ->
      BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.16f)) {
        Text(message, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = BreatheInk)
      }
    }
  }
}

@Composable
private fun CalmOrb(secondsRemaining: Int) {
  BoxWithConstraints(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    val outerSize = maxWidth.coerceAtMost(268.dp)
    val middleSize = outerSize * 0.88f
    val innerSize = outerSize * 0.78f

    Box(
      modifier = Modifier
        .size(outerSize)
        .background(BreatheAccent.copy(alpha = 0.08f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(middleSize)
          .background(BreatheAccent.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(innerSize)
            .background(
              brush = Brush.linearGradient(
                colors = listOf(BreatheCardSurface, BreatheGreen.copy(alpha = 0.18f), BreatheYellow.copy(alpha = 0.18f))
              ),
              shape = CircleShape
            )
            .border(1.dp, BreatheBorder.copy(alpha = 0.4f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = formatMinutes(secondsRemaining),
              style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
              color = BreatheAccentStrong
            )
            Text(
              text = "MINUTES",
              style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
              color = BreatheAccentStrong.copy(alpha = 0.6f)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SessionInfoRow(icon: ImageVector, label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 72.dp)
      .background(BreatheCardSurface, RoundedCornerShape(18.dp))
      .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .background(BreatheAccent.copy(alpha = 0.14f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = BreatheAccentStrong)
    }

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
      Text(
        text = value,
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        color = BreatheInk,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

private fun formatMinutes(seconds: Int): String {
  val safeSeconds = seconds.coerceAtLeast(0)
  val minutes = safeSeconds / 60
  val remainder = safeSeconds % 60
  return "%02d:%02d".format(minutes, remainder)
}
