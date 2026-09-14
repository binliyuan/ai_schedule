package com.solunis.schedule.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _games = MutableLiveData<List<GameItem>>(emptyList())
    val games: LiveData<List<GameItem>> = _games

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadGames()
    }

    private fun loadGames() {
        _isLoading.value = true
        // TODO: replace with real API call
        _games.value = mockGames()
        _isLoading.value = false
    }

    fun refresh() {
        loadGames()
    }

    private fun mockGames(): List<GameItem> = listOf(
        GameItem(
            id = "word_crush",
            name = "单词消消乐",
            description = "边玩边背单词",
            imageUrl = "https://img.icons8.com/color/512/puzzle.png",
            actionType = ActionType.H5,
            actionUrl = "https://example.com/word-crush",
            tag = "HOT"
        ),
        GameItem(
            id = "math_speed",
            name = "速算挑战",
            description = "限时数学闯关",
            imageUrl = "https://img.icons8.com/color/512/lightning-bolt.png",
            actionType = ActionType.H5,
            actionUrl = "https://example.com/math-speed"
        ),
        GameItem(
            id = "knowledge_map",
            name = "知识地图",
            description = "探索问答世界",
            imageUrl = "https://img.icons8.com/color/512/globe.png",
            actionType = ActionType.QUICK_APP,
            actionUrl = "https://example.com/knowledge-map"
        ),
        GameItem(
            id = "memory_flip",
            name = "记忆翻牌",
            description = "训练记忆力",
            imageUrl = "https://img.icons8.com/color/512/target.png",
            actionType = ActionType.DOWNLOAD,
            actionUrl = "https://play.google.com/store/apps/details?id=com.example.memory",
            tag = "NEW"
        ),
        GameItem(
            id = "sudoku",
            name = "数独大师",
            description = "经典数独挑战",
            imageUrl = "https://img.icons8.com/color/512/grid-2.png",
            actionType = ActionType.DEEPLINK,
            actionUrl = "sudoku://game/start"
        ),
        GameItem(
            id = "typing_race",
            name = "打字竞速",
            description = "指尖上的速度",
            imageUrl = "https://img.icons8.com/color/512/keyboard.png",
            actionType = ActionType.H5,
            actionUrl = "https://example.com/typing-race"
        ),
        GameItem(
            id = "quiz_battle",
            name = "知识对战",
            description = "实时答题 PK",
            imageUrl = "https://img.icons8.com/color/512/battle.png",
            actionType = ActionType.QUICK_APP,
            actionUrl = "https://example.com/quiz-battle",
            tag = "HOT"
        ),
        GameItem(
            id = "draw_guess",
            name = "你画我猜",
            description = "创意涂鸦猜词",
            imageUrl = "https://img.icons8.com/color/512/paint-palette.png",
            actionType = ActionType.DOWNLOAD,
            actionUrl = "https://play.google.com/store/apps/details?id=com.example.drawguess"
        )
    )

    enum class ActionType {
        DEEPLINK,
        DOWNLOAD,
        QUICK_APP,
        H5
    }

    data class GameItem(
        val id: String,
        val name: String,
        val description: String,
        val imageUrl: String,
        val actionType: ActionType,
        val actionUrl: String,
        val tag: String? = null
    )
}
