package com.breathe.data.remote.api

import com.breathe.data.model.QuickUpdateRequest
import com.breathe.data.model.QuickUpdateResponse
import com.breathe.data.model.QuickUpdatesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface QuickUpdateApi {
  @GET("api/quick-updates")
  suspend fun getQuickUpdates(@Query("limit") limit: Int): QuickUpdatesResponse

  @POST("api/quick-updates")
  suspend fun postQuickUpdate(@Body request: QuickUpdateRequest): QuickUpdateResponse
}
