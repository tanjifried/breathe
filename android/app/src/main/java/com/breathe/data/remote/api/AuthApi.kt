package com.breathe.data.remote.api

import com.breathe.data.model.JoinRequest
import com.breathe.data.model.JoinResponse
import com.breathe.data.model.LoginRequest
import com.breathe.data.model.PairResponse
import com.breathe.data.model.RegisterRequest
import com.breathe.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
  @POST("api/register")
  suspend fun register(@Body request: RegisterRequest): RegisterResponse

  @POST("api/login")
  suspend fun login(@Body request: LoginRequest): RegisterResponse

  @POST("api/pair")
  suspend fun pair(): PairResponse

  @POST("api/join")
  suspend fun join(@Body request: JoinRequest): JoinResponse
}
