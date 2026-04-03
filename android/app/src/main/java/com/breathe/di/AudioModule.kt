package com.breathe.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class AppRecorder {
  fun start(filePath: String): Result<Unit> = runCatching {
    require(filePath.isNotBlank()) { "filePath must not be blank" }
  }

  fun stop(): Result<Unit> = Result.success(Unit)
}

class AppPlayer {
  fun play(filePath: String): Result<Unit> = runCatching {
    require(filePath.isNotBlank()) { "filePath must not be blank" }
  }

  fun stop(): Result<Unit> = Result.success(Unit)
}

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
  @Provides
  @Singleton
  fun provideRecorder(): AppRecorder = AppRecorder()

  @Provides
  @Singleton
  fun providePlayer(): AppPlayer = AppPlayer()
}
