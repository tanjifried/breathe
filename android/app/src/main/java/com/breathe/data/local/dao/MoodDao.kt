package com.breathe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breathe.data.model.MoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
  @Query("SELECT * FROM moods ORDER BY checkedAt DESC LIMIT :limit")
  fun observeRecent(limit: Int): Flow<List<MoodEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entity: MoodEntity)
}
