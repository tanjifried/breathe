package com.breathe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.breathe.data.local.dao.MoodDao
import com.breathe.data.local.dao.QuickUpdateDao
import com.breathe.data.local.dao.SessionDao
import com.breathe.data.local.dao.StatusDao
import com.breathe.data.local.dao.VoiceDao
import com.breathe.data.model.MoodEntity
import com.breathe.data.model.QuickUpdateEntity
import com.breathe.data.model.SessionEntity
import com.breathe.data.model.StatusEntity
import com.breathe.data.model.VoiceEntity

@Database(
  entities = [
    SessionEntity::class,
    StatusEntity::class,
    MoodEntity::class,
    VoiceEntity::class,
    QuickUpdateEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class BreatheDatabase : RoomDatabase() {
  abstract fun sessionDao(): SessionDao
  abstract fun statusDao(): StatusDao
  abstract fun moodDao(): MoodDao
  abstract fun voiceDao(): VoiceDao
  abstract fun quickUpdateDao(): QuickUpdateDao
}
