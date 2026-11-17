package com.ai.myhuggingface.domain.usecase

import com.ai.myhuggingface.core.config.AiProviderType
import com.ai.myhuggingface.domain.model.ChatMessage
import com.ai.myhuggingface.domain.model.ChatResult
import com.ai.myhuggingface.domain.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(
        messages: List<ChatMessage>,
        provider: AiProviderType
    ): ChatResult {

        require(messages.isNotEmpty()) {
            "Message cannot be empty."
        }

        return repository.sendMessage(
            messages = messages,
            provider = provider
        )
    }
}