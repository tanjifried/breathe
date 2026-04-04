package com.breathe.data.repository

import com.breathe.data.local.dao.SessionDao
import com.breathe.data.model.EndSessionRequest
import com.breathe.data.model.SessionEntity
import com.breathe.data.model.StartSessionRequest
import com.breathe.data.remote.api.SessionApi
import com.breathe.domain.model.CalmSession
import com.breathe.domain.model.ConflictLogEntry
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.model.TimeoutLock
import com.breathe.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
  private val sessionDao: SessionDao,
  private val sessionApi: SessionApi
) : SessionRepository {
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
    sessionDao.observeLatestTimeoutSession().map { entity ->
      if (entity == null) {
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
      return remote.sessionId
    }

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
  }

  override suspend fun sendPeace() = Unit

  private fun unlockAt(startedAt: String, minutes: Long): String? =
    runCatching { Instant.parse(startedAt).plus(Duration.ofMinutes(minutes)).toString() }.getOrNull()

  private fun remainingSeconds(startedAt: String, minutes: Long): Int {
    val unlockAt = runCatching { Instant.parse(startedAt).plus(Duration.ofMinutes(minutes)) }.getOrNull()
      ?: return 0
    return Duration.between(Instant.now(), unlockAt).seconds.coerceAtLeast(0).toInt()
  }
}
