package com.solunis.schedule.mcp

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.solunis.schedule.HomeActivity
import com.solunis.schedule.R
import com.solunis.schedule.data.database.AppDatabase

class McpService : Service() {

    private var mcpServer: McpServer? = null

    companion object {
        const val TAG = "McpService"
        const val CHANNEL_ID = "mcp_service"
        const val NOTIFICATION_ID = 1001
        var isRunning = false
            private set
        var serverPort = McpServer.DEFAULT_PORT
            private set
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("MCP Server 运行中 — 端口 $serverPort")
        startForeground(NOTIFICATION_ID, notification)

        if (mcpServer == null) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                mcpServer = McpServer(serverPort, db)
                mcpServer?.start()
                isRunning = true
                Log.i(TAG, "MCP Server started on port $serverPort")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start MCP Server", e)
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mcpServer?.stop()
        mcpServer = null
        isRunning = false
        Log.i(TAG, "MCP Server stopped")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MCP Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "WakeupSchedule MCP Server 服务"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WakeupSchedule")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
