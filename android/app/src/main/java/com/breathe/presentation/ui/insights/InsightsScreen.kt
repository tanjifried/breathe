package com.breathe.presentation.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.model.MoodTrend
import com.breathe.domain.model.WeeklySummary
import com.breathe.domain.usecase.GetInsightsUseCase
import com.breathe.presentation.navigation.Screen
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheBorder
import com.breathe.presentation.theme.BreatheCardSurface
import com.breathe.presentation.theme.BreatheInk
import com.breathe.presentation.theme.BreatheMutedInk
import com.breathe.presentation.theme.BreatheOverlay
import com.breathe.presentation.theme.BreatheYellow
import com.breathe.presentation.ui.common.AdaptiveTwoPane
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.PrimaryActionButton
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InsightsUiState(
  val weeklySummary: WeeklySummary = WeeklySummary(),
  val moodTrend: MoodTrend = MoodTrend(),
  val topFeature: String? = null,
  val headline: String = "",
  val recommendations: List<String> = emptyList()
)

sealed interface InsightsUiEvent {
  data object Refresh : InsightsUiEvent
}

@HiltViewModel
class InsightsViewModel @Inject constructor(
  private val getInsightsUseCase: GetInsightsUseCase
) : ViewModel() {
  val uiState: StateFlow<InsightsUiState> = getInsightsUseCase()
    .map {
      InsightsUiState(
        weeklySummary = it.weeklySummary,
        moodTrend = it.moodTrend,
        topFeature = it.topFeature,
        headline = it.headline,
        recommendations = it.recommendations
      )
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

  init {
    onEvent(InsightsUiEvent.Refresh)
  }

  fun onEvent(event: InsightsUiEvent) {
    when (event) {
      InsightsUiEvent.Refresh -> viewModelScope.launch { getInsightsUseCase.refresh() }
    }
  }
}

@Composable
fun InsightsScreen(
  onNavigate: (String) -> Unit = {},
  viewModel: InsightsViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Weekly insights",
    subtitle = "A softer review of patterns, not a scoreboard.",
    showBottomNav = true,
    selectedBottomRoute = Screen.Insights.route,
    onNavigate = onNavigate
  ) {
    BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.54f)) {
      if (uiState.headline.isBlank() && uiState.recommendations.isEmpty()) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(imageVector = Icons.Rounded.AutoGraph, contentDescription = null, tint = BreatheBorder)
          Text(
            text = "No weekly insight yet",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = BreatheInk,
            textAlign = TextAlign.Center
          )
          Text(
            text = "As you log calm, status, and timeout moments, a softer reflection will gather here.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = BreatheMutedInk,
            textAlign = TextAlign.Center
          )
        }
      } else {
        Icon(imageVector = Icons.Rounded.AutoGraph, contentDescription = null, tint = BreatheAccentStrong)
        Text(
          text = uiState.headline,
          style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
          color = BreatheInk
        )
        Text(
          text = "This is a weekly reflection on rhythm, not a measure of relationship performance.",
          style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
          color = BreatheMutedInk
        )
      }
    }

    AdaptiveTwoPane(
      first = { paneModifier ->
        InsightStatCard(modifier = paneModifier, label = "Top feature", value = uiState.topFeature ?: "none")
      },
      second = { paneModifier ->
        InsightStatCard(modifier = paneModifier, label = "Average mood", value = uiState.weeklySummary.averageMood?.formatOneDecimal() ?: "n/a")
      }
    )

    AdaptiveTwoPane(
      first = { paneModifier ->
        InsightStatCard(modifier = paneModifier, label = "Mood direction", value = uiState.moodTrend.direction)
      },
      second = { paneModifier ->
        InsightStatCard(modifier = paneModifier, label = "Shared reflections", value = uiState.weeklySummary.sharedReflections.toString())
      }
    )

    BreatheCard(containerColor = BreatheCardSurface) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Rounded.TipsAndUpdates, contentDescription = null, tint = BreatheAccentStrong)
        Text("Recommendations", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
      }

      if (uiState.recommendations.isEmpty()) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "No recommendations yet.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = BreatheMutedInk,
            textAlign = TextAlign.Center
          )
          Text(
            text = "A few more check-ins will give this space something gentle to offer back.",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = BreatheMutedInk,
            textAlign = TextAlign.Center
          )
        }
      } else {
        uiState.recommendations.forEach { recommendation ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(BreatheOverlay.copy(alpha = 0.52f), RoundedCornerShape(18.dp))
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              "•",
              style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
              color = BreatheAccentStrong,
              modifier = Modifier.padding(top = 2.dp)
            )
            Text(
              text = recommendation,
              modifier = Modifier.weight(1f),
              style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
              color = BreatheInk
            )
          }
        }
      }
    }

    BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.18f)) {
      Text(
        text = "This summary is here to steady your view of the week, not to judge it.",
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        color = BreatheInk
      )
    }

    PrimaryActionButton(
      text = "Refresh",
      onClick = { viewModel.onEvent(InsightsUiEvent.Refresh) },
      icon = Icons.Rounded.Refresh
    )
  }
}

@Composable
private fun InsightStatCard(label: String, value: String, modifier: Modifier = Modifier) {
  BreatheCard(modifier = modifier.heightIn(min = 104.dp), containerColor = BreatheCardSurface) {
    Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
    Text(
      text = value,
      style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
      color = BreatheInk,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun Double.formatOneDecimal(): String = String.format("%.1f", this)
