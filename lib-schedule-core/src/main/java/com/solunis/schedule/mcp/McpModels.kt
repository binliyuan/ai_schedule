package com.solunis.schedule.mcp

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonObject? = null,
    val id: Any? = null
)

data class JsonRpcResponse(
    val jsonrpc: String = "2.0",
    val result: Any? = null,
    val error: JsonRpcError? = null,
    val id: Any? = null
) {
    companion object {
        fun success(id: Any?, result: Any?) = JsonRpcResponse(result = result, id = id)
        fun error(id: Any?, code: Int, message: String) =
            JsonRpcResponse(error = JsonRpcError(code, message), id = id)
    }
}

data class JsonRpcError(
    val code: Int,
    val message: String
)

data class McpToolDef(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

data class McpInitializeResult(
    val protocolVersion: String = "2024-11-05",
    val capabilities: McpCapabilities = McpCapabilities(),
    val serverInfo: McpServerInfo = McpServerInfo()
)

data class McpCapabilities(
    val tools: JsonObject = JsonObject()
)

data class McpServerInfo(
    val name: String = "WakeupSchedule MCP Server",
    val version: String = "1.0.0"
)

data class McpToolsListResult(
    val tools: List<McpToolDef>
)

data class McpToolCallResult(
    val content: List<McpContent>,
    @SerializedName("isError")
    val isError: Boolean = false
)

data class McpContent(
    val type: String = "text",
    val text: String
)
