package com.breathe.domain.usecase

import com.breathe.domain.repository.QuickUpdateRepository
import javax.inject.Inject

class SendQuickUpdateUseCase @Inject constructor(
  private val quickUpdateRepository: QuickUpdateRepository
) {
  suspend operator fun invoke(presetKey: String, message: String, note: String? = null) =
    quickUpdateRepository.sendQuickUpdate(presetKey, message, note)
}
