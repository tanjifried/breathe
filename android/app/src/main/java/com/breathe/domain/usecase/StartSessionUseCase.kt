package com.breathe.domain.usecase

import com.breathe.domain.model.SessionFeature
import com.breathe.domain.repository.SessionRepository
import javax.inject.Inject

class StartSessionUseCase @Inject constructor(
  private val sessionRepository: SessionRepository
) {
  suspend operator fun invoke(feature: SessionFeature, moodBefore: Int? = null): Long? =
    sessionRepository.startSession(feature, moodBefore)
}
