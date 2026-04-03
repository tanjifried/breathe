package com.breathe.data.repository

import com.breathe.data.model.WeeklyInsightsResponse
import com.breathe.data.remote.api.SessionApi
import com.breathe.domain.model.MoodTrend
import com.breathe.domain.model.WeeklyInsights
import com.breathe.domain.model.WeeklySummary
import com.breathe.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightsRepositoryImpl @Inject constructor(
  private val sessionApi: SessionApi
) : InsightsRepository {
  private val state = MutableStateFlow(WeeklyInsights())

  override fun observeWeeklyInsights(): Flow<WeeklyInsights> = state.asStateFlow()

  override suspend fun refresh() {
    val remote = runCatching { sessionApi.getWeeklyInsights() }.getOrNull() ?: return
    state.value = remote.toDomain()
  }

  private fun WeeklyInsightsResponse.toDomain(): WeeklyInsights = WeeklyInsights(
    headline = headline,
    topFeature = topFeature,
    weeklySummary = WeeklySummary(
      checkinsLogged = weeklySummary.checkinsLogged,
      partnerCheckinsLogged = weeklySummary.partnerCheckinsLogged,
      averageMood = weeklySummary.averageMood,
      partnerAverageMood = weeklySummary.partnerAverageMood,
      sessionsStarted = weeklySummary.sessionsStarted,
      timeoutSessions = weeklySummary.timeoutSessions,
      calmSessions = weeklySummary.calmSessions,
      sharedReflections = weeklySummary.sharedReflections,
      averageMoodShift = weeklySummary.averageMoodShift,
      activeCoolingLock = weeklySummary.activeCoolingLock,
      coolingUntil = weeklySummary.coolingUntil
    ),
    moodTrend = MoodTrend(
      direction = moodTrend.direction,
      averageMood = moodTrend.averageMood,
      partnerAverageMood = moodTrend.partnerAverageMood,
      averageMoodShift = moodTrend.averageMoodShift
    ),
    recommendations = recommendations
  )
}
