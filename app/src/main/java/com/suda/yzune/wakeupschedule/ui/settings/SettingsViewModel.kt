package com.suda.yzune.wakeupschedule.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.suda.yzune.wakeupschedule.data.database.AppDatabase
import com.suda.yzune.wakeupschedule.data.repository.TableRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val tableRepository = TableRepository(database.tableDao())

    private val _maxWeek = MutableLiveData(20)
    val maxWeek: LiveData<Int> = _maxWeek

    private val _nodesPerDay = MutableLiveData(12)
    val nodesPerDay: LiveData<Int> = _nodesPerDay

    private val _showWeekend = MutableLiveData(true)
    val showWeekend: LiveData<Boolean> = _showWeekend

    init {
        loadSettings()
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
