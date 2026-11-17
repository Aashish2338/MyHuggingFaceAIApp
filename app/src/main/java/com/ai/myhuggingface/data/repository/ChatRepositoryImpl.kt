package com.ai.myhuggingface.data.repository

import com.ai.myhuggingface.core.config.AiProviderType
import com.ai.myhuggingface.data.factory.AIProviderFactory
import com.ai.myhuggingface.domain.model.ChatMessage
import com.ai.myhuggingface.domain.model.ChatResult
import com.ai.myhuggingface.domain.repository.ChatRepository

class ChatRepositoryImpl(private val providerFactory: AIProviderFactory) : ChatRepository {
    override suspend fun sendMessage(
        messages: List<ChatMessage>,
        provider: AiProviderType
    ): ChatResult {

        val aiProvider = providerFactory.createProvider(provider)

        val reply = aiProvider.generateResponse(messages)

        return ChatResult(
            reply = reply,
            provider = provider.displayName
        )
    }
}