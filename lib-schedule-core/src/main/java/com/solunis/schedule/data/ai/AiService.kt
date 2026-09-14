package com.solunis.schedule.data.ai

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
        const val TAG = "Service"
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

        AiLogger.section(TAG, "AI REQUEST")
        AiLogger.i(TAG, "URL: $url")
        AiLogger.i(TAG, "Provider: ${config.name}, Model: ${request.model}")
        AiLogger.i(TAG, "Messages count: ${request.messages.size}")
        request.messages.forEach { msg ->
            val preview = when (val c = msg.content) {
                is String -> AiLogger.truncate(c, 200)
                is List<*> -> "[multimodal: ${(c as List<*>).size} parts]"
                else -> c.toString()
            }
            AiLogger.i(TAG, "  [${msg.role}] $preview")
        }
        AiLogger.i(TAG, "Tools: ${request.tools?.map { it.function.name } ?: "none"}")
        AiLogger.d(TAG, "Body (${body.length} bytes): ${AiLogger.truncate(body, 2000)}")

        val httpRequest = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON_TYPE))
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .build()

        val startTime = System.currentTimeMillis()
        val response = client.newCall(httpRequest).execute()
        val elapsed = System.currentTimeMillis() - startTime
        val responseBody = response.body?.string() ?: ""

        AiLogger.section(TAG, "AI RESPONSE")
        AiLogger.i(TAG, "HTTP ${response.code} in ${elapsed}ms (${responseBody.length} bytes)")

        if (!response.isSuccessful) {
            AiLogger.e(TAG, "API ERROR: $responseBody")
            return@withContext ChatResponse(
                id = null, choices = null,
                error = ErrorInfo(message = "HTTP ${response.code}: $responseBody", type = "api_error", code = response.code.toString())
            )
        }

        try {
            val parsed = gson.fromJson(responseBody, ChatResponse::class.java)
            val choice = parsed.choices?.firstOrNull()
            if (choice != null) {
                AiLogger.i(TAG, "Finish reason: ${choice.finishReason}")
                if (choice.message.content != null) {
                    AiLogger.i(TAG, "Content: ${AiLogger.truncate(choice.message.content!!, 500)}")
                }
                if (!choice.message.toolCalls.isNullOrEmpty()) {
                    AiLogger.i(TAG, "Tool calls: ${choice.message.toolCalls!!.size}")
                    choice.message.toolCalls!!.forEach { tc ->
                        AiLogger.i(TAG, "  -> ${tc.function.name}(${tc.function.arguments})")
                    }
                }
            } else {
                AiLogger.w(TAG, "No choices in response")
                AiLogger.d(TAG, "Raw: ${AiLogger.truncate(responseBody, 1000)}")
            }
            parsed
        } catch (e: Exception) {
            AiLogger.e(TAG, "Parse error: ${e.message}", e)
            AiLogger.e(TAG, "Raw: ${AiLogger.truncate(responseBody, 1000)}")
            ChatResponse(id = null, choices = null, error = ErrorInfo(message = "解析响应失败: ${e.message}", type = "parse_error", code = null))
        }
    }

    private fun chatEndpoint(): String = when {
        config.name == "Google Gemini" -> "models/${config.model}:generateContent"
        else -> "chat/completions"
    }
}
