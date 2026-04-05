package com.breathe.presentation.ui.pair

import com.breathe.BuildConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breathe.domain.usecase.BootstrapSessionUseCase
import com.breathe.domain.usecase.ContinueOfflineUseCase
import com.breathe.domain.usecase.CreatePairingCodeUseCase
import com.breathe.domain.usecase.JoinPairUseCase
import com.breathe.domain.usecase.LoginUseCase
import com.breathe.domain.usecase.RegisterAccountUseCase
import com.breathe.presentation.theme.BreatheAccentStrong
import com.breathe.presentation.theme.BreatheCanvas
import com.breathe.presentation.ui.common.AppScreen
import com.breathe.presentation.ui.common.BreatheCard
import com.breathe.presentation.ui.common.SectionTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

data class PairingUiState(
  val username: String = "",
  val password: String = "",
  val pairingCode: String = "",
  val pairingExpiresAt: String? = null,
  val joinCode: String = "",
  val isRegistered: Boolean = false,
  val isPaired: Boolean = false,
  val isOfflineMode: Boolean = false,
  val isLoading: Boolean = false,
  val error: String? = null
)

sealed interface PairingUiEvent {
  data class UsernameChanged(val value: String) : PairingUiEvent
  data class PasswordChanged(val value: String) : PairingUiEvent
  data class JoinCodeChanged(val value: String) : PairingUiEvent
  data object ContinueOffline : PairingUiEvent
  data object RegisterAccount : PairingUiEvent
  data object SignIn : PairingUiEvent
  data object CreatePairingCode : PairingUiEvent
  data object JoinPair : PairingUiEvent
  data object ClearError : PairingUiEvent
}

@HiltViewModel
class PairingViewModel @Inject constructor(
  private val bootstrapSessionUseCase: BootstrapSessionUseCase,
  private val continueOfflineUseCase: ContinueOfflineUseCase,
  private val registerAccountUseCase: RegisterAccountUseCase,
  private val loginUseCase: LoginUseCase,
  private val createPairingCodeUseCase: CreatePairingCodeUseCase,
  private val joinPairUseCase: JoinPairUseCase
) : ViewModel() {
  private val _uiState = MutableStateFlow(PairingUiState())
  val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      bootstrapSessionUseCase().collect { session ->
        _uiState.update {
          it.copy(
            pairingCode = session.pairingCode.orEmpty(),
            pairingExpiresAt = session.pairingExpiresAt,
            isRegistered = session.hasToken,
            isPaired = session.isPaired,
            isOfflineMode = session.isOfflineMode
          )
        }
      }
    }
  }

  fun onEvent(event: PairingUiEvent) {
    when (event) {
      is PairingUiEvent.UsernameChanged -> {
        _uiState.update { it.copy(username = event.value, error = null) }
      }

      is PairingUiEvent.PasswordChanged -> {
        _uiState.update { it.copy(password = event.value, error = null) }
      }

      is PairingUiEvent.JoinCodeChanged -> {
        _uiState.update { it.copy(joinCode = event.value, error = null) }
      }

      PairingUiEvent.ContinueOffline -> submitContinueOffline()
      PairingUiEvent.RegisterAccount -> submitRegistration()
      PairingUiEvent.SignIn -> submitLogin()
      PairingUiEvent.CreatePairingCode -> submitCreatePairingCode()
      PairingUiEvent.JoinPair -> submitJoinPair()

      PairingUiEvent.ClearError -> {
        _uiState.update { it.copy(error = null) }
      }
    }
  }

  private fun submitRegistration() {
    val username = _uiState.value.username.trim()
    val password = _uiState.value.password

    if (username.isBlank()) {
      _uiState.update { it.copy(error = "Enter a username.") }
      return
    }

    if (password.length < 6) {
      _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
      return
    }

    viewModelScope.launch {
      setLoading(true)
      runCatching {
        registerAccountUseCase(username, password)
      }.onFailure { error ->
        _uiState.update { it.copy(error = error.toUserMessage()) }
      }
      setLoading(false)
    }
  }

  private fun submitCreatePairingCode() {
    if (!_uiState.value.isRegistered) {
      _uiState.update { it.copy(error = "Create your private account first.") }
      return
    }

    viewModelScope.launch {
      setLoading(true)
      runCatching {
        createPairingCodeUseCase()
      }.onFailure { error ->
        _uiState.update { it.copy(error = error.toUserMessage()) }
      }
      setLoading(false)
    }
  }

  private fun submitLogin() {
    val username = _uiState.value.username.trim()
    val password = _uiState.value.password

    if (username.isBlank()) {
      _uiState.update { it.copy(error = "Enter a username.") }
      return
    }

    if (password.isBlank()) {
      _uiState.update { it.copy(error = "Enter your password.") }
      return
    }

    viewModelScope.launch {
      setLoading(true)
      runCatching {
        loginUseCase(username, password)
      }.onFailure { error ->
        _uiState.update { it.copy(error = error.toUserMessage()) }
      }
      setLoading(false)
    }
  }

  private fun submitContinueOffline() {
    viewModelScope.launch {
      setLoading(true)
      runCatching {
        continueOfflineUseCase()
      }.onFailure { error ->
        _uiState.update { it.copy(error = error.toUserMessage()) }
      }
      setLoading(false)
    }
  }

  private fun submitJoinPair() {
    if (!_uiState.value.isRegistered) {
      _uiState.update { it.copy(error = "Create your private account first.") }
      return
    }

    val sanitized = _uiState.value.joinCode.filter(Char::isDigit)
    if (sanitized.length != 6) {
      _uiState.update { it.copy(error = "Enter a 6-digit pairing code.") }
      return
    }

    viewModelScope.launch {
      setLoading(true)
      runCatching {
        joinPairUseCase(sanitized)
      }.onFailure { error ->
        _uiState.update { it.copy(error = error.toUserMessage()) }
      }
      setLoading(false)
    }
  }

  private fun setLoading(isLoading: Boolean) {
    _uiState.update { it.copy(isLoading = isLoading) }
  }

  private fun Throwable.toUserMessage(): String {
    return when (this) {
      is HttpException -> "Server request failed (${code()})."
      else -> message ?: "Something went wrong."
    }
  }
}

