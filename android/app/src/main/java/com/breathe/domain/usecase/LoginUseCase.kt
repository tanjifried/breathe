package com.breathe.domain.usecase

import com.breathe.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
  private val authRepository: AuthRepository
) {
  suspend operator fun invoke(username: String, password: String) = authRepository.login(username, password)
}
