package com.breathe.domain.repository

import com.breathe.domain.model.QuickUpdate
import kotlinx.coroutines.flow.Flow

interface QuickUpdateRepository {
  fun observeRecentUpdates(limit: Int = 12): Flow<List<QuickUpdate>>
  suspend fun refresh(limit: Int = 12)
  suspend fun sendQuickUpdate(presetKey: String, message: String, note: String? = null)
}
