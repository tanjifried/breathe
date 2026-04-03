package com.breathe.domain.usecase

import com.breathe.domain.repository.MoodRepository
import javax.inject.Inject

class LogMoodUseCase @Inject constructor(
  private val moodRepository: MoodRepository
) {
  suspend operator fun invoke(mood: Int) = moodRepository.logMood(mood)
}
