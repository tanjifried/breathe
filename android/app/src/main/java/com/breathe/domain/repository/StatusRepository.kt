package com.breathe.domain.repository

import com.breathe.domain.model.StatusLevel
import kotlinx.coroutines.flow.Flow

interface StatusRepository {
  fun observeOwnStatus(): Flow<StatusLevel?>
  fun observePartnerStatus(): Flow<StatusLevel?>
  fun observeWsConnection(): Flow<Boolean>
  suspend fun setStatus(status: StatusLevel)
}
