package com.solunis.schedule.data.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object SkillLoader {

    private const val TAG = "SkillLoader"
    private const val ASSETS_DIR = "skills"
    private const val CACHE_DIR = "skills"
    private const val BASE_URL = "http://124.223.93.2:8012/api/skills/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun load(context: Context, skillName: String): String {
        val cached = readCache(context, skillName)
        if (cached != null) return cached
        return readAsset(context, skillName)
    }

    fun systemPrompt(context: Context): String = load(context, "system_prompt")
    fun generateSchedule(context: Context): String = load(context, "generate_schedule")
    fun recognizeImage(context: Context): String = load(context, "recognize_image")

    suspend fun syncFromServer(context: Context) {
        val skills = listOf("system_prompt", "generate_schedule", "recognize_image")
        for (name in skills) {
            try {
                val content = fetchFromServer(name)
                if (content != null) {
                    writeCache(context, name, content)
                    Log.d(TAG, "Synced skill: $name (${content.length} chars)")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync skill: $name", e)
            }
        }
    }

    private suspend fun fetchFromServer(skillName: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL$skillName.md")
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.trim()?.takeIf { it.isNotEmpty() }
            } else {
                Log.w(TAG, "Server returned ${response.code} for $skillName")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network error fetching $skillName", e)
            null
        }
    }

    private fun readCache(context: Context, skillName: String): String? {
        val file = File(context.filesDir, "$CACHE_DIR/$skillName.md")
        return if (file.exists()) {
            try {
                file.readText().trim().takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    private fun writeCache(context: Context, skillName: String, content: String) {
        val dir = File(context.filesDir, CACHE_DIR)
        dir.mkdirs()
        File(dir, "$skillName.md").writeText(content)
    }

    private fun readAsset(context: Context, skillName: String): String {
        return context.assets.open("$ASSETS_DIR/$skillName.md")
            .bufferedReader()
            .use { it.readText() }
            .trim()
    }

    fun clearCache(context: Context) {
        val dir = File(context.filesDir, CACHE_DIR)
        if (dir.exists()) dir.deleteRecursively()
    }
}
