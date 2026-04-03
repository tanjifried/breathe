package com.breathe.domain.usecase

import com.breathe.domain.model.AuthSession
import com.breathe.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BootstrapSessionUseCase @Inject constructor(
  private val authRepository: AuthRepository
) {
  suspend fun bootstrap() = authRepository.bootstrap()

  operator fun invoke(): Flow<AuthSession> = authRepository.observeSession()
}
