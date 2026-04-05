package com.breathe.presentation.ui.status

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.StatusRepository
import com.breathe.domain.usecase.SetStatusUseCase
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheGreen
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheRed
import com.breathe.presentation.theme.BreatheYellow
import com.breathe.presentation.ui.common.AdaptiveTwoPane
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.PrimaryActionButton
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
fun StatusScreen(
  onNavigate: (String) -> Unit = {},
  viewModel: StatusViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var draftStatus by remember { mutableStateOf(uiState.selectedStatus) }

  LaunchedEffect(uiState.selectedStatus) {
    if (draftStatus == null || draftStatus == uiState.selectedStatus) {
      draftStatus = uiState.selectedStatus
    }
  }

  AppScreen(
    title = "Status check-in",
    subtitle = "Signal your nervous-system state early so the next step can fit the moment.",
    showBottomNav = true,
    selectedBottomRoute = Screen.Status.route,
    onNavigate = onNavigate
  ) {
    AdaptiveTwoPane(
      first = { paneModifier ->
        StatusAvatarCard(
          modifier = paneModifier,
          heading = "You",
          label = statusPersonaLabel(uiState.selectedStatus),
          accent = statusAccent(uiState.selectedStatus),
          icon = statusIcon(uiState.selectedStatus)
        )
      },
      second = { paneModifier ->
        StatusAvatarCard(
          modifier = paneModifier,
          heading = "Partner",
          label = statusPersonaLabel(uiState.partnerStatus),
          accent = statusAccent(uiState.partnerStatus),
          icon = statusIcon(uiState.partnerStatus)
        )
      }
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(
        text = "How are you feeling right now?",
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
        color = BreatheInk
      )

      StatusOptionCard(
        title = "Green: Safe & Open",
        body = "I feel regulated and ready for connection.",
        accent = BreatheGreen,
        selected = draftStatus == StatusLevel.GREEN,
        onClick = { draftStatus = StatusLevel.GREEN },
        icon = Icons.Rounded.Spa
      )
      StatusOptionCard(
        title = "Yellow: Feeling Stretched",
        body = "I'm managing, but approaching my limit.",
        accent = BreatheYellow,
        selected = draftStatus == StatusLevel.YELLOW,
        onClick = { draftStatus = StatusLevel.YELLOW },
        icon = Icons.Rounded.Favorite
      )
      StatusOptionCard(
        title = "Red: Dysregulated / Shutting Down",
        body = "I need to stop and regulate before continuing.",
        accent = BreatheRed,
        selected = draftStatus == StatusLevel.RED,
        onClick = { draftStatus = StatusLevel.RED },
        icon = Icons.Rounded.Timer
      )
    }

    SelectionNudgeCard(status = draftStatus)

    PrimaryActionButton(
      text = "Update Status",
      onClick = { draftStatus?.let { viewModel.onEvent(StatusUiEvent.SelectStatus(it)) } },
      enabled = draftStatus != null && draftStatus != uiState.selectedStatus
    )
  }
}

@Composable
private fun StatusAvatarCard(
  heading: String,
  label: String,
  accent: Color,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  BreatheCard(modifier = modifier.heightIn(min = 188.dp), containerColor = BreatheOverlay.copy(alpha = 0.52f)) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .background(BreatheCardSurface, CircleShape)
          .border(4.dp, accent.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
      }
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = heading.uppercase(),
          style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
          color = accent,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = label,
          style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
          color = BreatheInk,
          textAlign = TextAlign.Center,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
private fun StatusOptionCard(
  title: String,
  body: String,
  accent: Color,
  selected: Boolean,
  onClick: () -> Unit,
  icon: ImageVector
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        if (selected) accent.copy(alpha = 0.16f) else BreatheOverlay.copy(alpha = 0.45f),
        RoundedCornerShape(24.dp)
      )
      .border(
        if (selected) 2.dp else 1.dp,
        if (selected) accent else BreatheBorder.copy(alpha = 0.18f),
        RoundedCornerShape(24.dp)
      )
      .heightIn(min = 112.dp)
      .clip(RoundedCornerShape(24.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.Top
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .background(accent.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = accent)
      }
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          title,
          style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
          color = accent,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = body,
          style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
          color = BreatheMutedInk,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Icon(
      imageVector = Icons.Rounded.Favorite,
      contentDescription = null,
      tint = if (selected) accent else BreatheBorder,
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}

@Composable
private fun SelectionNudgeCard(status: StatusLevel?) {
  val (accent, title, body) = when (status) {
    StatusLevel.GREEN -> Triple(
      BreatheGreen,
      "Choosing Green...",
      "You feel open enough to stay in contact. This is a good moment for warmth, simplicity, and clear repair."
    )
    StatusLevel.YELLOW -> Triple(
      BreatheYellow,
      "Choosing Yellow...",
      "This is a signal to slow down. Keep interactions gentle and avoid heavier topics until both bodies settle."
    )
    StatusLevel.RED -> Triple(
      BreatheRed,
      "Choosing Red...",
      "This means stop pushing content. Shift into Calm or Timeout so the next interaction can be safer and softer."
    )
    null -> Triple(
      BreatheAccentStrong,
      "Choosing a state...",
      "A status is information, not failure. Pick the closest one so the next step can fit the moment."
    )
  }

  BreatheCard(containerColor = accent.copy(alpha = 0.12f)) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(accent.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = Icons.Rounded.Lightbulb, contentDescription = null, tint = accent)
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheInk)
        Text(body, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
      }
    }
  }
}

private fun statusPersonaLabel(status: StatusLevel?): String = when (status) {
  StatusLevel.GREEN -> "Grounded"
  StatusLevel.YELLOW -> "Stretched"
  StatusLevel.RED -> "Flooded"
  null -> "Checking in"
}

private fun statusAccent(status: StatusLevel?): Color = when (status) {
  StatusLevel.GREEN -> BreatheGreen
  StatusLevel.YELLOW -> BreatheYellow
  StatusLevel.RED -> BreatheRed
  null -> BreatheAccentStrong
}

private fun statusIcon(status: StatusLevel?): ImageVector = when (status) {
  StatusLevel.GREEN -> Icons.Rounded.Spa
  StatusLevel.YELLOW -> Icons.Rounded.Favorite
  StatusLevel.RED -> Icons.Rounded.Timer
  null -> Icons.Rounded.Favorite
}
