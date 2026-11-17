package com.ai.myhuggingface.data.remote

import com.ai.myhuggingface.core.config.AiConfig
import com.ai.myhuggingface.core.network.NetworkClient
import com.ai.myhuggingface.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class OpenAIProvider(private val apiKey: String) : AiChatProvider {

    override suspend fun generateResponse(
        messages: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {

        require(apiKey.isNotBlank()) {
            "OpenAI API key is missing."
        }

        val json = JSONObject().apply {
            put("model", AiConfig.OPENAI_MODEL)
            put("messages", messages.toJsonArray())
            put("max_tokens", 1000)
        }

        val body = json.toString().toRequestBody(
            "application/json".toMediaType()
        )

        val request = Request.Builder()
            .url(AiConfig.OPENAI_URL)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        NetworkClient.client.newCall(request)
            .execute().use { response ->

                val responseText =
                    response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    throw AiApiException(
                        response.code,
                        responseText
                    )
                }

                JSONObject(responseText)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }
    }
}