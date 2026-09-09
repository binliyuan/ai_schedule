package com.solunis.schedule.data.ai

import android.util.Log

class AiAgent(
    private val aiService: AiService,
    private val toolExecutor: AiToolExecutor,
    private val config: AiProviderConfig
) {

    companion object {
        const val TAG = "AiAgent"
        const val MAX_ROUNDS = 10

        private const val SYSTEM_PROMPT = """你是 WakeupSchedule 课表助手。你可以帮助用户：
1. 查看、添加、修改、删除课程
2. 从课表图片中识别课程信息并批量导入

规则：
- 星期几用数字表示：1=周一, 2=周二, ..., 7=周日
- startNode 表示第几节课开始（从1开始）
- step 表示连续几节课（通常是2）
- 从图片识别课表时，调用 batch_import_courses 一次性导入所有课程
- 返回结果时用简洁的中文"""
    }

    data class AgentResult(
        val success: Boolean,
        val message: String,
        val toolCallCount: Int = 0
    )

    suspend fun run(userMessage: ChatMessage): AgentResult {
        val messages = mutableListOf(
            ChatMessage.system(SYSTEM_PROMPT),
            userMessage
        )
        val tools = AiToolDefs.allTools()
        var totalToolCalls = 0

        for (round in 1..MAX_ROUNDS) {
            Log.d(TAG, "round $round, messages=${messages.size}")

            val request = ChatRequest(
                model = config.model,
                messages = messages,
                tools = tools,
                toolChoice = "auto"
            )

            val response = aiService.chatCompletion(request)

            if (response.error != null) {
                return AgentResult(false, "AI 调用失败: ${response.error.message}")
            }

            val choice = response.choices?.firstOrNull()
                ?: return AgentResult(false, "AI 未返回结果")

            val msg = choice.message
            val toolCalls = msg.toolCalls

            if (toolCalls.isNullOrEmpty()) {
                return AgentResult(true, msg.content ?: "完成", totalToolCalls)
            }

            messages.add(ChatMessage(
                role = "assistant",
                content = msg.content,
                toolCalls = toolCalls
            ))

            for (toolCall in toolCalls) {
                Log.d(TAG, "tool_call: ${toolCall.function.name}(${toolCall.function.arguments})")
                val result = toolExecutor.execute(toolCall.function.name, toolCall.function.arguments)
                messages.add(ChatMessage.toolResult(toolCall.id, result))
                totalToolCalls++
            }
        }

        return AgentResult(false, "超过最大轮次限制", totalToolCalls)
    }

    suspend fun recognizeScheduleFromImage(base64Image: String): AgentResult {
        val message = ChatMessage.userWithImage(
            "请识别这张课表图片中的所有课程信息，包括课程名称、星期几、第几节课、教室等。识别完成后请调用 batch_import_courses 工具将所有课程导入课表。",
            base64Image
        )
        return run(message)
    }
}
