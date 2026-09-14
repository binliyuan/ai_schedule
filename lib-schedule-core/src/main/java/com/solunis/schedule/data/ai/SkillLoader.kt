package com.solunis.schedule.data.ai

import android.content.Context

object SkillLoader {

    private const val SKILLS_DIR = "skills"

    fun load(context: Context, skillName: String): String {
        return context.assets.open("$SKILLS_DIR/$skillName.md")
            .bufferedReader()
            .use { it.readText() }
            .trim()
    }

    fun systemPrompt(context: Context): String = load(context, "system_prompt")
    fun generateSchedule(context: Context): String = load(context, "generate_schedule")
    fun recognizeImage(context: Context): String = load(context, "recognize_image")
}
