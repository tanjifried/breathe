package com.breathe.data.repository

import com.breathe.data.local.AuthSessionStorage
import com.breathe.data.local.dao.QuickUpdateDao
import com.breathe.data.model.QuickUpdateEntity
import com.breathe.data.model.QuickUpdateRequest
import com.breathe.data.model.QuickUpdateResponse
import com.breathe.data.remote.api.QuickUpdateApi
import com.breathe.data.remote.ws.WebSocketManager
import com.breathe.data.remote.ws.WsEvent
import com.breathe.domain.model.QuickUpdate
import com.breathe.domain.repository.QuickUpdateRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickUpdateRepositoryImpl @Inject constructor(
  private val quickUpdateDao: QuickUpdateDao,
  private val quickUpdateApi: QuickUpdateApi,
  private val authSessionStorage: AuthSessionStorage,
  private val webSocketManager: WebSocketManager,
  private val gson: Gson
) : QuickUpdateRepository {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  init {
    scope.launch {
      refresh()
    }

    scope.launch {
      webSocketManager.events.collect { event ->
        when (event) {
          WsEvent.Connected -> refresh()
          is WsEvent.Message -> if (event.type == QUICK_UPDATE_TYPE) {
            gson.fromJson(event.raw, QuickUpdateResponse::class.java)
              ?.let { quickUpdateDao.upsert(it.toEntity()) }
          }

          WsEvent.Closed,
          is WsEvent.Failure -> Unit
        }
      }
    }
  }

  override fun observeRecentUpdates(limit: Int): Flow<List<QuickUpdate>> =
    quickUpdateDao.observeRecent(limit).map { list ->
      list.map {
        QuickUpdate(
          updateId = it.updateId,
          presetKey = it.presetKey,
          message = it.message,
          note = it.note,
          createdAt = it.createdAt,
          isOwn = it.isOwn
        )
      }
    }

  override suspend fun refresh(limit: Int) {
    val payload = runCatching { quickUpdateApi.getQuickUpdates(limit) }.getOrNull() ?: return
    quickUpdateDao.upsertAll(payload.entries.map { it.toEntity() })
  }

  override suspend fun sendQuickUpdate(presetKey: String, message: String, note: String?) {
    val provisionalId = System.currentTimeMillis()
    val provisional = QuickUpdateEntity(
      updateId = provisionalId,
      senderUserId = authSessionStorage.userId() ?: 0L,
      presetKey = presetKey,
      message = message,
      note = note?.trim()?.takeIf { it.isNotEmpty() },
      createdAt = java.time.Instant.now().toString(),
      isOwn = true
    )
    quickUpdateDao.upsert(provisional)

    val remote = runCatching {
      quickUpdateApi.postQuickUpdate(
        QuickUpdateRequest(
          presetKey = presetKey,
          message = message,
          note = note?.trim()?.takeIf { it.isNotEmpty() }
        )
      )
    }.getOrNull() ?: return

    quickUpdateDao.deleteById(provisionalId)
    quickUpdateDao.upsert(remote.toEntity())
  }

  private fun QuickUpdateResponse.toEntity(): QuickUpdateEntity = QuickUpdateEntity(
    updateId = updateId,
    senderUserId = senderUserId,
    presetKey = presetKey,
    message = message,
    note = note,
    createdAt = createdAt,
    isOwn = senderUserId == authSessionStorage.userId()
  )

  private companion object {
    const val QUICK_UPDATE_TYPE = "QUICK_UPDATE"
  }
}
