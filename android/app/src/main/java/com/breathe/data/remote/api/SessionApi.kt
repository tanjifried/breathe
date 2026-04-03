package com.breathe.data.remote.api

import com.breathe.data.model.CheckinsResponse
import com.breathe.data.model.EndSessionRequest
import com.breathe.data.model.MoodCheckinRequest
import com.breathe.data.model.MoodCheckinResponse
import com.breathe.data.model.ReentryLockResponse
import com.breathe.data.model.SessionSummaryResponse
import com.breathe.data.model.StartSessionRequest
import com.breathe.data.model.StartSessionResponse
import com.breathe.data.model.WeeklyInsightsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SessionApi {
  @POST("api/sessions/start")
  suspend fun startSession(@Body request: StartSessionRequest): StartSessionResponse

  @POST("api/sessions/end")
  suspend fun endSession(@Body request: EndSessionRequest): SessionSummaryResponse

  @GET("api/reentry-lock")
  suspend fun getReentryLock(): ReentryLockResponse

  @POST("api/checkins")
  suspend fun postCheckin(@Body request: MoodCheckinRequest): MoodCheckinResponse

  @GET("api/checkins")
  suspend fun getCheckins(@Query("limit") limit: Int): CheckinsResponse

  @GET("api/insights/weekly")
  suspend fun getWeeklyInsights(): WeeklyInsightsResponse
}
