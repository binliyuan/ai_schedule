package com.solunis.schedule.data.ai

import android.content.Context

class AiAgent(
    private val context: Context,
    private val aiService: AiService,
    private val toolExecutor: AiToolExecutor,
    private val config: AiProviderConfig
) {

    companion object {
        const val TAG = "Agent"
        const val MAX_ROUNDS = 10
    }

    data class AgentResult(
        val success: Boolean,
        val message: String,
        val toolCallCount: Int = 0
    )

    suspend fun run(userMessage: ChatMessage): AgentResult {
        AiLogger.section(TAG, "AiAgent START")
        AiLogger.i(TAG, "Provider: ${config.name}, Model: ${config.model}")

        val systemPrompt = SkillLoader.systemPrompt(context)
        AiLogger.i(TAG, "System prompt loaded (${systemPrompt.length} chars)")

        val userPreview = when (val c = userMessage.content) {
            is String -> AiLogger.truncate(c, 200)
            else -> "[multimodal content]"
        }
        AiLogger.i(TAG, "User message: $userPreview")

        val messages = mutableListOf(
            ChatMessage.system(systemPrompt),
            userMessage
        )
        val tools = AiToolDefs.allTools()
        var totalToolCalls = 0

        for (round in 1..MAX_ROUNDS) {
            AiLogger.divider(TAG, "Round $round/$MAX_ROUNDS (messages: ${messages.size})")

            val request = ChatRequest(
                model = config.model,
                messages = messages,
                tools = tools,
                toolChoice = "auto"
            )

            val response = aiService.chatCompletion(request)

            if (response.error != null) {
                AiLogger.e(TAG, "AI ERROR: ${response.error.message}")
                AiLogger.section(TAG, "AiAgent END (failed)")
                return AgentResult(false, "AI 调用失败: ${response.error.message}")
            }

            val choice = response.choices?.firstOrNull()
            if (choice == null) {
                AiLogger.e(TAG, "AI returned no choices")
                AiLogger.section(TAG, "AiAgent END (no choices)")
                return AgentResult(false, "AI 未返回结果")
            }

            val msg = choice.message
            val toolCalls = msg.toolCalls

            if (toolCalls.isNullOrEmpty()) {
                val finalContent = msg.content ?: "完成"
                AiLogger.i(TAG, "AI final answer: ${AiLogger.truncate(finalContent, 300)}")
                AiLogger.section(TAG, "AiAgent END (success, $totalToolCalls tool calls)")
                return AgentResult(true, finalContent, totalToolCalls)
            }

            AiLogger.i(TAG, "AI wants to call ${toolCalls.size} tool(s)")

            messages.add(ChatMessage(
                role = "assistant",
                content = msg.content,
                toolCalls = toolCalls
            ))

            for (toolCall in toolCalls) {
                AiLogger.i(TAG, ">>> TOOL CALL: ${toolCall.function.name}")
                AiLogger.i(TAG, "    Args: ${toolCall.function.arguments}")

                val result = toolExecutor.execute(toolCall.function.name, toolCall.function.arguments)

                AiLogger.i(TAG, "<<< TOOL RESULT: ${AiLogger.truncate(result, 500)}")

                messages.add(ChatMessage.toolResult(toolCall.id, result))
                totalToolCalls++
            }
        }

        AiLogger.w(TAG, "Max rounds exceeded")
        AiLogger.section(TAG, "AiAgent END (max rounds)")
        return AgentResult(false, "超过最大轮次限制", totalToolCalls)
    }

    suspend fun generateSchedule(): AgentResult {
        val prompt = SkillLoader.generateSchedule(context)
        AiLogger.i(TAG, "Skill: generate_schedule")
        return run(ChatMessage.user(prompt))
    }

    suspend fun recognizeScheduleFromImage(base64Image: String): AgentResult {
        val prompt = SkillLoader.recognizeImage(context)
        AiLogger.i(TAG, "Skill: recognize_image (image: ${base64Image.length} base64 chars)")
        return run(ChatMessage.userWithImage(prompt, base64Image))
    }
}
