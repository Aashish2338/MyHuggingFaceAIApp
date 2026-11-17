package com.ai.myhuggingface.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.myhuggingface.core.config.AiProviderPreferences
import com.ai.myhuggingface.core.config.AiProviderType
import com.ai.myhuggingface.data.remote.AiApiException
import com.ai.myhuggingface.domain.model.ChatMessage
import com.ai.myhuggingface.domain.model.MessageRole
import com.ai.myhuggingface.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiMessage(
    val text: String, val isUser: Boolean
)

data class ChatError(
    val message: String,
    val isRetryable: Boolean
)

class AiChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val preferences: AiProviderPreferences
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val messages = _messages.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<ChatError?>(null)
    val error = _error.asStateFlow()

    val selectedProvider: StateFlow<AiProviderType> = preferences.selectedProvider.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiProviderType.HUGGING_FACE
    )

    private var conversation = mutableListOf<ChatMessage>()

    private var lastFailedMessages: List<ChatMessage>? = null

    fun selectProvider(provider: AiProviderType) {
        viewModelScope.launch {
            preferences.saveProvider(provider)
            _error.value = null
        }
    }

    fun sendMessage(message: String) {

        val prompt = message.trim()

        if (prompt.isBlank() || _isLoading.value) return

        _messages.update {
            it + ChatUiMessage(
                text = prompt, isUser = true
            )
        }

        conversation.add(
            ChatMessage(
                text = prompt, role = MessageRole.USER
            )
        )

        lastFailedMessages = conversation.toList()

        requestAI(conversation.toList())
    }

    private fun requestAI(
        requestMessages: List<ChatMessage>
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                val result = sendMessageUseCase(
                    messages = requestMessages, provider = selectedProvider.value
                )

                conversation.add(
                    ChatMessage(
                        text = result.reply, role = MessageRole.ASSISTANT
                    )
                )

                _messages.update {
                    it + ChatUiMessage(
                        text = result.reply, isUser = false
                    )
                }

                lastFailedMessages = null

            } catch (e: Exception) {

                _error.value = e.toChatError()

            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun Exception.toChatError(): ChatError {

        if (this is AiApiException) {
            val retryable = statusCode == 429 || statusCode in 500..599
            return ChatError(
                message = message ?: "Unable to generate response.",
                isRetryable = retryable
            )
        }

        return ChatError(
            message = message ?: "Unable to generate response.",
            isRetryable = true
        )
    }

    fun retryMessage() {

        val failedMessages = lastFailedMessages ?: return

        if (_isLoading.value) return

        requestAI(failedMessages)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearConversation() {
        if (_isLoading.value) return
        conversation.clear()
        lastFailedMessages = null
        _messages.value = emptyList()
        _error.value = null
    }
}