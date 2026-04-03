package com.breathe.presentation.ui.insights

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
import com.breathe.domain.model.MoodTrend
import com.breathe.domain.model.WeeklySummary
import com.breathe.domain.usecase.GetInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Weekly Insights")
    Text(uiState.headline.ifBlank { "No insight yet." })
    Text("Top feature: ${uiState.topFeature ?: "none"}")
    Text("Average mood: ${uiState.weeklySummary.averageMood ?: "n/a"}")
    Text("Mood direction: ${uiState.moodTrend.direction}")
    uiState.recommendations.forEach { Text("• $it") }
    Button(onClick = { viewModel.onEvent(InsightsUiEvent.Refresh) }) { Text("Refresh") }
  }
}
