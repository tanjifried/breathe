package com.breathe.data.repository

import com.breathe.domain.model.VoicePrompt
import com.breathe.domain.repository.VoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepositoryImpl @Inject constructor() : VoiceRepository {
  override fun observePrompts(): Flow<List<VoicePrompt>> = flowOf(
    listOf(
      VoicePrompt(label = "Breathe with me for one minute.", slot = 1),
      VoicePrompt(label = "I care about us more than winning this moment.", slot = 2),
      VoicePrompt(label = "Let us slow down and come back when our bodies settle.", slot = 3)
    )
  )
}
