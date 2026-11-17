package com.ai.myhuggingface.data.remote

import com.ai.myhuggingface.domain.model.ChatMessage

interface AiChatProvider {
    suspend fun generateResponse(
        messages: List<ChatMessage>
    ): String
}