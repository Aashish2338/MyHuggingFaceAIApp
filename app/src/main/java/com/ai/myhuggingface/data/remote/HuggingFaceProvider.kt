package com.ai.myhuggingface.data.remote

import com.ai.myhuggingface.core.config.AiConfig
import com.ai.myhuggingface.core.network.NetworkClient
import com.ai.myhuggingface.domain.model.ChatMessage
import com.ai.myhuggingface.domain.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class HuggingFaceProvider(private val apiToken: String) : AiChatProvider {

    override suspend fun generateResponse(
        messages: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {

        require(apiToken.isNotBlank()) {
            "Hugging Face token is missing."
        }

        val json = JSONObject().apply {
            put("model", AiConfig.HF_MODEL)
            put("messages", messages.toJsonArray())
            put("max_tokens", 1000)
            put("stream", false)
        }

        val body = json.toString().toRequestBody(
            "application/json".toMediaType()
        )

        val request = Request.Builder()
            .url(AiConfig.HF_URL)
            .header("Authorization", "Bearer $apiToken")
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
                    .takeIf { it.isNotBlank() }
                    ?: throw Exception("AI returned an empty response.")
            }
    }
}

internal fun List<ChatMessage>.toJsonArray(): JSONArray {
    return JSONArray().apply {
        this@toJsonArray.forEach { message ->
            put(
                JSONObject().apply {
                    put(
                        "role",
                        when (message.role) {
                            MessageRole.USER -> "user"
                            MessageRole.ASSISTANT -> "assistant"
                            MessageRole.SYSTEM -> "system"
                        }
                    )
                    put("content", message.text)
                }
            )
        }
    }
}