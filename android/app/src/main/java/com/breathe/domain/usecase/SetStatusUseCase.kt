package com.breathe.domain.usecase

import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.StatusRepository
import javax.inject.Inject

class SetStatusUseCase @Inject constructor(
  private val statusRepository: StatusRepository
) {
  suspend operator fun invoke(status: StatusLevel) = statusRepository.setStatus(status)
}
