package com.breathe.data.remote

import com.breathe.data.local.AuthSessionStorage
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
  private val authSessionStorage: AuthSessionStorage
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val overrideUrl = authSessionStorage.serverUrl().trim().removeSuffix("/").toHttpUrlOrNull()
      ?: return chain.proceed(request)

    val nextUrl = request.url.newBuilder()
      .scheme(overrideUrl.scheme)
      .host(overrideUrl.host)
      .port(overrideUrl.port)
      .build()

    return chain.proceed(
      request.newBuilder()
        .url(nextUrl)
        .build()
    )
  }
}
