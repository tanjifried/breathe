package com.breathe.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breathe.domain.usecase.BootstrapSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppEntryUiState(
  val isLoading: Boolean = true,
  val targetRoute: String? = null
)

@HiltViewModel
class AppEntryViewModel @Inject constructor(
  private val bootstrapSessionUseCase: BootstrapSessionUseCase
) : ViewModel() {
  private val _uiState = MutableStateFlow(AppEntryUiState())
  val uiState: StateFlow<AppEntryUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      bootstrapSessionUseCase.bootstrap()
      bootstrapSessionUseCase().collect { session ->
        val targetRoute = if (session.isOfflineMode || (session.hasToken && session.isPaired && session.pairingCode.isNullOrBlank())) {
          Screen.Home.route
        } else {
          Screen.Pairing.route
        }

        _uiState.update {
          it.copy(isLoading = false, targetRoute = targetRoute)
        }
      }
    }
  }
}
