package com.ai.myhuggingface.data.remote

import org.json.JSONObject

class AiApiException(
    val statusCode: Int,
    val responseBody: String
) : Exception(describe(statusCode, responseBody)) {

    companion object {

        private fun describe(
            statusCode: Int,
            responseBody: String
        ): String {

            val reason = when (statusCode) {
                400 -> "The AI provider rejected the request."
                401 -> "Authentication failed. The API key is missing or invalid."
                402 -> "Payment required: this provider has no remaining " +
                        "credits or needs a paid plan."
                403 -> "Access denied. Check that the API key has permission " +
                        "to use this model."
                404 -> "The configured model was not found."
                429 -> "Rate limit exceeded. Wait a moment and try again."
                in 500..599 ->
                    "The AI provider is temporarily unavailable. " +
                            "Try again shortly."
                else -> "The AI provider returned an unexpected error."
            }

            val detail = detailFrom(responseBody)

            return if (detail == null) reason else "$reason\n\n$detail"
        }

        private fun detailFrom(responseBody: String): String? {

            if (responseBody.isBlank()) return null

            val message = runCatching {
                val json = JSONObject(responseBody)
                when (val error = json.opt("error")) {
                    is JSONObject ->
                        error.optString("message").ifBlank { null }
                    is String ->
                        error.ifBlank { null }
                    else ->
                        json.optString("message").ifBlank { null }
                }
            }.getOrNull()

            return (message ?: responseBody)
                .replace(Regex("\\s+"), " ")
                .trim()
                .take(300)
                .ifBlank { null }
        }
    }
}
