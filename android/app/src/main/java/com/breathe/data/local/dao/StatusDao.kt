package com.breathe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breathe.data.model.StatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
  @Query("SELECT * FROM status WHERE scope = :scope LIMIT 1")
  fun observeStatus(scope: String): Flow<StatusEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: StatusEntity)

  @Query("DELETE FROM status WHERE scope = :scope")
  suspend fun delete(scope: String)
}
