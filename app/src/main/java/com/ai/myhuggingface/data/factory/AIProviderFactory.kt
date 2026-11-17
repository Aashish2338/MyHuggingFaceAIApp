package com.ai.myhuggingface.data.factory

import com.ai.myhuggingface.core.config.AiProviderType
import com.ai.myhuggingface.data.remote.AiChatProvider
import com.ai.myhuggingface.data.remote.GeminiProvider
import com.ai.myhuggingface.data.remote.HuggingFaceProvider
import com.ai.myhuggingface.data.remote.OpenAIProvider

class AIProviderFactory(
    private val hfToken: String,
    private val openAiKey: String,
    private val geminiKey: String
) {
    fun createProvider(
        type: AiProviderType
    ): AiChatProvider {

        return when (type) {

            AiProviderType.HUGGING_FACE ->
                HuggingFaceProvider(hfToken)

            AiProviderType.OPENAI ->
                OpenAIProvider(openAiKey)

            AiProviderType.GEMINI ->
                GeminiProvider(geminiKey)
        }
    }
}