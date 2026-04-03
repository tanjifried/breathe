package com.breathe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breathe.data.model.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
  @Query("SELECT * FROM sessions WHERE featureUsed = 'calm' AND durationSeconds IS NULL ORDER BY startedAt DESC LIMIT 1")
  fun observeLatestActiveCalmSession(): Flow<SessionEntity?>

  @Query("SELECT * FROM sessions WHERE featureUsed = 'timeout' ORDER BY startedAt DESC LIMIT 1")
  fun observeLatestTimeoutSession(): Flow<SessionEntity?>

  @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
  fun observeAllSessions(): Flow<List<SessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: SessionEntity)

  @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
  suspend fun deleteSession(sessionId: Long)
}
