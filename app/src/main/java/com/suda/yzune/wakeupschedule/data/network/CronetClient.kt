package com.suda.yzune.wakeupschedule.data.network

import android.content.Context
import com.google.android.gms.net.CronetProviderInstaller
import org.chromium.net.CronetEngine

object CronetClient {

    @Volatile
    private var engine: CronetEngine? = null

    fun initialize(context: Context) {
        CronetProviderInstaller.installProvider(context).addOnSuccessListener {
            engine = CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .build()
        }
    }

    fun getEngine(): CronetEngine? = engine
}
