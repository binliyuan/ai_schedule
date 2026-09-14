package com.solunis.schedule.data.repository

import androidx.lifecycle.LiveData
import com.solunis.schedule.data.database.dao.TableDao
import com.solunis.schedule.data.database.entity.TableBean

class TableRepository(private val tableDao: TableDao) {

    fun getAllTables(): LiveData<List<TableBean>> = tableDao.getAllTables()

    fun getDefaultTable(): LiveData<TableBean?> = tableDao.getDefaultTable()

    suspend fun getDefaultTableSync(): TableBean? = tableDao.getDefaultTableSync()

    suspend fun getTableById(id: Int): TableBean? = tableDao.getTableById(id)

    suspend fun insertTable(table: TableBean): Long = tableDao.insertTable(table)

    suspend fun updateTable(table: TableBean) = tableDao.updateTable(table)

    suspend fun changeDefaultTable(newId: Int) {
        tableDao.clearDefault()
        tableDao.setDefault(newId)
    }

    suspend fun deleteTable(table: TableBean) = tableDao.deleteTable(table)
}
