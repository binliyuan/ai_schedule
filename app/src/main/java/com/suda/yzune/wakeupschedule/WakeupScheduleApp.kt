package com.suda.yzune.wakeupschedule

import android.app.Application
import com.suda.yzune.wakeupschedule.data.database.AppDatabase
import com.suda.yzune.wakeupschedule.data.network.CronetClient

class WakeupScheduleApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        CronetClient.initialize(this)
    }
}
