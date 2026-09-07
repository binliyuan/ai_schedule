package com.solunis.schedule.mcp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.solunis.schedule.data.database.AppDatabase
import fi.iki.elonen.NanoHTTPD

class McpServer(
    port: Int,
    private val database: AppDatabase
) : NanoHTTPD(port) {

    private val gson = Gson()
    private val toolExecutor = McpToolExecutor(database)

    companion object {
        const val TAG = "McpServer"
        const val DEFAULT_PORT = 8090
    }

    override fun serve(session: IHTTPSession): Response {
        // CORS headers
        val corsHeaders = mutableMapOf(
            "Access-Control-Allow-Origin" to "*",
            "Access-Control-Allow-Headers" to "Content-Type",
            "Access-Control-Allow-Methods" to "GET, POST, OPTIONS"
        )

        if (session.method == Method.OPTIONS) {
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "").apply {
                corsHeaders.forEach { (k, v) -> addHeader(k, v) }
            }
        }

        val uri = session.uri
        Log.d(TAG, "${session.method} $uri")

        val response = when {
            uri == "/mcp" && session.method == Method.POST -> handleJsonRpc(session)
            uri == "/health" -> newFixedLengthResponse(Response.Status.OK, "application/json", """{"status":"ok","server":"WakeupSchedule MCP"}""")
            uri == "/" -> handleInfo()
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", """{"error":"not found"}""")
        }

        corsHeaders.forEach { (k, v) -> response.addHeader(k, v) }
        return response
    }

    private fun handleInfo(): Response {
        val info = mapOf(
            "name" to "WakeupSchedule MCP Server",
            "version" to "1.0.0",
            "protocol" to "MCP (Model Context Protocol)",
            "endpoint" to "/mcp",
            "transport" to "HTTP JSON-RPC"
        )
        return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(info))
    }

    private fun handleJsonRpc(session: IHTTPSession): Response {
        val bodyMap = HashMap<String, String>()
        session.parseBody(bodyMap)
        val body = bodyMap["postData"] ?: ""

        if (body.isBlank()) {
            return jsonResponse(JsonRpcResponse.error(null, -32700, "Parse error: empty body"))
        }

        return try {
            val request = gson.fromJson(body, JsonRpcRequest::class.java)
            val result = handleMethod(request)
            jsonResponse(result)
        } catch (e: Exception) {
            Log.e(TAG, "JSON-RPC error", e)
            jsonResponse(JsonRpcResponse.error(null, -32700, "Parse error: ${e.message}"))
        }
    }

    private fun handleMethod(request: JsonRpcRequest): JsonRpcResponse {
        Log.d(TAG, "method=${request.method}")

        return when (request.method) {
            "initialize" -> {
                JsonRpcResponse.success(request.id, McpInitializeResult())
            }

            "notifications/initialized" -> {
                JsonRpcResponse.success(request.id, mapOf("status" to "ok"))
            }

            "tools/list" -> {
                val tools = McpTools.allTools()
                JsonRpcResponse.success(request.id, McpToolsListResult(tools))
            }

            "tools/call" -> {
                val params = request.params
                if (params == null) {
                    return JsonRpcResponse.error(request.id, -32602, "Missing params")
                }

                val toolName = params.get("name")?.asString
                    ?: return JsonRpcResponse.error(request.id, -32602, "Missing tool name")

                val arguments = if (params.has("arguments")) {
                    params.getAsJsonObject("arguments")
                } else {
                    JsonObject()
                }

                Log.d(TAG, "tool=$toolName args=$arguments")
                val result = toolExecutor.execute(toolName, arguments)
                JsonRpcResponse.success(request.id, result)
            }

            "ping" -> {
                JsonRpcResponse.success(request.id, mapOf("status" to "pong"))
            }

            else -> {
                JsonRpcResponse.error(request.id, -32601, "Method not found: ${request.method}")
            }
        }
    }

    private fun jsonResponse(response: JsonRpcResponse): Response {
        val json = gson.toJson(response)
        return newFixedLengthResponse(Response.Status.OK, "application/json", json)
    }
}
