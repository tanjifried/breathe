package com.breathe.presentation.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.breathe.domain.model.ConflictLogEntry
import com.breathe.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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
  val uiState: StateFlow<LogUiState> = sessionRepository.observeConflictLogs()
    .map { entries -> LogUiState(entries = entries, selectedEntry = entries.firstOrNull(), isShared = entries.firstOrNull()?.isShared == true) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogUiState())

  fun onEvent(event: LogUiEvent) = Unit
}

@Composable
fun ConflictLogScreen(viewModel: LogViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Conflict Log")
    Text("Entries: ${uiState.entries.size}")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(uiState.entries) { entry ->
        Button(onClick = { viewModel.onEvent(LogUiEvent.SelectEntry(entry.sessionId)) }) {
          Text("${entry.feature.name} · ${entry.startedAt}")
        }
      }
    }
  }
}
