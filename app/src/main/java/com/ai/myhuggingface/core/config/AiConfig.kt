package com.ai.myhuggingface.core.config

object AiConfig {
    const val HF_MODEL = "openai/gpt-oss-120b:fastest"

    const val OPENAI_MODEL = "gpt-4.1-mini"

    const val GEMINI_MODEL = "gemini-3.8-flash"

    const val HF_URL = "https://router.huggingface.co/v1/chat/completions"

    const val OPENAI_URL = "https://api.openai.com/v1/chat/completions"

    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
}