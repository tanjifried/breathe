package com.breathe.di

import com.breathe.BuildConfig
import com.breathe.data.remote.AuthInterceptor
import com.breathe.data.remote.DynamicBaseUrlInterceptor
import com.breathe.data.remote.api.AuthApi
import com.breathe.data.remote.api.QuickUpdateApi
import com.breathe.data.remote.api.SessionApi
import com.breathe.data.remote.api.StatusApi
import com.breathe.data.remote.api.VoiceApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides
  @Singleton
  fun provideGson(): Gson = GsonBuilder().create()

  @Provides
  @Singleton
  fun provideAuthInterceptor(authSessionStorage: com.breathe.data.local.AuthSessionStorage): Interceptor =
    AuthInterceptor(authSessionStorage)

  @Provides
  @Singleton
  fun provideOkHttpClient(
    authInterceptor: Interceptor,
    dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
  ): OkHttpClient {
    val logger = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }

    return OkHttpClient.Builder()
      .pingInterval(25, TimeUnit.SECONDS)
      .addInterceptor(dynamicBaseUrlInterceptor)
      .addInterceptor(authInterceptor)
      .addInterceptor(logger)
      .build()
  }

  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
    Retrofit.Builder()
      .baseUrl(BuildConfig.DEFAULT_SERVER_URL)
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()

  @Provides
  @Singleton
  fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

  @Provides
  @Singleton
  fun provideStatusApi(retrofit: Retrofit): StatusApi = retrofit.create(StatusApi::class.java)

  @Provides
  @Singleton
  fun provideSessionApi(retrofit: Retrofit): SessionApi = retrofit.create(SessionApi::class.java)

  @Provides
  @Singleton
  fun provideVoiceApi(retrofit: Retrofit): VoiceApi = retrofit.create(VoiceApi::class.java)

  @Provides
  @Singleton
  fun provideQuickUpdateApi(retrofit: Retrofit): QuickUpdateApi = retrofit.create(QuickUpdateApi::class.java)
}
