package com.breathe.domain.usecase

import com.breathe.domain.model.WeeklyInsights
import com.breathe.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInsightsUseCase @Inject constructor(
  private val insightsRepository: InsightsRepository
) {
  operator fun invoke(): Flow<WeeklyInsights> = insightsRepository.observeWeeklyInsights()

  suspend fun refresh() = insightsRepository.refresh()
}
