package com.ai.myhuggingface.core.config

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aiDataStore by preferencesDataStore(
    name = "ai_settings"
)

class AiProviderPreferences(private val context: Context) {
    companion object {
        private val PROVIDER_KEY =
            stringPreferencesKey("selected_ai_provider")
    }

    val selectedProvider: Flow<AiProviderType> =
        context.aiDataStore.data.map { preferences ->

            preferences[PROVIDER_KEY]
                ?.let {
                    runCatching {
                        AiProviderType.valueOf(it)
                    }.getOrNull()
                }
                ?: AiProviderType.HUGGING_FACE
        }

    suspend fun saveProvider(
        provider: AiProviderType
    ) {
        context.aiDataStore.edit { preferences ->
            preferences[PROVIDER_KEY] = provider.name
        }
    }
}