package com.breathe.data.remote.api

import com.breathe.data.model.VoiceFileResponse
import retrofit2.http.GET

interface VoiceApi {
  @GET("api/voice")
  suspend fun getVoiceFiles(): List<VoiceFileResponse>
}
