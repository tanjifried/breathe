package com.breathe.domain.repository

import com.breathe.domain.model.MoodCheckin
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
  fun observeRecentMoods(limit: Int = 7): Flow<List<MoodCheckin>>
  suspend fun logMood(mood: Int)
}
