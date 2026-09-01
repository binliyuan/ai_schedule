package com.solunis.schedule.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.solunis.schedule.data.database.entity.TableBean

@Dao
interface TableDao {

    @Query("SELECT * FROM table_bean")
    fun getAllTables(): LiveData<List<TableBean>>

    @Query("SELECT * FROM table_bean WHERE type = 1 LIMIT 1")
    fun getDefaultTable(): LiveData<TableBean?>

    @Query("SELECT * FROM table_bean WHERE type = 1 LIMIT 1")
    suspend fun getDefaultTableSync(): TableBean?

    @Query("SELECT * FROM table_bean WHERE id = :id")
    suspend fun getTableById(id: Int): TableBean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableBean): Long

    @Update
    suspend fun updateTable(table: TableBean)

    @Query("UPDATE table_bean SET type = 0")
    suspend fun clearDefault()

    @Query("UPDATE table_bean SET type = 1 WHERE id = :id")
    suspend fun setDefault(id: Int)

    @Delete
    suspend fun deleteTable(table: TableBean)
}
