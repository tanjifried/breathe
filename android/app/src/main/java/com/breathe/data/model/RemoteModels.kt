package com.breathe.data.model

data class RegisterRequest(
  val username: String,
  val password: String
)

data class LoginRequest(
  val username: String,
  val password: String
)

data class RegisterResponse(
  val token: String,
  val user: AuthUserDto
)

data class AuthUserDto(
  val id: Long,
  val username: String
)

data class PairResponse(
  val coupleId: Long,
  val pairingCode: String,
  val expiresAt: String
)

data class JoinRequest(
  val pairingCode: String
)

data class JoinResponse(
  val coupleId: Long,
  val pairedWith: Long
)

data class StatusRequest(
  val color: String
)

data class StatusItemResponse(
  val color: String? = null,
  val updatedAt: String? = null,
  val userId: Long? = null
)

data class StatusResponse(
  val me: StatusItemResponse? = null,
  val partner: StatusItemResponse? = null
)

data class StartSessionRequest(
  val featureUsed: String,
  val moodBefore: Int? = null
)

data class StartSessionResponse(
  val sessionId: Long,
  val startedAt: String,
  val locked: Boolean,
  val until: String? = null
)

data class EndSessionRequest(
  val sessionId: Long,
  val moodAfter: Int? = null,
  val privateNote: String? = null,
  val shared: Boolean = false
)

data class SessionSummaryResponse(
  val sessionId: Long,
  val coupleId: Long? = null,
  val triggeredByUserId: Long? = null,
  val featureUsed: String,
  val startedAt: String,
  val durationSeconds: Int? = null,
  val moodBefore: Int? = null,
  val moodAfter: Int? = null,
  val privateNote: String? = null,
  val shared: Boolean = false
)

data class ReentryLockResponse(
  val locked: Boolean,
  val until: String? = null
)

data class MoodCheckinRequest(
  val mood: Int
)

data class MoodCheckinResponse(
  val checkinId: Long,
  val mood: Int,
  val checkedAt: String
)

data class CheckinsResponse(
  val entries: List<MoodCheckinResponse> = emptyList(),
  val total: Int = 0
)

data class WeeklySummaryResponse(
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

data class MoodTrendResponse(
  val direction: String = "steady",
  val averageMood: Double? = null,
  val partnerAverageMood: Double? = null,
  val averageMoodShift: Double? = null
)

data class WeeklyInsightsResponse(
  val periodStart: String? = null,
  val periodEnd: String? = null,
  val headline: String = "",
  val topFeature: String? = null,
  val dataCompleteness: String? = null,
  val weeklySummary: WeeklySummaryResponse = WeeklySummaryResponse(),
  val moodTrend: MoodTrendResponse = MoodTrendResponse(),
  val patterns: List<String> = emptyList(),
  val recommendations: List<String> = emptyList()
)

data class VoiceFileResponse(
  val id: Long,
  val promptIndex: Int,
  val sessionType: String,
  val filePath: String,
  val durationSeconds: Int? = null,
  val uploadedAt: String
)
