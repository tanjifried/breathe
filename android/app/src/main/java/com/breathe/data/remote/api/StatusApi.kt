package com.breathe.data.remote.api

import com.breathe.data.model.StatusRequest
import com.breathe.data.model.StatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StatusApi {
  @POST("api/status")
  suspend fun setStatus(@Body request: StatusRequest)

  @GET("api/status")
  suspend fun getStatus(): StatusResponse
}
