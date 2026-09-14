package com.solunis.schedule.data.ai

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

// ---- Request ----

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val tools: List<ToolDef>? = null,
    @SerializedName("tool_choice")
    val toolChoice: String? = null,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096
)

data class ChatMessage(
    val role: String,
    val content: Any? = null,
    @SerializedName("tool_calls")
    val toolCalls: List<ToolCallInfo>? = null,
    @SerializedName("tool_call_id")
    val toolCallId: String? = null,
    val name: String? = null
) {
    companion object {
        fun system(text: String) = ChatMessage(role = "system", content = text)
        fun user(text: String) = ChatMessage(role = "user", content = text)
        fun userWithImage(text: String, base64Image: String): ChatMessage {
            val content = listOf(
                mapOf("type" to "text", "text" to text),
                mapOf("type" to "image_url", "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64Image"))
            )
            return ChatMessage(role = "user", content = content)
        }
        fun assistant(text: String) = ChatMessage(role = "assistant", content = text)
        fun toolResult(toolCallId: String, result: String) =
            ChatMessage(role = "tool", content = result, toolCallId = toolCallId)
    }
}

data class ToolDef(
    val type: String = "function",
    val function: ToolFunction
)

data class ToolFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject
)

// ---- Response ----

data class ChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val error: ErrorInfo? = null
)

data class Choice(
    val index: Int,
    val message: ResponseMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class ResponseMessage(
    val role: String?,
    val content: String?,
    @SerializedName("tool_calls")
    val toolCalls: List<ToolCallInfo>?
)

data class ToolCallInfo(
    val id: String,
    val type: String = "function",
    val function: ToolCallFunction
)

data class ToolCallFunction(
    val name: String,
    val arguments: String
)

data class ErrorInfo(
    val message: String?,
    val type: String?,
    val code: String?
)
