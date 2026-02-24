package com.example.visprog

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.Handler
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val locationFile by lazy {
        File(filesDir, "location_log_service.json")
    }

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val ACTION_LOCATION_UPDATE = "com.example.visprog.LOCATION_UPDATE"
        const val NOTIFICATION_CHANNEL_ID = "location_channel"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val localDateTime = Instant.ofEpochMilli(location.time)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                    val currentTime = localDateTime.format(formatter)

                    val locationData = LocationData(
                        location.latitude,
                        location.longitude,
                        location.altitude,
                        currentTime
                    )
                    saveLocationToFile(locationData)

                    val intent = Intent(ACTION_LOCATION_UPDATE).apply {
                        putExtra("latitude", locationData.latitude)
                        putExtra("longitude", locationData.longitude)
                        putExtra("altitude", locationData.altitude)
                        putExtra("time", locationData.time)
                    }
                    sendBroadcast(intent)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Отслеживание местоположения")
            .setContentText("Сервис работает в фоновом режиме")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun saveLocationToFile(locationData: LocationData) {
        try {
            val gson = Gson()
            val jsonString = gson.toJson(locationData)
            locationFile.appendText(jsonString + "\n")
            
            // Show toast on main thread
            handler.post {
                Toast.makeText(applicationContext, "Координаты записаны в файл", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Канал сервиса местоположения",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}