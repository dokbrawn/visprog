package com.example.visprog.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.visprog.data.LocationData
import com.example.visprog.network.ZeroMQClient
import com.example.visprog.supp.FileUtils
import java.io.File
import kotlin.concurrent.thread

class LocationService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val TAG = "LocationService"
    private val NOTIFICATION_CHANNEL_ID = "LocationServiceChannel"
    private lateinit var zmqClient: ZeroMQClient

    companion object {
        const val ACTION_LOCATION_UPDATE = "com.example.visprog.services.LOCATION_UPDATE"
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
        // Use the correct IP and port for the C++ server
        zmqClient = ZeroMQClient("192.168.0.11", 5556)
        thread { zmqClient.start() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Location service started")
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Tracking location in the background")
            .build()

        startForeground(1, notification)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permissions not granted, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, this)

        return START_STICKY
    }

    override fun onLocationChanged(location: Location) {
        val locationData = LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            time = System.currentTimeMillis()
        )

        val file = File(filesDir, "location_log_service.json")
        FileUtils.saveLocationToFile(file, locationData)
        thread { zmqClient.sendLocationData(locationData) }

        // Broadcast the location update
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra("latitude", location.latitude)
            putExtra("longitude", location.longitude)
            putExtra("altitude", location.altitude)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
        thread { zmqClient.stop() }
        Log.d(TAG, "Location service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}
