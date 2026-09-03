package com.solunis.schedule

import android.app.Application
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.local.TokenManager

class WakeupScheduleApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        TokenManager.init(this)
    }
}
