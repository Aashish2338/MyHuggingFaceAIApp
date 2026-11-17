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

class GeminiProvider(
    private val apiKey: String
) : AiChatProvider {

    override suspend fun generateResponse(
        messages: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {

        require(apiKey.isNotBlank()) {
            "Gemini API key is missing."
        }

        val contents = JSONArray()

        messages
            .filter { it.role != MessageRole.SYSTEM }
            .forEach { message ->

                contents.put(
                    JSONObject().apply {
                        put(
                            "role",
                            if (message.role == MessageRole.ASSISTANT)
                                "model"
                            else
                                "user"
                        )

                        put(
                            "parts",
                            JSONArray().put(
                                JSONObject().put(
                                    "text",
                                    message.text
                                )
                            )
                        )
                    }
                )
            }

        val systemInstruction = messages
            .filter { it.role == MessageRole.SYSTEM }
            .joinToString("\n") { it.text }

        val json = JSONObject().apply {
            put("contents", contents)

            if (systemInstruction.isNotBlank()) {
                put(
                    "systemInstruction",
                    JSONObject().put(
                        "parts",
                        JSONArray().put(
                            JSONObject().put(
                                "text",
                                systemInstruction
                            )
                        )
                    )
                )
            }

            put(
                "generationConfig",
                JSONObject().put("maxOutputTokens", 1000)
            )
        }

        val body = json.toString().toRequestBody(
            "application/json".toMediaType()
        )

        val request = Request.Builder()
            .url(
                "${AiConfig.GEMINI_BASE_URL}" +
                        "${AiConfig.GEMINI_MODEL}:generateContent"
            )
            .header("x-goog-api-key", apiKey)
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

                val candidates = JSONObject(responseText)
                    .getJSONArray("candidates")

                val parts = candidates
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")

                buildString {
                    for (i in 0 until parts.length()) {
                        append(
                            parts.getJSONObject(i)
                                .optString("text")
                        )
                    }
                }.trim().takeIf { it.isNotBlank() }
                    ?: throw Exception("Gemini returned an empty response.")
            }
    }
}