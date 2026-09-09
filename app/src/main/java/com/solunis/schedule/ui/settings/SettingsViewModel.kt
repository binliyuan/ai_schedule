package com.solunis.schedule.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.local.TokenManager
import com.solunis.schedule.data.model.User
import com.solunis.schedule.data.network.RetrofitClient
import com.solunis.schedule.data.repository.TableRepository
import com.solunis.schedule.data.repository.UserRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val tableRepository = TableRepository(database.tableDao())
    private val userRepository = UserRepository(RetrofitClient.apiService)
    private val prefs = application.getSharedPreferences("ai_model_config", Context.MODE_PRIVATE)

    private val _maxWeek = MutableLiveData(20)
    val maxWeek: LiveData<Int> = _maxWeek

    private val _nodesPerDay = MutableLiveData(12)
    val nodesPerDay: LiveData<Int> = _nodesPerDay

    private val _showWeekend = MutableLiveData(true)
    val showWeekend: LiveData<Boolean> = _showWeekend

    private val _modelProvider = MutableLiveData("")
    val modelProvider: LiveData<String> = _modelProvider

    private val _modelName = MutableLiveData("")
    val modelName: LiveData<String> = _modelName

    private val _apiKey = MutableLiveData("")
    val apiKey: LiveData<String> = _apiKey

    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> = _currentUser

    val providerOptions = listOf("OpenAI", "Anthropic", "Google Gemini", "DeepSeek", "豆包", "通义千问", "文心一言", "Ollama (本地)")
    val modelMap = mapOf(
        "OpenAI" to listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo"),
        "Anthropic" to listOf("claude-sonnet-4-20250514", "claude-3-5-haiku-20241022", "claude-opus-4-20250514"),
        "Google Gemini" to listOf("gemini-2.0-flash", "gemini-1.5-pro", "gemini-1.5-flash"),
        "DeepSeek" to listOf("deepseek-v4-flash-vision-exp", "deepseek-v4-pro", "deepseek-chat", "deepseek-coder", "deepseek-reasoner"),
        "豆包" to listOf("doubao-pro-256k", "doubao-pro-32k", "doubao-lite-32k"),
        "通义千问" to listOf("qwen-max", "qwen-plus", "qwen-turbo"),
        "文心一言" to listOf("ernie-4.0", "ernie-3.5-turbo", "ernie-speed"),
        "Ollama (本地)" to listOf("llama3", "mistral", "qwen2", "gemma2")
    )

    init {
        loadSettings()
        loadModelConfig()
        refreshLoginState()
    }

    fun refreshLoginState() {
        _isLoggedIn.value = userRepository.isLoggedIn()
        _currentUser.value = userRepository.getCurrentUser()
    }

    fun logout() {
        userRepository.logout()
        _isLoggedIn.value = false
        _currentUser.value = null
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val table = tableRepository.getDefaultTableSync()
            table?.let {
                _maxWeek.postValue(it.maxWeek)
                _nodesPerDay.postValue(it.nodes)
                _showWeekend.postValue(it.showSat && it.showSun)
            }
        }
    }

    private fun loadModelConfig() {
        _modelProvider.value = prefs.getString("provider", "") ?: ""
        _modelName.value = prefs.getString("model", "") ?: ""
        _apiKey.value = prefs.getString("api_key", "") ?: ""
    }

    fun updateMaxWeek(value: Int) {
        _maxWeek.value = value
        saveSettings()
    }

    fun updateNodesPerDay(value: Int) {
        _nodesPerDay.value = value
        saveSettings()
    }

    fun updateShowWeekend(value: Boolean) {
        _showWeekend.value = value
        saveSettings()
    }

    fun updateModelProvider(provider: String) {
        _modelProvider.value = provider
        val models = modelMap[provider]
        if (models != null && models.isNotEmpty()) {
            _modelName.value = models.first()
        } else {
            _modelName.value = ""
        }
    }

    fun updateModelName(model: String) {
        _modelName.value = model
    }

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun saveModelConfig() {
        prefs.edit()
            .putString("provider", _modelProvider.value ?: "")
            .putString("model", _modelName.value ?: "")
            .putString("api_key", _apiKey.value ?: "")
            .apply()
    }

    fun getModelsForProvider(provider: String): List<String> {
        return modelMap[provider] ?: emptyList()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val table = tableRepository.getDefaultTableSync() ?: return@launch
            tableRepository.updateTable(
                table.copy(
                    maxWeek = _maxWeek.value ?: 20,
                    nodes = _nodesPerDay.value ?: 12,
                    showSat = _showWeekend.value ?: true,
                    showSun = _showWeekend.value ?: true
                )
            )
        }
    }
}
