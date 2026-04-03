package com.breathe.domain.repository

import com.breathe.domain.model.WeeklyInsights
import kotlinx.coroutines.flow.Flow

interface InsightsRepository {
  fun observeWeeklyInsights(): Flow<WeeklyInsights>
  suspend fun refresh()
}
