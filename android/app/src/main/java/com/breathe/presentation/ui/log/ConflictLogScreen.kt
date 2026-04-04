package com.breathe.presentation.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.breathe.domain.model.ConflictLogEntry
import com.breathe.domain.repository.SessionRepository
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.MiniStat
import com.breathe.presentation.ui.common.SectionTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LogUiState(
  val entries: List<ConflictLogEntry> = emptyList(),
  val selectedEntry: ConflictLogEntry? = null,
  val isShared: Boolean = false
)

sealed interface LogUiEvent {
  data class SelectEntry(val sessionId: Long) : LogUiEvent
}

@HiltViewModel
class LogViewModel @Inject constructor(
  sessionRepository: SessionRepository
) : ViewModel() {
  private val selectedSessionId = MutableStateFlow<Long?>(null)

  val uiState: StateFlow<LogUiState> = combine(
    sessionRepository.observeConflictLogs(),
    selectedSessionId
  ) { entries, selectedId ->
    val selectedEntry = entries.firstOrNull { it.sessionId == selectedId } ?: entries.firstOrNull()
    LogUiState(
      entries = entries,
      selectedEntry = selectedEntry,
      isShared = selectedEntry?.isShared == true
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogUiState())

  fun onEvent(event: LogUiEvent) {
    when (event) {
      is LogUiEvent.SelectEntry -> selectedSessionId.update { event.sessionId }
    }
  }
}

@Composable
fun ConflictLogScreen(viewModel: LogViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppScreen(
    title = "Conflict log",
    subtitle = "A private record of repair attempts and the shape each difficult moment took."
  ) {
    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Overview")
        MiniStat("Entries", uiState.entries.size.toString())
        MiniStat("Selected", uiState.selectedEntry?.feature?.name ?: "None yet")
        MiniStat("Shared", if (uiState.isShared) "Yes" else "No")
      }
    }

    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Selected entry")
        val entry = uiState.selectedEntry
        if (entry == null) {
          Text("No sessions have been logged yet.")
        } else {
          MiniStat("Feature", entry.feature.name)
          MiniStat("Started", entry.startedAt)
          MiniStat("Duration", entry.durationSeconds?.let { "$it sec" } ?: "Active or unavailable")
          MiniStat("Mood shift", buildMoodShift(entry))
          Text(entry.privateNote ?: "No private note saved for this session.")
        }
      }
    }

    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Entries")
        if (uiState.entries.isEmpty()) {
          Text("Your local log is still empty. Calm and Timeout sessions will appear here.")
        } else {
          LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.entries, key = { it.sessionId }) { entry ->
              Button(
                onClick = { viewModel.onEvent(LogUiEvent.SelectEntry(entry.sessionId)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
              ) {
                Text("${entry.feature.name} · ${entry.startedAt}")
              }
            }
          }
        }
      }
    }
  }
}

private fun buildMoodShift(entry: ConflictLogEntry): String {
  val before = entry.moodBefore
  val after = entry.moodAfter
  return if (before != null && after != null) {
    val delta = after - before
    if (delta >= 0) "+$delta" else delta.toString()
  } else {
    "n/a"
  }
}
