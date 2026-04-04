package com.breathe.domain.usecase

import com.breathe.domain.model.QuickUpdate
import com.breathe.domain.repository.QuickUpdateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuickUpdatesUseCase @Inject constructor(
  private val quickUpdateRepository: QuickUpdateRepository
) {
  operator fun invoke(limit: Int = 12): Flow<List<QuickUpdate>> =
    quickUpdateRepository.observeRecentUpdates(limit)

  suspend fun refresh(limit: Int = 12) = quickUpdateRepository.refresh(limit)
}
