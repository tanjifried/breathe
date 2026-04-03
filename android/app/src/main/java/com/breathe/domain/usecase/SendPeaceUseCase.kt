package com.breathe.domain.usecase

import com.breathe.domain.repository.SessionRepository
import javax.inject.Inject

class SendPeaceUseCase @Inject constructor(
  private val sessionRepository: SessionRepository
) {
  suspend operator fun invoke() = sessionRepository.sendPeace()
}
