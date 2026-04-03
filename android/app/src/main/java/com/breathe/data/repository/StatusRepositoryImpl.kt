package com.breathe.data.repository

import com.breathe.data.local.dao.StatusDao
import com.breathe.data.model.StatusEntity
import com.breathe.data.model.StatusRequest
import com.breathe.data.remote.api.StatusApi
import com.breathe.data.remote.ws.WebSocketManager
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.StatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepositoryImpl @Inject constructor(
  private val statusDao: StatusDao,
  private val statusApi: StatusApi,
  private val webSocketManager: WebSocketManager
) : StatusRepository {
  override fun observeOwnStatus(): Flow<StatusLevel?> =
    statusDao.observeStatus(SELF_SCOPE).map { StatusLevel.fromWire(it?.color) }

  override fun observePartnerStatus(): Flow<StatusLevel?> =
    statusDao.observeStatus(PARTNER_SCOPE).map { StatusLevel.fromWire(it?.color) }

  override fun observeWsConnection(): Flow<Boolean> = webSocketManager.connectionState

  override suspend fun setStatus(status: StatusLevel) {
    statusDao.upsert(
      StatusEntity(
        scope = SELF_SCOPE,
        color = status.wireValue(),
        updatedAt = Instant.now().toString()
      )
    )

    runCatching {
      statusApi.setStatus(StatusRequest(status.wireValue()))
    }
  }

  private companion object {
    const val SELF_SCOPE = "self"
    const val PARTNER_SCOPE = "partner"
  }
}
