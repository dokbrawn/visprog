package com.example.visprog.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*

class BackgroundService : Service() {

    private val LOG_TAG = "BG_SERVICE"
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent): IBinder? = null

    private fun sendMessageToActivity(msg: String?) {
        val intent = Intent("BackGroundUpdate")
        intent.putExtra("Status", msg)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            for (i in 0 until 1000) {
                delay(1000)
                sendMessageToActivity("Task running: $i")
                Log.d(LOG_TAG, "Task running: $i")
            }
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(LOG_TAG, "Service destroyed")
    }
}
