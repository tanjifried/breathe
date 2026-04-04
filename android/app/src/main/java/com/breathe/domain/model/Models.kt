package com.breathe.domain.model

enum class StatusLevel {
  GREEN,
  YELLOW,
  RED;

  fun wireValue(): String = name.lowercase()

  companion object {
    fun fromWire(value: String?): StatusLevel? = entries.firstOrNull { it.wireValue() == value }
  }
}

enum class SessionFeature {
  CALM,
  TIMEOUT;

  fun wireValue(): String = name.lowercase()

  companion object {
    fun fromWire(value: String?): SessionFeature =
      entries.firstOrNull { it.wireValue() == value } ?: CALM
  }
}

data class CalmSession(
  val sessionId: Long? = null,
  val secondsRemaining: Int = 0,
  val startedAt: String? = null,
  val voiceTrack: String? = null
)

data class TimeoutLock(
  val sessionId: Long? = null,
  val secondsRemaining: Int = 0,
  val isLocked: Boolean = false,
  val unlocksAt: String? = null
)

data class ConflictLogEntry(
  val sessionId: Long,
  val feature: SessionFeature,
  val startedAt: String,
  val durationSeconds: Int? = null,
  val moodBefore: Int? = null,
  val moodAfter: Int? = null,
  val privateNote: String? = null,
  val isShared: Boolean = false
)

data class VoicePrompt(
  val label: String,
  val slot: Int
)

data class MoodCheckin(
  val checkinId: Long,
  val mood: Int,
  val checkedAt: String
)

data class AuthSession(
  val userId: Long? = null,
  val username: String? = null,
  val coupleId: Long? = null,
  val pairingCode: String? = null,
  val pairingExpiresAt: String? = null,
  val hasToken: Boolean = false,
  val isPaired: Boolean = false,
  val isOfflineMode: Boolean = false
)

data class WeeklySummary(
  val checkinsLogged: Int = 0,
  val partnerCheckinsLogged: Int = 0,
  val averageMood: Double? = null,
  val partnerAverageMood: Double? = null,
  val sessionsStarted: Int = 0,
  val timeoutSessions: Int = 0,
  val calmSessions: Int = 0,
  val sharedReflections: Int = 0,
  val averageMoodShift: Double? = null,
  val activeCoolingLock: Boolean = false,
  val coolingUntil: String? = null
)

data class MoodTrend(
  val direction: String = "steady",
  val averageMood: Double? = null,
  val partnerAverageMood: Double? = null,
  val averageMoodShift: Double? = null
)

data class WeeklyInsights(
  val headline: String = "",
  val weeklySummary: WeeklySummary = WeeklySummary(),
  val moodTrend: MoodTrend = MoodTrend(),
  val topFeature: String? = null,
  val recommendations: List<String> = emptyList()
)
