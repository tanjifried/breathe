package com.breathe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
  @PrimaryKey val sessionId: Long,
  val featureUsed: String,
  val startedAt: String,
  val durationSeconds: Int? = null,
  val moodBefore: Int? = null,
  val moodAfter: Int? = null,
  val privateNote: String? = null,
  val shared: Boolean = false
)

@Entity(tableName = "status")
data class StatusEntity(
  @PrimaryKey val scope: String,
  val color: String,
  val updatedAt: String
)

@Entity(tableName = "moods")
data class MoodEntity(
  @PrimaryKey val checkinId: Long,
  val mood: Int,
  val checkedAt: String
)

@Entity(tableName = "voice_files")
data class VoiceEntity(
  @PrimaryKey val id: Long,
  val sessionType: String,
  val promptIndex: Int,
  val filePath: String,
  val durationSeconds: Int? = null,
  val uploadedAt: String
)
