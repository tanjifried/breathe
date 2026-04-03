package com.breathe.domain.usecase

import com.breathe.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterAccountUseCase @Inject constructor(
  private val authRepository: AuthRepository
) {
  suspend operator fun invoke(username: String, password: String) = authRepository.register(username, password)
}
