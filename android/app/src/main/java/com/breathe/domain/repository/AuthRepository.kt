package com.breathe.domain.repository

import com.breathe.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
  fun observeSession(): Flow<AuthSession>
  suspend fun bootstrap()
  suspend fun register(username: String, password: String)
  suspend fun login(username: String, password: String)
  suspend fun createPairingCode()
  suspend fun joinPairingCode(pairingCode: String)
}
