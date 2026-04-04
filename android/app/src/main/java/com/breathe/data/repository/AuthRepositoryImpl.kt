package com.breathe.data.repository

import com.breathe.data.local.AuthSessionStorage
import com.breathe.data.model.JoinRequest
import com.breathe.data.model.LoginRequest
import com.breathe.data.model.RegisterRequest
import com.breathe.data.remote.api.AuthApi
import com.breathe.data.remote.ws.WebSocketManager
import com.breathe.domain.model.AuthSession
import com.breathe.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
  private val authApi: AuthApi,
  private val authSessionStorage: AuthSessionStorage,
  private val webSocketManager: WebSocketManager
) : AuthRepository {
  override fun observeSession(): Flow<AuthSession> = authSessionStorage.observe().map { snapshot ->
    AuthSession(
      userId = snapshot.userId,
      username = snapshot.username,
      coupleId = snapshot.coupleId,
      pairingCode = snapshot.pairingCode,
      pairingExpiresAt = snapshot.pairingExpiresAt,
      hasToken = snapshot.hasToken,
      isPaired = snapshot.isPaired,
      isOfflineMode = snapshot.offlineMode
    )
  }

  override suspend fun bootstrap() {
    connectIfAuthenticated()
  }

  override suspend fun continueOffline() {
    webSocketManager.disconnect()
    authSessionStorage.saveOfflineSession()
  }

  override suspend fun register(username: String, password: String) {
    val response = authApi.register(RegisterRequest(username = username.trim(), password = password))
    authSessionStorage.saveRegistration(response)
    connectIfAuthenticated()
  }

  override suspend fun login(username: String, password: String) {
    val response = authApi.login(LoginRequest(username = username.trim(), password = password))
    authSessionStorage.saveRegistration(response)
    connectIfAuthenticated()
  }

  override suspend fun createPairingCode() {
    val response = authApi.pair()
    authSessionStorage.savePairing(
      coupleId = response.coupleId,
      pairingCode = response.pairingCode,
      expiresAt = response.expiresAt
    )
    connectIfAuthenticated()
  }

  override suspend fun joinPairingCode(pairingCode: String) {
    val response = authApi.join(JoinRequest(pairingCode = pairingCode))
    authSessionStorage.saveJoinedCouple(response.coupleId)
    connectIfAuthenticated()
  }

  private fun connectIfAuthenticated() {
    val token = authSessionStorage.token() ?: return
    webSocketManager.connect(authSessionStorage.serverUrl(), token)
  }
}
