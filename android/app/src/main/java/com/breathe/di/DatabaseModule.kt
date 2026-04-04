package com.breathe.di

import android.content.Context
import androidx.room.Room
import com.breathe.data.local.BreatheDatabase
import com.breathe.data.local.dao.MoodDao
import com.breathe.data.local.dao.QuickUpdateDao
import com.breathe.data.local.dao.SessionDao
import com.breathe.data.local.dao.StatusDao
import com.breathe.data.local.dao.VoiceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext context: Context): BreatheDatabase =
    Room.databaseBuilder(context, BreatheDatabase::class.java, "breathe.db")
      .fallbackToDestructiveMigration()
      .build()

  @Provides
  fun provideSessionDao(database: BreatheDatabase): SessionDao = database.sessionDao()

  @Provides
  fun provideStatusDao(database: BreatheDatabase): StatusDao = database.statusDao()

  @Provides
  fun provideMoodDao(database: BreatheDatabase): MoodDao = database.moodDao()

  @Provides
  fun provideVoiceDao(database: BreatheDatabase): VoiceDao = database.voiceDao()

  @Provides
  fun provideQuickUpdateDao(database: BreatheDatabase): QuickUpdateDao = database.quickUpdateDao()
}
