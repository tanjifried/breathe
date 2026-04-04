package com.breathe.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.breathe.data.model.QuickUpdateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickUpdateDao {
  @Query("SELECT * FROM quick_updates ORDER BY datetime(createdAt) DESC, updateId DESC LIMIT :limit")
  fun observeRecent(limit: Int): Flow<List<QuickUpdateEntity>>

  @Upsert
  suspend fun upsert(entity: QuickUpdateEntity)

  @Upsert
  suspend fun upsertAll(entities: List<QuickUpdateEntity>)

  @Query("DELETE FROM quick_updates WHERE updateId = :updateId")
  suspend fun deleteById(updateId: Long)
}
