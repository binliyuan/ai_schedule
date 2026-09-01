package com.solunis.schedule.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solunis.schedule.data.database.dao.CourseDao
import com.solunis.schedule.data.database.dao.HomeworkDao
import com.solunis.schedule.data.database.dao.TableDao
import com.solunis.schedule.data.database.dao.TimeDao
import com.solunis.schedule.data.database.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TimeTableBean::class,
        TimeDetailBean::class,
        TableBean::class,
        CourseBaseBean::class,
        CourseDetailBean::class,
        HomeworkBean::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun tableDao(): TableDao
    abstract fun timeDao(): TimeDao
    abstract fun homeworkDao(): HomeworkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wakeup_schedule.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val timeTableId = database.timeDao().insertTimeTable(
                TimeTableBean(name = "默认", sameLen = true, courseLen = 50)
            ).toInt()

            val defaultTimeDetails = listOf(
                TimeDetailBean(1, "08:00", "08:50", timeTableId),
                TimeDetailBean(2, "08:55", "09:45", timeTableId),
                TimeDetailBean(3, "10:05", "10:55", timeTableId),
                TimeDetailBean(4, "11:00", "11:50", timeTableId),
                TimeDetailBean(5, "14:00", "14:50", timeTableId),
                TimeDetailBean(6, "14:55", "15:45", timeTableId),
                TimeDetailBean(7, "16:05", "16:55", timeTableId),
                TimeDetailBean(8, "17:00", "17:50", timeTableId),
                TimeDetailBean(9, "19:00", "19:50", timeTableId),
                TimeDetailBean(10, "19:55", "20:45", timeTableId),
                TimeDetailBean(11, "21:00", "21:50", timeTableId),
                TimeDetailBean(12, "21:55", "22:45", timeTableId)
            )
            database.timeDao().insertTimeDetails(defaultTimeDetails)

            database.tableDao().insertTable(
                TableBean(
                    tableName = "默认课表",
                    nodes = 12,
                    maxWeek = 20,
                    startDate = "2024-09-02",
                    type = 1,
                    timeTable = timeTableId
                )
            )
        }
    }
}
