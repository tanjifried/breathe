package com.breathe.domain.repository

import com.breathe.domain.model.CalmSession
import com.breathe.domain.model.ConflictLogEntry
import com.breathe.domain.model.SessionFeature
import com.breathe.domain.model.TimeoutLock
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
  fun observeActiveCalmSession(): Flow<CalmSession?>
  fun observeTimeoutLock(): Flow<TimeoutLock>
  fun observeConflictLogs(): Flow<List<ConflictLogEntry>>
  suspend fun startSession(feature: SessionFeature, moodBefore: Int? = null): Long?
  suspend fun endSession(
    sessionId: Long,
    moodAfter: Int? = null,
    privateNote: String? = null,
    shared: Boolean = false
  )
  suspend fun sendPeace()
}
