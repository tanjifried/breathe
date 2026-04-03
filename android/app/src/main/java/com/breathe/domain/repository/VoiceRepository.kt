package com.breathe.domain.repository

import com.breathe.domain.model.VoicePrompt
import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
  fun observePrompts(): Flow<List<VoicePrompt>>
}
