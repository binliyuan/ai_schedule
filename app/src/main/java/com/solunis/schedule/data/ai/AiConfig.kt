package com.solunis.schedule.data.ai

import android.content.Context

data class AiProviderConfig(
    val name: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String,
    val supportsVision: Boolean = true
)

object AiConfig {

    private const val PREFS_NAME = "ai_model_config"
    const val DEFAULT_PROVIDER = "DeepSeek"
    const val DEFAULT_MODEL = "deepseek-v4-flash-vision-exp"
    const val DEFAULT_API_KEY = "sk-33e6f56a534e435ab38b0a8cc049c924"

    fun getConfig(context: Context): AiProviderConfig? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val provider = prefs.getString("provider", "").let { if (it.isNullOrBlank()) DEFAULT_PROVIDER else it }
        val model = prefs.getString("model", "").let { if (it.isNullOrBlank()) DEFAULT_MODEL else it }
        val apiKey = prefs.getString("api_key", "").let { if (it.isNullOrBlank()) DEFAULT_API_KEY else it }

        val baseUrl = providerBaseUrl(provider)
        return AiProviderConfig(
            name = provider,
            baseUrl = baseUrl,
            model = model,
            apiKey = apiKey,
            supportsVision = providerSupportsVision(provider)
        )
    }

    fun initDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString("provider", "").isNullOrBlank()) {
            prefs.edit()
                .putString("provider", DEFAULT_PROVIDER)
                .putString("model", DEFAULT_MODEL)
                .putString("api_key", DEFAULT_API_KEY)
                .apply()
        }
    }

    private fun providerBaseUrl(provider: String): String = when (provider) {
        "OpenAI" -> "https://api.openai.com/v1/"
        "Anthropic" -> "https://api.anthropic.com/v1/"
        "Google Gemini" -> "https://generativelanguage.googleapis.com/v1beta/"
        "DeepSeek" -> "https://api.deepseek.com/"
        "通义千问" -> "https://dashscope.aliyuncs.com/compatible-mode/v1/"
        "文心一言" -> "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/"
        "豆包" -> "https://ark.cn-beijing.volces.com/api/v3/"
        "Ollama (本地)" -> "http://localhost:11434/v1/"
        else -> "https://api.openai.com/v1/"
    }

    private fun providerSupportsVision(provider: String): Boolean = when (provider) {
        "DeepSeek", "OpenAI", "Google Gemini", "通义千问", "豆包" -> true
        else -> false
    }
}
