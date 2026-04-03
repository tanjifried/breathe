package com.breathe.di

import com.breathe.data.repository.AuthRepositoryImpl
import com.breathe.data.repository.InsightsRepositoryImpl
import com.breathe.data.repository.MoodRepositoryImpl
import com.breathe.data.repository.SessionRepositoryImpl
import com.breathe.data.repository.StatusRepositoryImpl
import com.breathe.data.repository.VoiceRepositoryImpl
import com.breathe.domain.repository.AuthRepository
import com.breathe.domain.repository.InsightsRepository
import com.breathe.domain.repository.MoodRepository
import com.breathe.domain.repository.SessionRepository
import com.breathe.domain.repository.StatusRepository
import com.breathe.domain.repository.VoiceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds
  abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

  @Binds
  abstract fun bindStatusRepository(impl: StatusRepositoryImpl): StatusRepository

  @Binds
  abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

  @Binds
  abstract fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository

  @Binds
  abstract fun bindVoiceRepository(impl: VoiceRepositoryImpl): VoiceRepository

  @Binds
  abstract fun bindInsightsRepository(impl: InsightsRepositoryImpl): InsightsRepository
}
