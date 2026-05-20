package com.example.visprog.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.visprog.R
import com.example.visprog.data.LocationData
import com.google.android.gms.location.*
import org.json.JSONObject
import java.io.File

class LocationService : Service() {
    private val channelId = "LocationServiceChannel"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        const val ACTION_LOCATION_UPDATE = "com.example.visprog.services.LOCATION_UPDATE"
        var isRunning = false
        var lastLocationData: LocationData? = null
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_STICKY
        
        isRunning = true
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Отслеживание локации")
            .setContentText("Сервис запущен")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(2, notification)
        setupLocationUpdates()
        return START_STICKY
    }

    private fun setupLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                val locData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    accuracy = location.accuracy,
                    time = System.currentTimeMillis()
                )
                lastLocationData = locData
                saveToLog(locData)
                broadcastUpdate(locData)
            }
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    private fun saveToLog(data: LocationData) {
        try {
            val file = File(filesDir, "location_log_service.json")
            val json = data.toJSONObject().toString() + "\n"
            file.appendText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun broadcastUpdate(data: LocationData) {
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            setPackage(packageName)
            putExtra("latitude", data.latitude)
            putExtra("longitude", data.longitude)
            putExtra("altitude", data.altitude)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(NotificationChannel(channelId, "Location Updates", NotificationManager.IMPORTANCE_LOW))
        }
    }
}
