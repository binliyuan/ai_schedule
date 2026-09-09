package com.solunis.schedule.data.ai

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AiService(private val config: AiProviderConfig) {

    companion object {
        const val TAG = "AiService"
        private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun chatCompletion(request: ChatRequest): ChatResponse = withContext(Dispatchers.IO) {
        val url = "${config.baseUrl.trimEnd('/')}/${chatEndpoint()}"
        val body = gson.toJson(request)

        Log.d(TAG, "POST $url model=${request.model}")

        val httpRequest = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON_TYPE))
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(httpRequest).execute()
        val responseBody = response.body?.string() ?: ""

        Log.d(TAG, "response code=${response.code} length=${responseBody.length}")

        if (!response.isSuccessful) {
            Log.e(TAG, "API error: $responseBody")
            return@withContext ChatResponse(
                id = null,
                choices = null,
                error = ErrorInfo(message = "HTTP ${response.code}: $responseBody", type = "api_error", code = response.code.toString())
            )
        }

        try {
            gson.fromJson(responseBody, ChatResponse::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Parse error", e)
            ChatResponse(id = null, choices = null, error = ErrorInfo(message = "解析响应失败: ${e.message}", type = "parse_error", code = null))
        }
    }

    private fun chatEndpoint(): String = when {
        config.name == "Google Gemini" -> "models/${config.model}:generateContent"
        else -> "chat/completions"
    }
}
