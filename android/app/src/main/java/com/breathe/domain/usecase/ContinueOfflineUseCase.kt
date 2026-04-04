package com.breathe.domain.usecase

import com.breathe.domain.repository.AuthRepository
import javax.inject.Inject

class ContinueOfflineUseCase @Inject constructor(
  private val authRepository: AuthRepository
) {
  suspend operator fun invoke() = authRepository.continueOffline()
}
