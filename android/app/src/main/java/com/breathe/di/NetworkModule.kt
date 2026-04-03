package com.breathe.di

import com.breathe.data.remote.ws.WebSocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides
  @Singleton
  fun provideWebSocketManager(okHttpClient: OkHttpClient): WebSocketManager = WebSocketManager(okHttpClient)
}
