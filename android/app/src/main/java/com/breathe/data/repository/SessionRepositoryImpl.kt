package com.breathe.data.repository

import com.breathe.data.local.dao.SessionDao
import com.breathe.data.model.EndSessionRequest
import com.breathe.data.model.SessionEntity
import com.breathe.data.model.StartSessionRequest
import com.breathe.data.remote.api.SessionApi
import com.breathe.data.remote.ws.WebSocketManager
import com.breathe.data.remote.ws.WsEvent
import com.breathe.domain.model.CalmSession
import com.breathe.domain.model.ConflictLogEntry
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.model.TimeoutLock
import com.breathe.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
  private val sessionDao: SessionDao,
  private val sessionApi: SessionApi,
  private val webSocketManager: WebSocketManager
) : SessionRepository {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val remoteLock = MutableStateFlow(TimeoutLock())

  init {
    scope.launch {
      refreshRemoteLock()
    }

    scope.launch {
      webSocketManager.events.collect { event ->
        when (event) {
          WsEvent.Connected -> refreshRemoteLock()
          is WsEvent.Message -> if (event.type == SESSION_STARTED_TYPE || event.type == SESSION_ENDED_TYPE) {
            refreshRemoteLock()
          }

          WsEvent.Closed,
          is WsEvent.Failure -> Unit
        }
      }
    }
  }

  override fun observeActiveCalmSession(): Flow<CalmSession?> =
    sessionDao.observeLatestActiveCalmSession().map { entity ->
      entity?.let {
        CalmSession(
          sessionId = it.sessionId,
          secondsRemaining = remainingSeconds(it.startedAt, minutes = 20),
          startedAt = it.startedAt
        )
      }
    }

  override fun observeTimeoutLock(): Flow<TimeoutLock> =
    combine(sessionDao.observeLatestTimeoutSession(), remoteLock) { entity, remote ->
      val local = if (entity == null) {
        TimeoutLock()
      } else {
        val unlockAt = unlockAt(entity.startedAt, minutes = 20)
        val secondsRemaining = remainingSeconds(entity.startedAt, minutes = 20)
        TimeoutLock(
          sessionId = entity.sessionId,
          secondsRemaining = secondsRemaining,
          isLocked = secondsRemaining > 0,
          unlocksAt = unlockAt
        )
      }

      when {
        local.isLocked && local.secondsRemaining >= remote.secondsRemaining -> local
        remote.isLocked -> remote
        else -> local
      }
    }

  override fun observeConflictLogs(): Flow<List<ConflictLogEntry>> =
    sessionDao.observeAllSessions().map { list ->
      list.map {
        ConflictLogEntry(
          sessionId = it.sessionId,
          feature = SessionFeature.fromWire(it.featureUsed),
          startedAt = it.startedAt,
          durationSeconds = it.durationSeconds,
          moodBefore = it.moodBefore,
          moodAfter = it.moodAfter,
          privateNote = it.privateNote,
          isShared = it.shared
        )
      }
    }

  override suspend fun startSession(feature: SessionFeature, moodBefore: Int?): Long? {
    when (feature) {
      SessionFeature.CALM -> {
        sessionDao.getLatestActiveCalmSession()?.let { return it.sessionId }
      }

      SessionFeature.TIMEOUT -> {
        sessionDao.getLatestTimeoutSession()?.let {
          if (remainingSeconds(it.startedAt, minutes = 20) > 0) {
            return it.sessionId
          }
        }
      }
    }

    val provisionalId = System.currentTimeMillis()
    val startedAt = Instant.now().toString()
    sessionDao.upsert(
      SessionEntity(
        sessionId = provisionalId,
        featureUsed = feature.wireValue(),
        startedAt = startedAt,
        moodBefore = moodBefore
      )
    )

    val remote = runCatching {
      sessionApi.startSession(StartSessionRequest(featureUsed = feature.wireValue(), moodBefore = moodBefore))
    }.getOrNull()

    if (remote != null && remote.sessionId != provisionalId) {
      sessionDao.deleteSession(provisionalId)
      sessionDao.upsert(
        SessionEntity(
          sessionId = remote.sessionId,
          featureUsed = feature.wireValue(),
          startedAt = remote.startedAt,
          moodBefore = moodBefore
        )
      )
      refreshRemoteLock()
      return remote.sessionId
    }

    refreshRemoteLock()
    return provisionalId
  }

  override suspend fun endSession(
    sessionId: Long,
    moodAfter: Int?,
    privateNote: String?,
    shared: Boolean
  ) {
    val existing = sessionDao.getSessionById(sessionId)
    if (existing != null) {
      val endedAt = Instant.now()
      val computedDuration = runCatching {
        Duration.between(Instant.parse(existing.startedAt), endedAt).seconds.coerceAtLeast(0).toInt()
      }.getOrDefault(existing.durationSeconds ?: 0)

      sessionDao.upsert(
        existing.copy(
          durationSeconds = existing.durationSeconds ?: computedDuration,
          moodAfter = moodAfter ?: existing.moodAfter,
          privateNote = privateNote ?: existing.privateNote,
          shared = shared || existing.shared
        )
      )
    }

    runCatching {
      sessionApi.endSession(
        EndSessionRequest(
          sessionId = sessionId,
          moodAfter = moodAfter,
          privateNote = privateNote,
          shared = shared
        )
      )
    }

    refreshRemoteLock()
  }

  override suspend fun sendPeace() {
    runCatching { sessionApi.sendPeace() }
  }

  private suspend fun refreshRemoteLock() {
    val payload = runCatching { sessionApi.getReentryLock() }.getOrNull()

    remoteLock.value = if (payload?.locked == true) {
      val until = payload.until
      val remainingSeconds = until?.let { remainingSecondsUntil(it) } ?: 0
      TimeoutLock(
        sessionId = null,
        secondsRemaining = remainingSeconds,
        isLocked = remainingSeconds > 0,
        unlocksAt = until
      )
    } else {
      TimeoutLock()
    }
  }

  private fun unlockAt(startedAt: String, minutes: Long): String? =
    runCatching { Instant.parse(startedAt).plus(Duration.ofMinutes(minutes)).toString() }.getOrNull()

  private fun remainingSeconds(startedAt: String, minutes: Long): Int {
    val unlockAt = runCatching { Instant.parse(startedAt).plus(Duration.ofMinutes(minutes)) }.getOrNull()
      ?: return 0
    return Duration.between(Instant.now(), unlockAt).seconds.coerceAtLeast(0).toInt()
  }

  private fun remainingSecondsUntil(until: String): Int {
    val unlockAt = runCatching { Instant.parse(until) }.getOrNull() ?: return 0
    return Duration.between(Instant.now(), unlockAt).seconds.coerceAtLeast(0).toInt()
  }

  private companion object {
    const val SESSION_STARTED_TYPE = "SESSION_STARTED"
    const val SESSION_ENDED_TYPE = "SESSION_ENDED"
  }
}
