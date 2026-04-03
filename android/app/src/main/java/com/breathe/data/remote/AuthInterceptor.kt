package com.breathe.data.remote

import com.breathe.data.local.AuthSessionStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
  private val authSessionStorage: AuthSessionStorage
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val token = authSessionStorage.token()
    val request = chain.request()

    if (token.isNullOrBlank() || request.header("Authorization") != null) {
      return chain.proceed(request)
    }

    return chain.proceed(
      request.newBuilder()
        .header("Authorization", "Bearer $token")
        .build()
    )
  }
}
