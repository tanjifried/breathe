package com.breathe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breathe.data.model.VoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceDao {
  @Query("SELECT * FROM voice_files ORDER BY promptIndex ASC")
  fun observeAll(): Flow<List<VoiceEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(entries: List<VoiceEntity>)
}
