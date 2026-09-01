package com.suda.yzune.wakeupschedule.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _coins = MutableLiveData(2480)
    val coins: LiveData<Int> = _coins

    private val _weekStats = MutableLiveData(
        WeekStats(levelsCleared = 12, wordsPracticed = 340, accuracy = 87, streak = 3)
    )
    val weekStats: LiveData<WeekStats> = _weekStats

    val featuredGame = GameItem(
        emoji = "🧩",
        name = "单词消消乐",
        description = "边玩边背单词，每关通过可获得经验值",
        tag = null,
        level = 7,
        xp = 680,
        xpMax = 1000
    )

    val allGames = listOf(
        GameItem("🧩", "单词消消乐", "关卡 · 益智", tag = "NEW"),
        GameItem("⚡", "速算挑战",   "计时 · 数学", tag = null),
        GameItem("🗺️", "知识地图",  "探索 · 问答", tag = null),
        GameItem("🎯", "记忆翻牌",  "记忆 · 训练", tag = null)
    )

    data class WeekStats(
        val levelsCleared: Int,
        val wordsPracticed: Int,
        val accuracy: Int,
        val streak: Int
    )

    data class GameItem(
        val emoji: String,
        val name: String,
        val description: String,
        val tag: String?,
        val level: Int = 0,
        val xp: Int = 0,
        val xpMax: Int = 1000
    )
}
