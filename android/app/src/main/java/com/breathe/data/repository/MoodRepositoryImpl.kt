package com.breathe.data.repository

import com.breathe.data.local.dao.MoodDao
import com.breathe.data.model.MoodCheckinRequest
import com.breathe.data.model.MoodEntity
import com.breathe.data.remote.api.SessionApi
import com.breathe.domain.model.MoodCheckin
import com.breathe.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodRepositoryImpl @Inject constructor(
  private val moodDao: MoodDao,
  private val sessionApi: SessionApi
) : MoodRepository {
  override fun observeRecentMoods(limit: Int): Flow<List<MoodCheckin>> =
    moodDao.observeRecent(limit).map { list ->
      list.map {
        MoodCheckin(
          checkinId = it.checkinId,
          mood = it.mood,
          checkedAt = it.checkedAt
        )
      }
    }

  override suspend fun logMood(mood: Int) {
    val localId = System.currentTimeMillis()
    val checkedAt = Instant.now().toString()
    moodDao.insert(MoodEntity(checkinId = localId, mood = mood, checkedAt = checkedAt))

    runCatching {
      sessionApi.postCheckin(MoodCheckinRequest(mood = mood))
    }
  }
}
