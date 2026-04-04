package com.breathe.data.repository

import com.breathe.data.local.dao.StatusDao
import com.breathe.data.model.StatusEntity
import com.breathe.data.model.StatusRequest
import com.breathe.data.remote.api.StatusApi
import com.breathe.data.remote.ws.WsEvent
import com.breathe.data.remote.ws.WebSocketManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.breathe.domain.model.StatusLevel
import com.breathe.domain.repository.StatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepositoryImpl @Inject constructor(
  private val statusDao: StatusDao,
  private val statusApi: StatusApi,
  private val webSocketManager: WebSocketManager,
  private val gson: Gson
) : StatusRepository {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  init {
    scope.launch {
      refreshFromServer()
    }

    scope.launch {
      webSocketManager.events.collect { event ->
        when (event) {
          WsEvent.Connected -> refreshFromServer()
          is WsEvent.Message -> {
            if (event.type == STATUS_UPDATE_TYPE) {
              applyPartnerStatusUpdate(event.raw)
            }
          }

          WsEvent.Closed,
          is WsEvent.Failure -> Unit
        }
      }
    }
  }

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

  private suspend fun refreshFromServer() {
    val payload = runCatching { statusApi.getStatus() }.getOrNull() ?: return
    val ownColor = payload.me?.color
    val ownUpdatedAt = payload.me?.updatedAt ?: Instant.now().toString()
    val partnerColor = payload.partner?.color
    val partnerUpdatedAt = payload.partner?.updatedAt ?: Instant.now().toString()

    if (ownColor.isNullOrBlank()) {
      statusDao.delete(SELF_SCOPE)
    } else {
      statusDao.upsert(
        StatusEntity(
          scope = SELF_SCOPE,
          color = ownColor,
          updatedAt = ownUpdatedAt
        )
      )
    }

    if (partnerColor.isNullOrBlank()) {
      statusDao.delete(PARTNER_SCOPE)
    } else {
      statusDao.upsert(
        StatusEntity(
          scope = PARTNER_SCOPE,
          color = partnerColor,
          updatedAt = partnerUpdatedAt
        )
      )
    }
  }

  private suspend fun applyPartnerStatusUpdate(raw: String) {
    val payload = runCatching { gson.fromJson(raw, JsonObject::class.java) }.getOrNull() ?: return
    val color = payload.get("color")?.asString?.takeIf { it.isNotBlank() } ?: return
    val updatedAt = payload.get("updatedAt")?.asString ?: Instant.now().toString()

    statusDao.upsert(
      StatusEntity(
        scope = PARTNER_SCOPE,
        color = color,
        updatedAt = updatedAt
      )
    )
  }

  private companion object {
    const val SELF_SCOPE = "self"
    const val PARTNER_SCOPE = "partner"
    const val STATUS_UPDATE_TYPE = "STATUS_UPDATE"
  }
}
