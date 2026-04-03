package com.breathe.domain.usecase

import com.breathe.domain.repository.AuthRepository
import javax.inject.Inject

class JoinPairUseCase @Inject constructor(
  private val authRepository: AuthRepository
) {
  suspend operator fun invoke(pairingCode: String) = authRepository.joinPairingCode(pairingCode)
}
