package com.ai.myhuggingface.presentation.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ai.myhuggingface.BuildConfig
import com.ai.myhuggingface.core.config.AiProviderPreferences
import com.ai.myhuggingface.data.factory.AIProviderFactory
import com.ai.myhuggingface.data.repository.ChatRepositoryImpl
import com.ai.myhuggingface.domain.usecase.SendMessageUseCase

class AiChatViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val providerFactory = AIProviderFactory(
            hfToken = BuildConfig.HF_API_TOKEN,
            openAiKey = BuildConfig.OPENAI_API_KEY,
            geminiKey = BuildConfig.GEMINI_API_KEY
        )

        val repository = ChatRepositoryImpl(providerFactory)
        val useCase = SendMessageUseCase(repository)
        val preferences = AiProviderPreferences(context.applicationContext)

        return AiChatViewModel(
            sendMessageUseCase = useCase,
            preferences = preferences
        ) as T
    }
}