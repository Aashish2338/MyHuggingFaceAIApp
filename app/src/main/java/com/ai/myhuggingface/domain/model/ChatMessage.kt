package com.ai.myhuggingface.domain.model

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val text: String,
    val role: MessageRole
)