@Composable
fun PairingScreen(
  onContinue: () -> Unit,
  viewModel: PairingViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(uiState.isPaired, uiState.pairingCode, uiState.isOfflineMode) {
    if (uiState.isOfflineMode || (uiState.isPaired && uiState.pairingCode.isBlank())) {
      onContinue()
    }
  }

  AppScreen(
    title = "Pair while calm",
    subtitle = "Set up trust before conflict. Use a private account, a pairing code, or continue offline while features are still under construction."
  ) {
    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Private account")
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          value = uiState.username,
          onValueChange = { viewModel.onEvent(PairingUiEvent.UsernameChanged(it)) },
          label = { Text("Username") }
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          value = uiState.password,
          onValueChange = { viewModel.onEvent(PairingUiEvent.PasswordChanged(it)) },
          label = { Text("Password") }
        )

        Button(
          onClick = { viewModel.onEvent(PairingUiEvent.RegisterAccount) },
          enabled = !uiState.isLoading && !uiState.isRegistered,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
        ) {
          Text(if (uiState.isRegistered) "Account ready" else "Create private account")
        }

        Button(
          onClick = { viewModel.onEvent(PairingUiEvent.SignIn) },
          enabled = !uiState.isLoading && !uiState.isRegistered,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Sign in")
        }

        if (BuildConfig.DEBUG) {
          Button(
            onClick = { viewModel.onEvent(PairingUiEvent.ContinueOffline) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Continue offline")
          }
        }
      }
    }

    BreatheCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Pairing code")
        Button(
          onClick = { viewModel.onEvent(PairingUiEvent.CreatePairingCode) },
          enabled = !uiState.isLoading && uiState.isRegistered,
          colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
        ) {
          Text(if (uiState.pairingCode.isBlank()) "Create pairing code" else "Refresh pairing code")
        }

        if (uiState.pairingCode.isNotBlank()) {
          Text("Your code: ${uiState.pairingCode}", style = MaterialTheme.typography.titleMedium)
          uiState.pairingExpiresAt?.let { Text("Expires at: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          value = uiState.joinCode,
          onValueChange = { viewModel.onEvent(PairingUiEvent.JoinCodeChanged(it)) },
          label = { Text("Join code") }
        )

        Button(
          onClick = { viewModel.onEvent(PairingUiEvent.JoinPair) },
          enabled = !uiState.isLoading && uiState.isRegistered
        ) {
          Text("Join pair")
        }

        if (uiState.isRegistered && (uiState.isPaired || uiState.pairingCode.isNotBlank() || uiState.isOfflineMode)) {
          Button(onClick = onContinue, enabled = !uiState.isLoading) {
            Text("Continue to home")
          }
        }
      }
    }

    if (uiState.isLoading) {
      CircularProgressIndicator()
    }

    uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
  }
}
