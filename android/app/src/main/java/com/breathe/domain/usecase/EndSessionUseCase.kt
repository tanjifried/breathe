package com.breathe.domain.usecase

import com.breathe.domain.repository.SessionRepository
import javax.inject.Inject

class EndSessionUseCase @Inject constructor(
  private val sessionRepository: SessionRepository
) {
  suspend operator fun invoke(
    sessionId: Long,
    moodAfter: Int? = null,
    privateNote: String? = null,
    shared: Boolean = false
  ) = sessionRepository.endSession(sessionId, moodAfter, privateNote, shared)
}
