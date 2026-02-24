package com.example.visprog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.visprog.data.LocationData
import com.google.android.gms.location.*
import com.google.gson.Gson
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class LocationService : Service() {

    companion object {
        const val ACTION_LOCATION_UPDATE = "com.example.visprog.LOCATION_UPDATE"
        private const val LOG_FILE_NAME = "location_log_service.json"
        private const val NOTIFICATION_CHANNEL_ID = "location_service_channel"
        private const val NOTIFICATION_ID = 1
    }

    private val logTag = "LocationService"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager

    private val serverIp = "192.168.0.11"
    private val serverPort = "5556"
    private val zmqContext = ZContext()
    private var isServiceRunning = false

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isServiceRunning) return START_STICKY
        
        Log.d(logTag, "Location service started.")
        startForegroundService("Ожидание сигнала GPS...")
        setupLocationUpdates()
        isServiceRunning = true
        return START_STICKY
    }

    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(10)).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    val locationData = LocationData(
                        latitude = it.latitude, longitude = it.longitude, altitude = it.altitude, time = System.currentTimeMillis()
                    )
                    Log.d(logTag, "New location: $locationData")
                    
                    // ИЗМЕНЕНО: Обновляем уведомление с новыми данными
                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val formattedTime = sdf.format(Date(locationData.time))
                    updateNotification("Последнее обновление в $formattedTime")

                    sendLocationToServer(locationData)
                    saveLocationLocally(locationData)
                    broadcastLocationUpdate(locationData)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            Log.e(logTag, "Location permission lost. Stopping service.")
            stopSelf()
        }
    }
    
    // ... (broadcastLocationUpdate, saveLocationLocally, sendLocationToServer остаются без изменений)

    private fun broadcastLocationUpdate(data: LocationData) {
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra("latitude", data.latitude)
            putExtra("longitude", data.longitude)
            putExtra("altitude", data.altitude)
            putExtra("time", data.time)
        }
        sendBroadcast(intent)
    }
    
    private fun saveLocationLocally(data: LocationData) {
        try {
            val file = File(filesDir, LOG_FILE_NAME)
            val json = Gson().toJson(data)
            file.appendText(json + "\n")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to write to local log file", e)
        }
    }

    private fun sendLocationToServer(data: LocationData) {
        thread {
            var socket: ZMQ.Socket? = null
            try {
                socket = zmqContext.createSocket(SocketType.REQ)
                socket.setReceiveTimeOut(3000)
                socket.connect("tcp://$serverIp:$serverPort")
                val json = Gson().toJson(data)
                socket.send(json.toByteArray(ZMQ.CHARSET), 0)
                val reply = socket.recv(0)
                if (reply != null) {
                    Log.d(logTag, "Server reply: ${String(reply, ZMQ.CHARSET)}")
                } else {
                    Log.w(logTag, "No reply from server.")
                }
            } catch (e: Exception) {
                Log.e(logTag, "ZMQ Error: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    private fun startForegroundService(initialText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Location Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(chan)
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Отслеживание местоположения")
            .setContentText(initialText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Замените на свою иконку
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    // ИЗМЕНЕНО: Новый метод для обновления текста уведомления
    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Отслеживание местоположения")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        zmqContext.destroy()
        Log.d(logTag, "Location service stopped.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
