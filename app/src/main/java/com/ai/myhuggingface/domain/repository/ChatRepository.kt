package com.ai.myhuggingface.domain.repository

import com.ai.myhuggingface.core.config.AiProviderType
import com.ai.myhuggingface.domain.model.ChatMessage
import com.ai.myhuggingface.domain.model.ChatResult

interface ChatRepository {
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        provider: AiProviderType
    ): ChatResult
}